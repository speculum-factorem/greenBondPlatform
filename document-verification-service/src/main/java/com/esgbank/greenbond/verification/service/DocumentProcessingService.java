package com.esgbank.greenbond.verification.service;

import com.esgbank.greenbond.verification.model.Document;
import com.esgbank.greenbond.verification.model.DocumentField;
import com.esgbank.greenbond.verification.model.VerificationStep;
import com.esgbank.greenbond.verification.model.enums.DocumentStatus;
import com.esgbank.greenbond.verification.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final AuditService auditService;
    private final Tika tika = new Tika();

    // Асинхронная обработка документа: извлечение метаданных, валидация и извлечение полей
    @Async
    public void processDocumentAsync(Document document) {
        log.info("Starting async processing for document: {}", document.getDocumentId());

        try {
            // Обновляем статус на PROCESSING
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);

            // Шаг 1: Извлекаем метаданные и содержимое документа через Apache Tika
            processExtraction(document);

            // Шаг 2: Валидируем структуру документа (размер, тип файла)
            processValidation(document);

            // Шаг 3: Извлекаем специфичные поля в зависимости от типа документа
            processFieldExtraction(document);

            // Обновляем статус на EXTRACTION_COMPLETED - документ готов к верификации
            document.setStatus(DocumentStatus.EXTRACTION_COMPLETED);
            documentRepository.save(document);

            // Записываем успешное завершение обработки в аудит трейл
            auditService.logDocumentAction(document.getDocumentId(), "PROCESSING_COMPLETED",
                    "SYSTEM", "Document processing completed successfully");

            log.info("Document processing completed: {}", document.getDocumentId());

        } catch (Exception e) {
            log.error("Document processing failed: {}. Error: {}",
                    document.getDocumentId(), e.getMessage(), e);

            // При ошибке обработки помечаем документ как REJECTED
            document.setStatus(DocumentStatus.REJECTED);
            documentRepository.save(document);

            // Записываем ошибку в аудит трейл
            auditService.logDocumentAction(document.getDocumentId(), "PROCESSING_FAILED",
                    "SYSTEM", "Document processing failed: " + e.getMessage());
        }
    }

    // Извлечение метаданных и содержимого из документа используя Apache Tika
    private void processExtraction(Document document) throws IOException, TikaException, SAXException {
        log.debug("Extracting content from document: {}", document.getDocumentId());

        // Создаем парсер для автоматического определения типа документа
        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1); // Без ограничения размера
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        try (FileInputStream stream = new FileInputStream(document.getFilePath())) {
            // Парсим документ и извлекаем метаданные
            parser.parse(stream, handler, metadata, context);

            // Извлекаем метаданные (автор, дата создания, количество страниц и т.д.)
            Map<String, Object> extractedMetadata = extractMetadata(metadata);
            document.setMetadata(extractedMetadata);

            // Добавляем шаг извлечения в историю обработки
            VerificationStep extractionStep = VerificationStep.builder()
                    .stepName("CONTENT_EXTRACTION")
                    .status("COMPLETED")
                    .performedBy("SYSTEM")
                    .performedAt(LocalDateTime.now())
                    .comments("Content extracted successfully")
                    .details(Map.of("contentLength", handler.toString().length()))
                    .build();

            addVerificationStep(document, extractionStep);

            log.debug("Content extraction completed for document: {}", document.getDocumentId());
        }
    }

    // Валидация документа: проверка размера файла и типа
    private void processValidation(Document document) {
        log.debug("Validating document: {}", document.getDocumentId());

        List<String> validationErrors = new ArrayList<>();

        // Проверяем размер файла (максимум 10MB)
        if (document.getFileSize() > 10 * 1024 * 1024) { // 10MB
            validationErrors.add("File size exceeds limit");
        }

        // Validate MIME type
        if (!isAllowedMimeType(document.getMimeType())) {
            validationErrors.add("Unsupported file type: " + document.getMimeType());
        }

        VerificationStep validationStep = VerificationStep.builder()
                .stepName("DOCUMENT_VALIDATION")
                .status(validationErrors.isEmpty() ? "COMPLETED" : "FAILED")
                .performedBy("SYSTEM")
                .performedAt(LocalDateTime.now())
                .comments(validationErrors.isEmpty() ? "Validation passed" :
                        "Validation failed: " + String.join(", ", validationErrors))
                .details(Map.of("errors", validationErrors))
                .build();

        addVerificationStep(document, validationStep);

        if (!validationErrors.isEmpty()) {
            throw new RuntimeException("Document validation failed: " + String.join(", ", validationErrors));
        }

        log.debug("Document validation completed for document: {}", document.getDocumentId());
    }

    private void processFieldExtraction(Document document) {
        log.debug("Extracting fields from document: {}", document.getDocumentId());

        List<DocumentField> extractedFields = new ArrayList<>();

        // Extract common fields based on document type
        switch (document.getDocumentType()) {
            case ESG_REPORT:
                extractedFields.addAll(extractESGFields());
                break;
            case FINANCIAL_STATEMENT:
                extractedFields.addAll(extractFinancialFields());
                break;
            case PROJECT_PROPOSAL:
                extractedFields.addAll(extractProjectFields());
                break;
            default:
                extractedFields.addAll(extractGenericFields());
        }

        document.setExtractedFields(extractedFields);

        VerificationStep fieldExtractionStep = VerificationStep.builder()
                .stepName("FIELD_EXTRACTION")
                .status("COMPLETED")
                .performedBy("SYSTEM")
                .performedAt(LocalDateTime.now())
                .comments("Extracted " + extractedFields.size() + " fields")
                .details(Map.of("extractedFieldsCount", extractedFields.size()))
                .build();

        addVerificationStep(document, fieldExtractionStep);

        log.debug("Field extraction completed for document: {}", document.getDocumentId());
    }

    private Map<String, Object> extractMetadata(Metadata metadata) {
        return Map.of(
                "author", metadata.get("Author"),
                "title", metadata.get("title"),
                "creationDate", metadata.get("Creation-Date"),
                "lastModified", metadata.get("Last-Modified"),
                "pageCount", metadata.get("xmpTPg:NPages"),
                "wordCount", metadata.get("meta:word-count")
        );
    }

    private List<DocumentField> extractESGFields() {
        return List.of(
                createField("carbon_emissions", null, 0.0, "NUMBER", false, "AUTO_EXTRACTED"),
                createField("energy_consumption", null, 0.0, "NUMBER", false, "AUTO_EXTRACTED"),
                createField("water_usage", null, 0.0, "NUMBER", false, "AUTO_EXTRACTED"),
                createField("waste_management", null, 0.0, "NUMBER", false, "AUTO_EXTRACTED")
        );
    }

    private List<DocumentField> extractFinancialFields() {
        return List.of(
                createField("revenue", null, 0.0, "NUMBER", false, "AUTO_EXTRACTED"),
                createField("net_income", null, 0.0, "NUMBER", false, "AUTO_EXTRACTED"),
                createField("total_assets", null, 0.0, "NUMBER", false, "AUTO_EXTRACTED"),
                createField("liabilities", null, 0.0, "NUMBER", false, "AUTO_EXTRACTED")
        );
    }

    private List<DocumentField> extractProjectFields() {
        return List.of(
                createField("project_budget", null, 0.0, "NUMBER", false, "AUTO_EXTRACTED"),
                createField("timeline", null, 0.0, "STRING", false, "AUTO_EXTRACTED"),
                createField("expected_impact", null, 0.0, "STRING", false, "AUTO_EXTRACTED"),
                createField("stakeholders", null, 0.0, "STRING", false, "AUTO_EXTRACTED")
        );
    }

    private List<DocumentField> extractGenericFields() {
        return List.of(
                createField("document_title", null, 0.0, "STRING", false, "AUTO_EXTRACTED"),
                createField("summary", null, 0.0, "STRING", false, "AUTO_EXTRACTED")
        );
    }

    private DocumentField createField(String name, String value, Double confidence,
                                      String dataType, Boolean isVerified, String source) {
        return DocumentField.builder()
                .fieldName(name)
                .fieldValue(value)
                .confidence(confidence)
                .dataType(dataType)
                .isVerified(isVerified)
                .verificationSource(source)
                .build();
    }

    private boolean isAllowedMimeType(String mimeType) {
        return mimeType != null && (
                mimeType.startsWith("application/pdf") ||
                        mimeType.startsWith("application/msword") ||
                        mimeType.startsWith("application/vnd.openxmlformats-officedocument") ||
                        mimeType.startsWith("image/") ||
                        mimeType.startsWith("text/")
        );
    }

    private void addVerificationStep(Document document, VerificationStep step) {
        if (document.getVerificationSteps() == null) {
            document.setVerificationSteps(new ArrayList<>());
        }
        document.getVerificationSteps().add(step);
        documentRepository.save(document);
    }
}