package com.esgbank.greenbond.blockchain.controller;

import com.esgbank.greenbond.blockchain.model.BlockchainTransactionResult;
import com.esgbank.greenbond.blockchain.model.BondInfo;
import com.esgbank.greenbond.blockchain.model.FundVerificationResult;
import com.esgbank.greenbond.blockchain.model.ImpactMetric;
import com.esgbank.greenbond.blockchain.service.BlockchainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/blockchain")
@RequiredArgsConstructor
@Tag(name = "Blockchain Integration", description = "Blockchain operations for Green Bonds")
public class BlockchainController {

    private final BlockchainService blockchainService;

    @PostMapping("/bonds/{bondId}/tokenize")
    @Operation(summary = "Tokenize a bond", description = "Create a tokenized representation of a green bond on blockchain")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bond successfully tokenized"),
            @ApiResponse(responseCode = "400", description = "Invalid bond data"),
            @ApiResponse(responseCode = "500", description = "Blockchain operation failed")
    })
    public ResponseEntity<BlockchainTransactionResult> tokenizeBond(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "Bond tokenization request") @Valid @RequestBody TokenizeBondRequest request) {

        log.info("REST API: Tokenizing bond: {}, requestId: {}", bondId, MDC.get("requestId"));

        BlockchainTransactionResult result = blockchainService.tokenizeBond(
                bondId,
                request.getTotalSupply(),
                request.getFaceValue(),
                request.getCouponRate(),
                request.getMaturityDate(),
                request.getProjectWallet(),
                request.getVerifierReportHash(),
                request.getIssuerWallet()
        );

        log.info("Bond tokenization completed via REST for bond: {}, txHash: {}",
                bondId, result.getTransactionHash());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/bonds/{bondId}")
    @Operation(summary = "Get bond information", description = "Retrieve bond information from blockchain")
    public ResponseEntity<BondInfo> getBondInfo(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "Transaction hash") @RequestParam(required = false) String transactionHash) {

        log.debug("REST API: Getting bond info for bond: {}", bondId);

        BondInfo bondInfo = blockchainService.getBondStatus(bondId, transactionHash);
        return ResponseEntity.ok(bondInfo);
    }

    @PostMapping("/bonds/{bondId}/impact")
    @Operation(summary = "Record impact data", description = "Record environmental impact data on blockchain")
    public ResponseEntity<BlockchainTransactionResult> recordImpactData(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "Impact data") @Valid @RequestBody ImpactMetric impactMetric) {

        log.info("REST API: Recording impact data for bond: {}, metric: {}",
                bondId, impactMetric.getMetricType());

        BlockchainTransactionResult result = blockchainService.recordImpactData(
                bondId,
                impactMetric.getMetricType(),
                impactMetric.getValue(),
                impactMetric.getUnit(),
                impactMetric.getTimestamp().toEpochSecond(java.time.ZoneOffset.UTC),
                impactMetric.getSource(),
                impactMetric.getDataHash()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/bonds/{bondId}/verify-funds")
    @Operation(summary = "Verify fund usage", description = "Verify that bond funds are used for intended purposes")
    public ResponseEntity<FundVerificationResult> verifyFundUsage(
            @Parameter(description = "Bond ID") @PathVariable String bondId,
            @Parameter(description = "Fund usage verification request") @Valid @RequestBody VerifyFundUsageRequest request) {

        log.info("REST API: Verifying fund usage for bond: {}, transaction: {}",
                bondId, request.getTransactionHash());

        FundVerificationResult result = blockchainService.verifyFundUsage(
                bondId,
                request.getTransactionHash(),
                request.getAmount(),
                request.getRecipient(),
                request.getPurpose(),
                request.getDocumentHash()
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/network/info")
    @Operation(summary = "Get network information", description = "Get blockchain network information")
    public ResponseEntity<Map<String, Object>> getNetworkInfo() {
        log.debug("REST API: Getting network info");

        String networkId = blockchainService.getNetworkInfo();

        return ResponseEntity.ok(Map.of(
                "networkId", networkId,
                "service", "blockchain-integration",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check blockchain connection health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("REST API: Health check");

        boolean isConnected = true; // This would be actual connection check

        return ResponseEntity.ok(Map.of(
                "status", isConnected ? "UP" : "DOWN",
                "blockchain", isConnected ? "CONNECTED" : "DISCONNECTED",
                "timestamp", System.currentTimeMillis()
        ));
    }
}