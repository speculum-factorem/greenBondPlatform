package com.esgbank.greenbond.blockchain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Fund usage verification result")
public class FundVerificationResult {

    @Schema(description = "Verification hash")
    private String verificationHash;

    @Schema(description = "Verification status")
    private boolean verified;

    @Schema(description = "Verification message")
    private String message;

    @Schema(description = "Block number")
    private Long blockNumber;
}