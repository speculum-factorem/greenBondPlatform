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
@Schema(description = "Bond information from blockchain")
public class BondInfo {

    @Schema(description = "Bond ID")
    private String bondId;

    @Schema(description = "Bond contract address")
    private String bondAddress;

    @Schema(description = "Bond status")
    private String status;

    @Schema(description = "Transaction hash")
    private String transactionHash;

    @Schema(description = "Block number")
    private Long blockNumber;

    @Schema(description = "Bond owner")
    private String owner;

    @Schema(description = "Total supply")
    private String totalSupply;
}