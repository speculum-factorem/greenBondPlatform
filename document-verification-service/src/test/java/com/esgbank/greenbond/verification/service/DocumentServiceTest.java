package com.esgbank.greenbond.verification.service;

import com.esgbank.greenbond.verification.dto.DocumentUploadRequest;
import com.esgbank.greenbond.verification.exception.DocumentNotFoundException;
import com.esgbank.greenbond.verification.exception.DocumentProcessingException;
import com.esgbank.greenbond.verification.mapper.DocumentMapper;
import com.esgbank.greenbond.verification.mapper.DocumentMapperImpl;
import com.esgbank.greenbond.verification.model.Document;
import com.esgbank.greenbond.verification.model.enums.DocumentStatus;
import com.esgbank.greenbond.verification.model.enums.DocumentType;
import com.esgbank.greenbond.verification.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private DocumentProcessingService documentProcessingService;

    @Mock
    private AuditService auditService;

    @Mock
    private MultipartFile multipartFile;

    private DocumentMapper documentMapper = new DocumentMapperImpl();

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(documentRepository, documentMapper,
                fileStorageService, documentProcessingService, auditService);
    }

    @Test
    void shouldGetDocumentSuccessfully() {
        // Given
        String documentId = "DOC-123";
        Document document = createDocument();

        when(documentRepository.findByDocumentId(documentId)).thenReturn(Optional.of(document));

        // When
        var result = documentService.getDocument(documentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDocumentId()).isEqualTo(documentId);
        assertThat(result.getDocumentName()).isEqualTo("Test Document");
    }

    @Test
    void shouldThrowExceptionWhenDocumentNotFound() {
        // Given
        String documentId = "non-existent-doc";
        when(documentRepository.findByDocumentId(documentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> documentService.getDocument(documentId))
                .isInstanceOf(DocumentNotFoundException.class)
                .hasMessageContaining("Document not found");
    }

    @Test
    void shouldGetDocumentsByBond() {
        // Given
        String bondId = "bond-123";
        Pageable pageable = PageRequest.of(0, 10);
        Document document = createDocument();
        Page<Document> documentPage = new PageImpl<>(List.of(document), pageable, 1);

        when(documentRepository.findByBondId(bondId, pageable)).thenReturn(documentPage);

        // When
        Page<Document> result = documentRepository.findByBondId(bondId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getBondId()).isEqualTo(bondId);
    }

    @Test
    void shouldDeleteDocumentSuccessfully() throws IOException {
        // Given
        String documentId = "DOC-123";
        String issuerId = "issuer-123";
        Document document = createDocument();
        document.setIssuerId(issuerId);

        when(documentRepository.findByDocumentId(documentId)).thenReturn(Optional.of(document));
        doNothing().when(fileStorageService).deleteFile(document.getFilePath());

        // When
        documentService.deleteDocument(documentId, issuerId);

        // Then
        verify(documentRepository).delete(document);
        verify(fileStorageService).deleteFile(document.getFilePath());
        verify(auditService).logDocumentAction(documentId, "DELETE", issuerId, "Document deleted");
    }

    @Test
    void shouldThrowExceptionWhenUnauthorizedDeletion() {
        // Given
        String documentId = "DOC-123";
        String unauthorizedIssuer = "unauthorized-issuer";
        Document document = createDocument();
        document.setIssuerId("authorized-issuer");

        when(documentRepository.findByDocumentId(documentId)).thenReturn(Optional.of(document));

        // When & Then
        assertThatThrownBy(() -> documentService.deleteDocument(documentId, unauthorizedIssuer))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("Unauthorized access");
    }

    private Document createDocument() {
        return Document.builder()
                .id("doc-uuid")
                .documentId("DOC-123")
                .bondId("bond-123")
                .issuerId("issuer-123")
                .issuerName("Test Issuer")
                .documentName("Test Document")
                .originalFileName("test.pdf")
                .documentType(DocumentType.ESG_REPORT)
                .status(DocumentStatus.UPLOADED)
                .filePath("/uploads/test.pdf")
                .fileHash("abc123")
                .mimeType("application/pdf")
                .fileSize(1024L)
                .uploadedBy("user-123")
                .build();
    }
}