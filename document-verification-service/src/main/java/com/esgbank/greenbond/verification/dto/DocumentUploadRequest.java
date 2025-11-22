package com.esgbank.greenbond.verification.dto;

import com.esgbank.greenbond.verification.model.enums.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Request for uploading a document")
public class DocumentUploadRequest {

    @NotBlank
    @Schema(description = "Bond ID associated with the document")
    private String bondId;

    @NotBlank
    @Schema(description = "Document name")
    private String documentName;

    @NotNull
    @Schema(description = "Document type")
    private DocumentType documentType;

    @Schema(description = "Additional metadata")
    private String metadata;
}