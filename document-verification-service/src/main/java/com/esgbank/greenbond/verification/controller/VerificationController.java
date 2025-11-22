package com.esgbank.greenbond.verification.controller;

import com.esgbank.greenbond.verification.dto.DocumentResponse;
import com.esgbank.greenbond.verification.dto.VerificationRequest;
import com.esgbank.greenbond.verification.mapper.DocumentMapper;
import com.esgbank.greenbond.verification.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/verification")
@RequiredArgsConstructor
@Tag(name = "Document Verification", description = "APIs for document verification processes")
public class VerificationController {

    private final VerificationService verificationService;
    private final DocumentMapper documentMapper;

    @PostMapping("/documents/{documentId}/verify")
    @Operation(summary = "Verify a document", description = "Verify a document by an auditor")
    public ResponseEntity<DocumentResponse> verifyDocument(
            @Parameter(description = "Document ID") @PathVariable String documentId,
            @Parameter(description = "Verification request") @Valid @RequestBody VerificationRequest request,
            @RequestHeader("X-User-Id") String verifierId,
            @RequestHeader("X-User-Name") String verifierName) {

        log.info("REST API: Verifying document: {}, verifier: {}", documentId, verifierId);

        var document = verificationService.verifyDocument(documentId, request, verifierId, verifierName);
        DocumentResponse response = documentMapper.toResponse(document);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/documents/{documentId}/review")
    @Operation(summary = "Request document review", description = "Request a review for a document")
    public ResponseEntity<DocumentResponse> requestReview(
            @Parameter(description = "Document ID") @PathVariable String documentId,
            @Parameter(description = "Review comment") @RequestParam String comment,
            @RequestHeader("X-User-Id") String requesterId) {

        log.info("REST API: Requesting review for document: {}, requester: {}", documentId, requesterId);

        var document = verificationService.requestReview(documentId, comment, requesterId);
        DocumentResponse response = documentMapper.toResponse(document);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/documents/{documentId}/fields/{fieldName}")
    @Operation(summary = "Update field verification", description = "Update verification status of a specific field")
    public ResponseEntity<DocumentResponse> updateFieldVerification(
            @Parameter(description = "Document ID") @PathVariable String documentId,
            @Parameter(description = "Field name") @PathVariable String fieldName,
            @Parameter(description = "Verification status") @RequestParam Boolean isVerified,
            @RequestHeader("X-User-Id") String verifierId) {

        log.debug("REST API: Updating field verification: {}, field: {}", documentId, fieldName);

        var document = verificationService.updateFieldVerification(documentId, fieldName, isVerified, verifierId);
        DocumentResponse response = documentMapper.toResponse(document);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/bond/{bondId}/fully-verified")
    @Operation(summary = "Check bond verification status", description = "Check if a bond has all required verified documents")
    public ResponseEntity<Map<String, Object>> isBondFullyVerified(
            @Parameter(description = "Bond ID") @PathVariable String bondId) {

        log.debug("REST API: Checking if bond is fully verified: {}", bondId);

        boolean isFullyVerified = verificationService.isBondFullyVerified(bondId);

        return ResponseEntity.ok(Map.of(
                "bondId", bondId,
                "isFullyVerified", isFullyVerified,
                "timestamp", System.currentTimeMillis()
        ));
    }
}