package com.esgbank.greenbond.issuance.controller;

import com.esgbank.greenbond.issuance.dto.BondAllocationRequest;
import com.esgbank.greenbond.issuance.dto.BondAllocationResponse;
import com.esgbank.greenbond.issuance.service.BondAllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/bonds/{bondId}/allocations")
@RequiredArgsConstructor
@Tag(name = "Bond Allocation", description = "APIs for managing bond allocations to investors")
public class BondAllocationController {

    private final BondAllocationService allocationService;

    @PostMapping
    @Operation(summary = "Allocate bonds to investor", description = "Allocate bonds from a specific issuance to an investor")
    public ResponseEntity<BondAllocationResponse> allocateBonds(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "Allocation request") @Valid @RequestBody BondAllocationRequest request) {

        log.info("REST API: Allocating bonds for bond: {}, investor: {}", bondId, request.getInvestorId());

        BondAllocationResponse response = allocationService.allocateBonds(bondId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get bond allocations", description = "Get paginated list of allocations for a bond")
    public ResponseEntity<Page<BondAllocationResponse>> getAllocationsByBond(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.debug("REST API: Getting allocations for bond: {}, page: {}", bondId, pageable.getPageNumber());

        Page<BondAllocationResponse> allocations = allocationService.getAllocationsByBond(bondId, pageable);
        return ResponseEntity.ok(allocations);
    }

    @GetMapping("/investor/{investorId}")
    @Operation(summary = "Get investor allocations", description = "Get paginated list of allocations for an investor")
    public ResponseEntity<Page<BondAllocationResponse>> getAllocationsByInvestor(
            @Parameter(description = "Investor ID") @PathVariable String investorId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.debug("REST API: Getting allocations for investor: {}, page: {}", investorId, pageable.getPageNumber());

        Page<BondAllocationResponse> allocations = allocationService.getAllocationsByInvestor(investorId, pageable);
        return ResponseEntity.ok(allocations);
    }

    @PostMapping("/{allocationId}/settle")
    @Operation(summary = "Settle allocation", description = "Mark a bond allocation as settled")
    public ResponseEntity<BondAllocationResponse> settleAllocation(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "Allocation ID") @PathVariable String allocationId) {

        log.info("REST API: Settling allocation: {}, bond: {}", allocationId, bondId);

        BondAllocationResponse response = allocationService.settleAllocation(allocationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/supply/remaining")
    @Operation(summary = "Get remaining supply", description = "Get remaining bond supply available for allocation")
    public ResponseEntity<Map<String, Object>> getRemainingSupply(
            @Parameter(description = "Bond ID") @PathVariable String bondId) {

        log.debug("REST API: Getting remaining supply for bond: {}", bondId);

        BigDecimal remainingSupply = allocationService.getRemainingSupply(bondId);
        BigDecimal totalAllocated = allocationService.getTotalAllocatedAmount(bondId);

        return ResponseEntity.ok(Map.of(
                "bondId", bondId,
                "remainingSupply", remainingSupply,
                "totalAllocated", totalAllocated,
                "timestamp", System.currentTimeMillis()
        ));
    }
}