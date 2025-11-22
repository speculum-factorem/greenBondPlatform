package com.esgbank.greenbond.verification.repository;

import com.esgbank.greenbond.verification.model.Document;
import com.esgbank.greenbond.verification.model.enums.DocumentStatus;
import com.esgbank.greenbond.verification.model.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {

    Optional<Document> findByDocumentId(String documentId);

    Page<Document> findByBondId(String bondId, Pageable pageable);

    Page<Document> findByIssuerId(String issuerId, Pageable pageable);

    Page<Document> findByStatus(DocumentStatus status, Pageable pageable);

    Page<Document> findByDocumentType(DocumentType documentType, Pageable pageable);

    Page<Document> findByIssuerIdAndStatus(String issuerId, DocumentStatus status, Pageable pageable);

    List<Document> findByBondIdAndDocumentType(String bondId, DocumentType documentType);

    @Query("{ 'bondId': ?0, 'documentType': ?1, 'status': 'VERIFIED' }")
    List<Document> findVerifiedDocumentsByBondAndType(String bondId, DocumentType documentType);

    @Query("{ 'createdAt': { $lt: ?0 }, 'status': { $in: ?1 } }")
    List<Document> findExpiredDocuments(LocalDateTime expiryDate, List<DocumentStatus> statuses);

    @Query("{ 'bondId': ?0, 'status': 'VERIFIED' }")
    List<Document> findVerifiedDocumentsByBond(String bondId);

    long countByBondIdAndStatus(String bondId, DocumentStatus status);

    boolean existsByBondIdAndDocumentTypeAndStatus(String bondId, DocumentType documentType, DocumentStatus status);
}