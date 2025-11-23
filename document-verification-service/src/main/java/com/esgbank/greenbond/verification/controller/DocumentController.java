package com.esgbank.greenbond.verification.controller;

import com.esgbank.greenbond.verification.dto.DocumentResponse;
import com.esgbank.greenbond.verification.dto.DocumentUploadRequest;
import com.esgbank.greenbond.verification.model.enums.DocumentStatus;
import com.esgbank.greenbond.verification.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "APIs for managing document upload and retrieval")
public class DocumentController {

    private final DocumentService documentService;

    // Эндпоинт для загрузки документа для верификации
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document", description = "Upload a document for verification")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid document or file"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<DocumentResponse> uploadDocument(
            @Parameter(description = "Document upload request")
            @RequestPart @Valid DocumentUploadRequest request,
            @Parameter(description = "Document file")
            @RequestPart MultipartFile file,
            @RequestHeader("X-User-Id") String issuerId,
            @RequestHeader("X-User-Name") String issuerName) {

        log.info("REST API: Uploading document: {} for bond: {}",
                request.getDocumentName(), request.getBondId());

        // Загружаем документ через сервис
        DocumentResponse response = documentService.uploadDocument(
                request, file, issuerId, issuerName, issuerName);

        return ResponseEntity.ok(response);
    }

    // Эндпоинт для получения информации о документе по ID
    @GetMapping("/{documentId}")
    @Operation(summary = "Get document details", description = "Get detailed information about a document")
    public ResponseEntity<DocumentResponse> getDocument(
            @Parameter(description = "Document ID") @PathVariable String documentId) {

        log.debug("REST API: Getting document details: {}", documentId);

        // Получаем документ через сервис
        DocumentResponse response = documentService.getDocument(documentId);
        return ResponseEntity.ok(response);
    }

    // Эндпоинт для получения списка документов по облигации с пагинацией
    @GetMapping("/bond/{bondId}")
    @Operation(summary = "Get documents by bond", description = "Get paginated list of documents for a bond")
    public ResponseEntity<Page<DocumentResponse>> getDocumentsByBond(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.debug("REST API: Getting documents for bond: {}, page: {}", bondId, pageable.getPageNumber());

        // Получаем документы через сервис с пагинацией
        Page<DocumentResponse> documents = documentService.getDocumentsByBond(bondId, pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/issuer/{issuerId}")
    @Operation(summary = "Get documents by issuer", description = "Get paginated list of documents for an issuer")
    public ResponseEntity<Page<DocumentResponse>> getDocumentsByIssuer(
            @Parameter(description = "Issuer ID") @PathVariable String issuerId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.debug("REST API: Getting documents for issuer: {}, page: {}", issuerId, pageable.getPageNumber());

        Page<DocumentResponse> documents = documentService.getDocumentsByIssuer(issuerId, pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get documents by status", description = "Get paginated list of documents by status")
    public ResponseEntity<Page<DocumentResponse>> getDocumentsByStatus(
            @Parameter(description = "Document status") @PathVariable DocumentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        log.debug("REST API: Getting documents with status: {}, page: {}", status, pageable.getPageNumber());

        Page<DocumentResponse> documents = documentService.getDocumentsByStatus(status, pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/bond/{bondId}/verified")
    @Operation(summary = "Get verified documents by bond", description = "Get list of verified documents for a bond")
    public ResponseEntity<List<DocumentResponse>> getVerifiedDocumentsByBond(
            @Parameter(description = "Bond ID") @PathVariable String bondId) {

        log.debug("REST API: Getting verified documents for bond: {}", bondId);

        List<DocumentResponse> documents = documentService.getVerifiedDocumentsByBond(bondId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}/download")
    @Operation(summary = "Download document", description = "Download the document file")
    public ResponseEntity<byte[]> downloadDocument(
            @Parameter(description = "Document ID") @PathVariable String documentId) {

        log.debug("REST API: Downloading document: {}", documentId);

        byte[] fileContent = documentService.downloadDocument(documentId);
        DocumentResponse document = documentService.getDocument(documentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, document.getMimeType())
                .body(fileContent);
    }

    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete a document", description = "Delete a document and its file")
    public ResponseEntity<Map<String, String>> deleteDocument(
            @Parameter(description = "Document ID") @PathVariable String documentId,
            @RequestHeader("X-User-Id") String issuerId) {

        log.info("REST API: Deleting document: {}, issuer: {}", documentId, issuerId);

        documentService.deleteDocument(documentId, issuerId);

        return ResponseEntity.ok(Map.of(
                "message", "Document deleted successfully",
                "documentId", documentId,
                "requestId", MDC.get("requestId")
        ));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check document verification service health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("REST API: Health check");

        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "document-verification-service",
                "timestamp", System.currentTimeMillis()
        ));
    }
}