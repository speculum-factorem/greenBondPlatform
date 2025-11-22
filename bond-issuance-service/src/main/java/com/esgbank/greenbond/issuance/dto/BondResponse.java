package com.esgbank.greenbond.issuance.dto;

import com.esgbank.greenbond.issuance.model.enums.BondStatus;
import com.esgbank.greenbond.issuance.model.enums.BondType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bond response with full details")
public class BondResponse {

    @Schema(description = "Bond ID")
    private String id;

    @Schema(description = "Unique bond identifier")
    private String bondId;

    @Schema(description = "Project name")
    private String projectName;

    @Schema(description = "Project description")
    private String projectDescription;

    @Schema(description = "Bond type")
    private BondType bondType;

    @Schema(description = "Bond status")
    private BondStatus status;

    @Schema(description = "Total supply amount")
    private BigDecimal totalSupply;

    @Schema(description = "Face value per unit")
    private BigDecimal faceValue;

    @Schema(description = "Annual coupon rate")
    private BigDecimal couponRate;

    @Schema(description = "Maturity date")
    private LocalDate maturityDate;

    @Schema(description = "Issue date")
    private LocalDate issueDate;

    @Schema(description = "Issuer ID")
    private String issuerId;

    @Schema(description = "Issuer name")
    private String issuerName;

    @Schema(description = "Project wallet address")
    private String projectWalletAddress;

    @Schema(description = "Blockchain transaction hash")
    private String blockchainTxHash;

    @Schema(description = "Bond contract address")
    private String bondContractAddress;

    @Schema(description = "ESG standard")
    private String esgStandard;

    @Schema(description = "Greenium details")
    private String greeniumDetails;

    @Schema(description = "Use of proceeds")
    private String useOfProceeds;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}