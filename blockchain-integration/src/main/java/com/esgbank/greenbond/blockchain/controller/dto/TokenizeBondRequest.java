package com.esgbank.greenbond.blockchain.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Bond tokenization request")
public class TokenizeBondRequest {

    @NotBlank
    @Schema(description = "Total supply", example = "1000000")
    private String totalSupply;

    @NotBlank
    @Schema(description = "Face value", example = "1000")
    private String faceValue;

    @NotBlank
    @Schema(description = "Coupon rate", example = "5.5")
    private String couponRate;

    @NotBlank
    @Schema(description = "Maturity date", example = "2028-12-31")
    private String maturityDate;

    @NotBlank
    @Schema(description = "Project wallet address")
    private String projectWallet;

    @NotBlank
    @Schema(description = "Verifier report hash")
    private String verifierReportHash;

    @NotBlank
    @Schema(description = "Issuer wallet address")
    private String issuerWallet;
}