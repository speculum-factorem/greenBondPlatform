package com.esgbank.greenbond.blockchain.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Fund usage verification request")
public class VerifyFundUsageRequest {

    @NotBlank
    @Schema(description = "Transaction hash")
    private String transactionHash;

    @NotBlank
    @Schema(description = "Amount", example = "50000")
    private String amount;

    @NotBlank
    @Schema(description = "Recipient address")
    private String recipient;

    @NotBlank
    @Schema(description = "Purpose of funds")
    private String purpose;

    @NotBlank
    @Schema(description = "Document hash for verification")
    private String documentHash;
}