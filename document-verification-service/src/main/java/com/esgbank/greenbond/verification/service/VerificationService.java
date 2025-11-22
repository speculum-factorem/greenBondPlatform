package com.esgbank.greenbond.verification.service;

import com.esgbank.greenbond.verification.dto.VerificationRequest;
import com.esgbank.greenbond.verification.exception.DocumentNotFoundException;
import com.esgbank.greenbond.verification.exception.DocumentProcessingException;
import com.esgbank.greenbond.verification.model.Document;
import com.esgbank.greenbond.verification.model.VerificationStep;
import com.esgbank.greenbond.verification.model.enums.DocumentStatus;
import com.esgbank.greenbond.verification.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final DocumentRepository documentRepository;
    private final AuditService auditService;
    private final BlockchainService blockchainService;

    public Document verifyDocument(String documentId, VerificationRequest request,
                                   String verifierId, String verifierName) {
        String requestId = MDC.get("requestId");
        log.info("Verifying document: {}, verifier: {}, requestId: {}",
                documentId, verifierId, requestId);

        Document document = documentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));

        // Validate document for verification
        validateForVerification(document);

        try {
            // Update document status
            DocumentStatus newStatus = Boolean.TRUE.equals(request.getIsApproved()) ?
                    DocumentStatus.VERIFIED : DocumentStatus.REJECTED;

            document.setStatus(newStatus);
            document.setVerifierId(verifierId);
            document.setVerifierName(verifierName);
            document.setVerifiedAt(LocalDateTime.now());
            document.setVerificationComment(request.getComment());

            // Add verification step
            VerificationStep verificationStep = VerificationStep.builder()
                    .stepName("MANUAL_VERIFICATION")
                    .status("COMPLETED")
                    .performedBy(verifierName)
                    .performedAt(LocalDateTime.now())
                    .comments(request.getComment())
                    .details(Map.of(
                            "approved", request.getIsApproved(),
                            "verifierId", verifierId
                    ))
                    .build();

            if (document.getVerificationSteps() != null) {
                document.getVerificationSteps().add(verificationStep);
            }

            Document verifiedDocument = documentRepository.save(document);

            // Record verification on blockchain if approved
            if (Boolean.TRUE.equals(request.getIsApproved())) {
                blockchainService.recordDocumentVerification(verifiedDocument);
            }

            // Audit trail
            String action = Boolean.TRUE.equals(request.getIsApproved()) ? "VERIFICATION_APPROVED" : "VERIFICATION_REJECTED";
            auditService.logDocumentAction(documentId, action, verifierId, request.getComment());

            log.info("Document verification completed: {}, status: {}", documentId, newStatus);

            return verifiedDocument;

        } catch (Exception e) {
            log.error("Document verification failed: {}. Error: {}", documentId, e.getMessage(), e);
            throw new DocumentProcessingException("Document verification failed: " + e.getMessage(), e);
        }
    }

    public Document requestReview(String documentId, String comment, String requesterId) {
        log.info("Requesting review for document: {}, requester: {}", documentId, requesterId);

        Document document = documentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));

        document.setStatus(DocumentStatus.UNDER_REVIEW);

        // Add review request step
        VerificationStep reviewStep = VerificationStep.builder()
                .stepName("REVIEW_REQUESTED")
                .status("PENDING")
                .performedBy(requesterId)
                .performedAt(LocalDateTime.now())
                .comments(comment)
                .details(Map.of("reviewRequested", true))
                .build();

        if (document.getVerificationSteps() != null) {
            document.getVerificationSteps().add(reviewStep);
        }

        Document updatedDocument = documentRepository.save(document);

        // Audit trail
        auditService.logDocumentAction(documentId, "REVIEW_REQUESTED", requesterId, comment);

        log.info("Review requested for document: {}", documentId);

        return updatedDocument;
    }

    public Document updateFieldVerification(String documentId, String fieldName,
                                            Boolean isVerified, String verifierId) {
        log.debug("Updating field verification: {}, field: {}, verifier: {}",
                documentId, fieldName, verifierId);

        Document document = documentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found: " + documentId));

        if (document.getExtractedFields() != null) {
            document.getExtractedFields().stream()
                    .filter(field -> fieldName.equals(field.getFieldName()))
                    .findFirst()
                    .ifPresent(field -> {
                        field.setIsVerified(isVerified);
                        field.setVerificationSource("MANUAL_VERIFICATION");
                    });
        }

        Document updatedDocument = documentRepository.save(document);

        // Audit trail
        auditService.logDocumentAction(documentId, "FIELD_VERIFICATION_UPDATED",
                verifierId, "Field '" + fieldName + "' verification updated to: " + isVerified);

        log.debug("Field verification updated: {}, field: {}", documentId, fieldName);

        return updatedDocument;
    }

    private void validateForVerification(Document document) {
        if (document.getStatus() != DocumentStatus.EXTRACTION_COMPLETED &&
                document.getStatus() != DocumentStatus.UNDER_REVIEW) {
            throw new DocumentProcessingException(
                    "Document must be in EXTRACTION_COMPLETED or UNDER_REVIEW status for verification. " +
                            "Current status: " + document.getStatus());
        }

        if (document.getExtractedFields() == null || document.getExtractedFields().isEmpty()) {
            throw new DocumentProcessingException("No extracted fields found for verification");
        }
    }

    public boolean isBondFullyVerified(String bondId) {
        log.debug("Checking if bond is fully verified: {}", bondId);

        // Define required document types for bond verification
        var requiredTypes = List.of(
                com.esgbank.greenbond.verification.model.enums.DocumentType.ESG_REPORT,
                com.esgbank.greenbond.verification.model.enums.DocumentType.FINANCIAL_STATEMENT,
                com.esgbank.greenbond.verification.model.enums.DocumentType.PROJECT_PROPOSAL
        );

        for (var docType : requiredTypes) {
            boolean hasVerifiedDoc = documentRepository.existsByBondIdAndDocumentTypeAndStatus(
                    bondId, docType, DocumentStatus.VERIFIED);

            if (!hasVerifiedDoc) {
                log.debug("Bond {} missing verified document of type: {}", bondId, docType);
                return false;
            }
        }

        log.debug("Bond {} is fully verified", bondId);
        return true;
    }
}