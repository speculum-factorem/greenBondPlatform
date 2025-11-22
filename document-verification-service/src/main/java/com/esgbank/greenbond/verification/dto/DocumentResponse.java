package com.esgbank.greenbond.verification.dto;

import com.esgbank.greenbond.verification.model.enums.DocumentStatus;
import com.esgbank.greenbond.verification.model.enums.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Document response with full details")
public class DocumentResponse {

    @Schema(description = "Document ID")
    private String id;

    @Schema(description = "Unique document identifier")
    private String documentId;

    @Schema(description = "Bond ID")
    private String bondId;

    @Schema(description = "Issuer ID")
    private String issuerId;

    @Schema(description = "Issuer name")
    private String issuerName;

    @Schema(description = "Document name")
    private String documentName;

    @Schema(description = "Original file name")
    private String originalFileName;

    @Schema(description = "Document type")
    private DocumentType documentType;

    @Schema(description = "Document status")
    private DocumentStatus status;

    @Schema(description = "File path")
    private String filePath;

    @Schema(description = "File hash")
    private String fileHash;

    @Schema(description = "MIME type")
    private String mimeType;

    @Schema(description = "File size in bytes")
    private Long fileSize;

    @Schema(description = "Uploaded by user")
    private String uploadedBy;

    @Schema(description = "Verifier ID")
    private String verifierId;

    @Schema(description = "Verifier name")
    private String verifierName;

    @Schema(description = "Verification timestamp")
    private LocalDateTime verifiedAt;

    @Schema(description = "Verification comment")
    private String verificationComment;

    @Schema(description = "Metadata")
    private Map<String, Object> metadata;

    @Schema(description = "Extracted fields")
    private List<DocumentFieldResponse> extractedFields;

    @Schema(description = "Verification steps")
    private List<VerificationStepResponse> verificationSteps;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}