package com.esgbank.greenbond.issuance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Request for allocating bonds to an investor")
public class BondAllocationRequest {

    @NotBlank
    @Schema(description = "Investor ID")
    private String investorId;

    @NotBlank
    @Schema(description = "Investor name")
    private String investorName;

    @NotNull
    @Positive
    @Schema(description = "Allocation amount")
    private BigDecimal allocationAmount;
}