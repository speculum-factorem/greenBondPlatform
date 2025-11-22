package com.esgbank.greenbond.issuance.service;

import com.esgbank.greenbond.issuance.dto.BondAllocationRequest;
import com.esgbank.greenbond.issuance.dto.BondAllocationResponse;
import com.esgbank.greenbond.issuance.exception.BondNotFoundException;
import com.esgbank.greenbond.issuance.exception.BondIssuanceException;
import com.esgbank.greenbond.issuance.mapper.BondAllocationMapper;
import com.esgbank.greenbond.issuance.model.Bond;
import com.esgbank.greenbond.issuance.model.BondAllocation;
import com.esgbank.greenbond.issuance.model.enums.BondStatus;
import com.esgbank.greenbond.issuance.repository.BondAllocationRepository;
import com.esgbank.greenbond.issuance.repository.BondRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class BondAllocationService {

    private final BondAllocationRepository allocationRepository;
    private final BondRepository bondRepository;
    private final BondAllocationMapper allocationMapper;
    private final BondValidationService bondValidationService;

    @Transactional
    public BondAllocationResponse allocateBonds(String bondId, BondAllocationRequest request) {
        String requestId = MDC.get("requestId");
        log.info("Allocating bonds: {}, investor: {}, amount: {}, requestId: {}",
                bondId, request.getInvestorId(), request.getAllocationAmount(), requestId);

        Bond bond = bondRepository.findByBondId(bondId)
                .orElseThrow(() -> new BondNotFoundException("Bond not found: " + bondId));

        // Validate bond for allocation
        bondValidationService.validateForAllocation(bond);

        // Check if allocation already exists
        allocationRepository.findByBondIdAndInvestorId(bondId, request.getInvestorId())
                .ifPresent(allocation -> {
                    throw new BondIssuanceException("Allocation already exists for investor: " + request.getInvestorId());
                });

        // Calculate units
        BigDecimal units = request.getAllocationAmount()
                .divide(bond.getFaceValue(), 4, RoundingMode.HALF_UP);

        // Check available supply
        BigDecimal totalAllocated = allocationRepository.sumAllocatedAmountByBondId(bond.getId());
        if (totalAllocated == null) {
            totalAllocated = BigDecimal.ZERO;
        }

        BigDecimal remainingSupply = bond.getTotalSupply().subtract(totalAllocated);
        if (request.getAllocationAmount().compareTo(remainingSupply) > 0) {
            throw new BondIssuanceException("Insufficient bond supply. Requested: " +
                    request.getAllocationAmount() + ", Available: " + remainingSupply);
        }

        try {
            // Create allocation
            BondAllocation allocation = BondAllocation.builder()
                    .bond(bond)
                    .investorId(request.getInvestorId())
                    .investorName(request.getInvestorName())
                    .allocatedAmount(request.getAllocationAmount())
                    .allocatedUnits(units)
                    .allocationStatus("ALLOCATED")
                    .build();

            BondAllocation savedAllocation = allocationRepository.save(allocation);
            log.info("Bond allocation created successfully: {}, allocationId: {}",
                    bondId, savedAllocation.getId());

            return allocationMapper.toResponse(savedAllocation);

        } catch (Exception e) {
            log.error("Bond allocation failed for bond: {}, investor: {}. Error: {}",
                    bondId, request.getInvestorId(), e.getMessage(), e);
            throw new BondIssuanceException("Bond allocation failed: " + e.getMessage(), e);
        }
    }

    public Page<BondAllocationResponse> getAllocationsByBond(String bondId, Pageable pageable) {
        log.debug("Fetching allocations for bond: {}, page: {}", bondId, pageable.getPageNumber());

        Page<BondAllocation> allocations = allocationRepository.findByBondId(bondId, pageable);
        return allocations.map(allocationMapper::toResponse);
    }

    public Page<BondAllocationResponse> getAllocationsByInvestor(String investorId, Pageable pageable) {
        log.debug("Fetching allocations for investor: {}, page: {}", investorId, pageable.getPageNumber());

        Page<BondAllocation> allocations = allocationRepository.findByInvestorId(investorId, pageable);
        return allocations.map(allocationMapper::toResponse);
    }

    @Transactional
    public BondAllocationResponse settleAllocation(String allocationId) {
        log.info("Settling bond allocation: {}", allocationId);

        BondAllocation allocation = allocationRepository.findById(allocationId)
                .orElseThrow(() -> new BondNotFoundException("Allocation not found: " + allocationId));

        if ("SETTLED".equals(allocation.getAllocationStatus())) {
            throw new BondIssuanceException("Allocation already settled: " + allocationId);
        }

        allocation.setAllocationStatus("SETTLED");
        allocation.setSettledAt(java.time.LocalDateTime.now());

        BondAllocation updatedAllocation = allocationRepository.save(allocation);
        log.info("Bond allocation settled successfully: {}", allocationId);

        return allocationMapper.toResponse(updatedAllocation);
    }

    public BigDecimal getTotalAllocatedAmount(String bondId) {
        BigDecimal totalAllocated = allocationRepository.sumAllocatedAmountByBondId(bondId);
        return totalAllocated != null ? totalAllocated : BigDecimal.ZERO;
    }

    public BigDecimal getRemainingSupply(String bondId) {
        Bond bond = bondRepository.findByBondId(bondId)
                .orElseThrow(() -> new BondNotFoundException("Bond not found: " + bondId));

        BigDecimal totalAllocated = getTotalAllocatedAmount(bondId);
        return bond.getTotalSupply().subtract(totalAllocated);
    }
}