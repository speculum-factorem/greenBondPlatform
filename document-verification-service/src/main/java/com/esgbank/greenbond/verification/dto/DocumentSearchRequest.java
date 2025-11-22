package com.esgbank.greenbond.verification.dto;

import com.esgbank.greenbond.verification.model.enums.DocumentStatus;
import com.esgbank.greenbond.verification.model.enums.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Request for searching documents")
public class DocumentSearchRequest {

    @Schema(description = "Bond ID")
    private String bondId;

    @Schema(description = "Issuer ID")
    private String issuerId;

    @Schema(description = "Document type")
    private DocumentType documentType;

    @Schema(description = "Document status")
    private DocumentStatus status;

    @Schema(description = "Uploaded by")
    private String uploadedBy;

    @Schema(description = "Verifier ID")
    private String verifierId;

    @Schema(description = "Date from")
    private LocalDateTime dateFrom;

    @Schema(description = "Date to")
    private LocalDateTime dateTo;

    @Schema(description = "Search term")
    private String searchTerm;
}