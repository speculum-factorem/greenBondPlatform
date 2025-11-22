package com.esgbank.greenbond.verification.controller;

import com.esgbank.greenbond.verification.dto.DocumentResponse;
import com.esgbank.greenbond.verification.dto.DocumentUploadRequest;
import com.esgbank.greenbond.verification.model.enums.DocumentStatus;
import com.esgbank.greenbond.verification.model.enums.DocumentType;
import com.esgbank.greenbond.verification.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentService documentService;

    @Test
    void shouldGetDocumentSuccessfully() throws Exception {
        // Given
        DocumentResponse response = createDocumentResponse();
        when(documentService.getDocument("DOC-123")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/documents/DOC-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("DOC-123"))
                .andExpect(jsonPath("$.documentName").value("Test Document"));
    }

    @Test
    void shouldGetDocumentsByBond() throws Exception {
        // Given
        DocumentResponse response = createDocumentResponse();
        Page<DocumentResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(documentService.getDocumentsByBond(eq("bond-123"), any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/documents/bond/bond-123")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].documentId").value("DOC-123"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldDownloadDocument() throws Exception {
        // Given
        byte[] fileContent = "test content".getBytes();
        when(documentService.downloadDocument("DOC-123")).thenReturn(fileContent);

        DocumentResponse document = createDocumentResponse();
        when(documentService.getDocument("DOC-123")).thenReturn(document);

        // When & Then
        mockMvc.perform(get("/api/v1/documents/DOC-123/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.pdf\""))
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void shouldDeleteDocument() throws Exception {
        // Given & When & Then
        mockMvc.perform(delete("/api/v1/documents/DOC-123")
                        .header("X-User-Id", "issuer-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Document deleted successfully"))
                .andExpect(jsonPath("$.documentId").value("DOC-123"));
    }

    private DocumentResponse createDocumentResponse() {
        return DocumentResponse.builder()
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