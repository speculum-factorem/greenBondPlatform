package com.esgbank.greenbond.verification.repository;

import com.esgbank.greenbond.verification.model.AuditTrail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditTrailRepository extends MongoRepository<AuditTrail, String> {

    Page<AuditTrail> findByDocumentId(String documentId, Pageable pageable);

    Page<AuditTrail> findByBondId(String bondId, Pageable pageable);

    Page<AuditTrail> findByPerformedBy(String performedBy, Pageable pageable);
}