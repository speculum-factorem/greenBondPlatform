package com.esgbank.greenbond.verification.service;

import com.esgbank.greenbond.verification.dto.DocumentResponse;
import com.esgbank.greenbond.verification.dto.DocumentUploadRequest;
import com.esgbank.greenbond.verification.exception.DocumentNotFoundException;
import com.esgbank.greenbond.verification.exception.DocumentProcessingException;
import com.esgbank.greenbond.verification.mapper.DocumentMapper;
import com.esgbank.greenbond.verification.model.Document;
import com.esgbank.greenbond.verification.model.enums.DocumentStatus;
import com.esgbank.greenbond.verification.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления документами.
 * Обеспечивает загрузку, хранение, обработку и верификацию документов для зеленых облигаций.
 * Использует MDC для трейсинга запросов через все сервисы.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final FileStorageService fileStorageService;
    private final DocumentProcessingService documentProcessingService;
    private final AuditService auditService;

    public DocumentResponse uploadDocument(DocumentUploadRequest request, MultipartFile file,
                                           String issuerId, String issuerName, String uploadedBy) {
        // Получаем requestId из MDC для трейсинга запроса
        String requestId = MDC.get("requestId");
        log.info("Uploading document: {} for bond: {}, issuer: {}, requestId: {}",
                request.getDocumentName(), request.getBondId(), issuerId, requestId);

        try {
            // Валидируем файл (размер, тип, безопасность)
            fileStorageService.validateFile(file);

            // Вычисляем SHA-256 хеш файла для проверки дубликатов и целостности
            String fileHash = calculateFileHash(file);

            // Проверяем нет ли уже такого же документа (по хешу) для этой облигации
            if (documentRepository.findByBondIdAndDocumentType(request.getBondId(), request.getDocumentType())
                    .stream().anyMatch(doc -> fileHash.equals(doc.getFileHash()))) {
                throw new DocumentProcessingException("Duplicate document detected");
            }

            // Сохраняем файл на диск в директорию для облигации
            String filePath = fileStorageService.storeFile(file, request.getBondId());

            // Генерируем уникальный ID для документа
            String documentId = UUID.randomUUID().toString();

            // Создаем сущность документа с начальным статусом UPLOADED
            Document document = Document.builder()
                    .documentId(documentId)
                    .bondId(request.getBondId())
                    .issuerId(issuerId)
                    .issuerName(issuerName)
                    .documentName(request.getDocumentName())
                    .originalFileName(file.getOriginalFilename())
                    .documentType(request.getDocumentType())
                    .status(DocumentStatus.UPLOADED)
                    .filePath(filePath)
                    .fileHash(fileHash)
                    .mimeType(file.getContentType())
                    .fileSize(file.getSize())
                    .uploadedBy(uploadedBy)
                    .build();

            // Сохраняем документ в MongoDB
            Document savedDocument = documentRepository.save(document);

            // Запускаем асинхронную обработку документа (извлечение метаданных, полей)
            documentProcessingService.processDocumentAsync(savedDocument);

            // Записываем действие в аудит трейл
            auditService.logDocumentAction(savedDocument.getDocumentId(), "UPLOAD",
                    uploadedBy, "Document uploaded successfully");

            log.info("Document uploaded successfully: {}, file: {}",
                    savedDocument.getDocumentId(), file.getOriginalFilename());

            return documentMapper.toResponse(savedDocument);

        } catch (IOException e) {
            log.error("File storage error for document: {}. Error: {}",
                    request.getDocumentName(), e.getMessage(), e);
            throw new DocumentProcessingException("File storage failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Document upload failed: {}. Error: {}",
                    request.getDocumentName(), e.getMessage(), e);
            throw new DocumentProcessingException("Document upload failed: " + e.getMessage(), e);
        }
    }

    public DocumentResponse getDocument(String documentId) {
        log.debug("Fetching document: {}", documentId);

        Document document = documentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));

        return documentMapper.toResponse(document);
    }

    public DocumentResponse getDocumentById(String id) {
        log.debug("Fetching document by internal id: {}", id);

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with id: " + id));

        return documentMapper.toResponse(document);
    }

    public Page<DocumentResponse> getDocumentsByBond(String bondId, Pageable pageable) {
        log.debug("Fetching documents for bond: {}, page: {}", bondId, pageable.getPageNumber());

        Page<Document> documents = documentRepository.findByBondId(bondId, pageable);
        return documents.map(documentMapper::toResponse);
    }

    public Page<DocumentResponse> getDocumentsByIssuer(String issuerId, Pageable pageable) {
        log.debug("Fetching documents for issuer: {}, page: {}", issuerId, pageable.getPageNumber());

        Page<Document> documents = documentRepository.findByIssuerId(issuerId, pageable);
        return documents.map(documentMapper::toResponse);
    }

    public Page<DocumentResponse> getDocumentsByStatus(DocumentStatus status, Pageable pageable) {
        log.debug("Fetching documents with status: {}, page: {}", status, pageable.getPageNumber());

        Page<Document> documents = documentRepository.findByStatus(status, pageable);
        return documents.map(documentMapper::toResponse);
    }

    public List<DocumentResponse> getVerifiedDocumentsByBond(String bondId) {
        log.debug("Fetching verified documents for bond: {}", bondId);

        List<Document> documents = documentRepository.findVerifiedDocumentsByBond(bondId);
        return documents.stream().map(documentMapper::toResponse).toList();
    }

    public void deleteDocument(String documentId, String issuerId) {
        log.info("Deleting document: {}, issuer: {}", documentId, issuerId);

        Document document = documentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));

        // Проверяем что эмитент имеет право удалять этот документ
        if (!document.getIssuerId().equals(issuerId)) {
            throw new DocumentProcessingException("Unauthorized access to document: " + documentId);
        }

        try {
            // Удаляем физический файл с диска
            Files.deleteIfExists(Paths.get(document.getFilePath()));

            // Удаляем из базы данных
            documentRepository.delete(document);

            // Записываем действие в аудит трейл
            auditService.logDocumentAction(documentId, "DELETE", issuerId, "Document deleted");

            log.info("Document deleted successfully: {}", documentId);

        } catch (IOException e) {
            log.error("File deletion failed for document: {}. Error: {}", documentId, e.getMessage());
            throw new DocumentProcessingException("File deletion failed: " + e.getMessage(), e);
        }
    }

    public byte[] downloadDocument(String documentId) {
        log.debug("Downloading document: {}", documentId);

        Document document = documentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));

        try {
            Path filePath = Paths.get(document.getFilePath());
            if (!Files.exists(filePath)) {
                throw new DocumentProcessingException("File not found: " + document.getFilePath());
            }

            // Audit trail
            auditService.logDocumentAction(documentId, "DOWNLOAD", "SYSTEM", "Document downloaded");

            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            log.error("File read error for document: {}. Error: {}", documentId, e.getMessage(), e);
            throw new DocumentProcessingException("File read failed: " + e.getMessage(), e);
        }
    }

    private String calculateFileHash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(file.getBytes());
        return HexFormat.of().formatHex(hash);
    }
}