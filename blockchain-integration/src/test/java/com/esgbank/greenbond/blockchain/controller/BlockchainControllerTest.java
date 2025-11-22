package com.esgbank.greenbond.blockchain.controller;

import com.esgbank.greenbond.blockchain.model.BlockchainTransactionResult;
import com.esgbank.greenbond.blockchain.model.BondInfo;
import com.esgbank.greenbond.blockchain.model.FundVerificationResult;
import com.esgbank.greenbond.blockchain.model.ImpactMetric;
import com.esgbank.greenbond.blockchain.service.BlockchainService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlockchainController.class)
class BlockchainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BlockchainService blockchainService;

    @Test
    void shouldTokenizeBondSuccessfully() throws Exception {
        // Given
        String bondId = "bond-123";
        var request = new TokenizeBondRequest();
        request.setTotalSupply("1000000");
        request.setFaceValue("1000");
        request.setCouponRate("5.5");
        request.setMaturityDate("2028-12-31");
        request.setProjectWallet("0xProjectWallet");
        request.setVerifierReportHash("verifierHash123");
        request.setIssuerWallet("0xIssuerWallet");

        var result = BlockchainTransactionResult.builder()
                .transactionHash("0xTxHash123")
                .contractAddress("0xContract123")
                .status("SUCCESS")
                .build();

        when(blockchainService.tokenizeBond(
                eq(bondId), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/blockchain/bonds/{bondId}/tokenize", bondId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionHash").value("0xTxHash123"))
                .andExpect(jsonPath("$.contractAddress").value("0xContract123"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void shouldGetBondInfo() throws Exception {
        // Given
        String bondId = "bond-123";
        var bondInfo = BondInfo.builder()
                .bondId(bondId)
                .bondAddress("0xContract123")
                .status("ACTIVE")
                .build();

        when(blockchainService.getBondStatus(eq(bondId), any())).thenReturn(bondInfo);

        // When & Then
        mockMvc.perform(get("/api/v1/blockchain/bonds/{bondId}", bondId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bondId").value(bondId))
                .andExpect(jsonPath("$.bondAddress").value("0xContract123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldRecordImpactData() throws Exception {
        // Given
        String bondId = "bond-123";
        var impactMetric = ImpactMetric.builder()
                .metricType("CO2_REDUCTION")
                .value(150.5)
                .unit("tons")
                .timestamp(LocalDateTime.now())
                .source("IoT_Sensor")
                .dataHash("dataHash123")
                .build();

        var result = BlockchainTransactionResult.builder()
                .transactionHash("0xImpactTxHash")
                .status("SUCCESS")
                .build();

        when(blockchainService.recordImpactData(
                eq(bondId), any(), any(), any(), any(), any(), any()
        )).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/blockchain/bonds/{bondId}/impact", bondId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(impactMetric)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionHash").value("0xImpactTxHash"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void shouldVerifyFundUsage() throws Exception {
        // Given
        String bondId = "bond-123";
        var request = new VerifyFundUsageRequest();
        request.setTransactionHash("0xTxHash");
        request.setAmount("50000");
        request.setRecipient("0xRecipient");
        request.setPurpose("Solar Panels");
        request.setDocumentHash("docHash123");

        var result = FundVerificationResult.builder()
                .verificationHash("verifyHash123")
                .verified(true)
                .message("Funds verified successfully")
                .build();

        when(blockchainService.verifyFundUsage(
                eq(bondId), any(), any(), any(), any(), any()
        )).thenReturn(result);

        // When & Then
        mockMvc.perform(post("/api/v1/blockchain/bonds/{bondId}/verify-funds", bondId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationHash").value("verifyHash123"))
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.message").value("Funds verified successfully"));
    }

    @Test
    void shouldGetNetworkInfo() throws Exception {
        // Given
        when(blockchainService.getNetworkInfo()).thenReturn("1337");

        // When & Then
        mockMvc.perform(get("/api/v1/blockchain/network/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.networkId").value("1337"))
                .andExpect(jsonPath("$.service").value("blockchain-integration"));
    }

    @Test
    void shouldHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/blockchain/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.blockchain").exists());
    }
}