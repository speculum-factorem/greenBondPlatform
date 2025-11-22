package com.esgbank.greenbond.issuance.repository;

import com.esgbank.greenbond.issuance.model.Bond;
import com.esgbank.greenbond.issuance.model.enums.BondStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BondRepository extends JpaRepository<Bond, String> {

    Optional<Bond> findByBondId(String bondId);

    Page<Bond> findByIssuerId(String issuerId, Pageable pageable);

    Page<Bond> findByStatus(BondStatus status, Pageable pageable);

    List<Bond> findByMaturityDateBeforeAndStatus(LocalDate maturityDate, BondStatus status);

    @Query("SELECT b FROM Bond b WHERE b.issuerId = :issuerId AND b.status IN :statuses")
    Page<Bond> findByIssuerIdAndStatusIn(@Param("issuerId") String issuerId,
                                         @Param("statuses") List<BondStatus> statuses,
                                         Pageable pageable);

    @Query("SELECT COUNT(b) FROM Bond b WHERE b.issuerId = :issuerId AND b.status = :status")
    long countByIssuerIdAndStatus(@Param("issuerId") String issuerId,
                                  @Param("status") BondStatus status);

    boolean existsByBondId(String bondId);
}