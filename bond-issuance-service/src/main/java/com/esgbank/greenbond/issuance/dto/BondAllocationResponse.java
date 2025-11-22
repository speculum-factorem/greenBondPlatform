package com.esgbank.greenbond.issuance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bond allocation response")
public class BondAllocationResponse {

    @Schema(description = "Allocation ID")
    private String id;

    @Schema(description = "Bond ID")
    private String bondId;

    @Schema(description = "Investor ID")
    private String investorId;

    @Schema(description = "Investor name")
    private String investorName;

    @Schema(description = "Allocated amount")
    private BigDecimal allocatedAmount;

    @Schema(description = "Allocated units")
    private BigDecimal allocatedUnits;

    @Schema(description = "Allocation status")
    private String allocationStatus;

    @Schema(description = "Blockchain token ID")
    private String blockchainTokenId;

    @Schema(description = "Allocation timestamp")
    private LocalDateTime allocatedAt;

    @Schema(description = "Settlement timestamp")
    private LocalDateTime settledAt;
}