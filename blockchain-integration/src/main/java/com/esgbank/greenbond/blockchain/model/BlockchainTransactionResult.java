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
@Schema(description = "Blockchain transaction result")
public class BlockchainTransactionResult {

    @Schema(description = "Transaction hash")
    private String transactionHash;

    @Schema(description = "Contract address (if deployed)")
    private String contractAddress;

    @Schema(description = "Block number")
    private Long blockNumber;

    @Schema(description = "Transaction status")
    private String status;

    @Schema(description = "Gas used")
    private String gasUsed;

    @Schema(description = "Error message (if any)")
    private String errorMessage;
}