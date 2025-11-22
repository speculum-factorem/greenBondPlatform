package com.esgbank.greenbond.issuance.dto;

import com.esgbank.greenbond.issuance.model.enums.BondType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "Request for creating a new green bond")
public class BondCreationRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Project name", example = "Solar Power Plant 'Sunshine'")
    private String projectName;

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "Project description", example = "Construction of 50MW solar power plant with battery storage")
    private String projectDescription;

    @NotNull
    @Schema(description = "Type of green bond")
    private BondType bondType;

    @NotNull
    @Positive
    @Digits(integer = 15, fraction = 4)
    @Schema(description = "Total supply amount", example = "1000000.0000")
    private BigDecimal totalSupply;

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 4)
    @Schema(description = "Face value per unit", example = "1000.0000")
    private BigDecimal faceValue;

    @NotNull
    @DecimalMin("0.0")
    @Digits(integer = 4, fraction = 4)
    @Schema(description = "Annual coupon rate", example = "5.5000")
    private BigDecimal couponRate;

    @NotNull
    @Future
    @Schema(description = "Maturity date")
    private LocalDate maturityDate;

    @NotBlank
    @Schema(description = "Project wallet address for fund distribution")
    private String projectWalletAddress;

    @NotBlank
    @Schema(description = "ESG standard compliance", example = "ICMA_GBP")
    private String esgStandard;

    @Schema(description = "Greenium details", example = "25 basis points")
    private String greeniumDetails;

    @NotBlank
    @Size(max = 2000)
    @Schema(description = "Detailed use of proceeds")
    private String useOfProceeds;
}