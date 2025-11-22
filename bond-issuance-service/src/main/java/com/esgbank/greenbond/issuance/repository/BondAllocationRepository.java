package com.esgbank.greenbond.issuance.repository;

import com.esgbank.greenbond.issuance.model.BondAllocation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BondAllocationRepository extends JpaRepository<BondAllocation, String> {

    Page<BondAllocation> findByBondId(String bondId, Pageable pageable);

    Page<BondAllocation> findByInvestorId(String investorId, Pageable pageable);

    Optional<BondAllocation> findByBondIdAndInvestorId(String bondId, String investorId);

    @Query("SELECT SUM(ba.allocatedAmount) FROM BondAllocation ba WHERE ba.bond.id = :bondId")
    BigDecimal sumAllocatedAmountByBondId(@Param("bondId") String bondId);

    @Query("SELECT ba FROM BondAllocation ba WHERE ba.bond.id = :bondId AND ba.allocationStatus = :status")
    List<BondAllocation> findByBondIdAndAllocationStatus(@Param("bondId") String bondId,
                                                         @Param("status") String status);
}