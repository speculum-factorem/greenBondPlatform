package com.esgbank.greenbond.verification.model;

import com.esgbank.greenbond.verification.model.enums.DocumentStatus;
import com.esgbank.greenbond.verification.model.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
public class Document {

    @Id
    private String id;

    @Indexed
    private String documentId;

    @Indexed
    private String bondId;

    @Indexed
    private String issuerId;

    private String issuerName;

    private String documentName;

    private String originalFileName;

    private DocumentType documentType;

    private DocumentStatus status;

    private String filePath;

    private String fileHash;

    private String mimeType;

    private Long fileSize;

    private String uploadedBy;

    private String verifierId;

    private String verifierName;

    private LocalDateTime verifiedAt;

    private String verificationComment;

    private Map<String, Object> metadata;

    private List<DocumentField> extractedFields;

    private List<VerificationStep> verificationSteps;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}