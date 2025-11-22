package com.esgbank.greenbond.issuance.controller;

import com.esgbank.greenbond.issuance.dto.BondCreationRequest;
import com.esgbank.greenbond.issuance.dto.BondResponse;
import com.esgbank.greenbond.issuance.model.enums.BondStatus;
import com.esgbank.greenbond.issuance.model.enums.BondType;
import com.esgbank.greenbond.issuance.service.BondService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BondController.class)
class BondControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BondService bondService;

    @Test
    void shouldCreateBondSuccessfully() throws Exception {
        // Given
        BondCreationRequest request = createBondRequest();
        BondResponse response = createBondResponse();

        when(bondService.createBond(any(), anyString(), anyString())).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/bonds")
                        .header("X-User-Id", "issuer-123")
                        .header("X-User-Name", "Solar Company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bondId").value("BOND-123"))
                .andExpect(jsonPath("$.projectName").value("Solar Project"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void shouldGetBondSuccessfully() throws Exception {
        // Given
        BondResponse response = createBondResponse();
        when(bondService.getBond("BOND-123")).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/bonds/BOND-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bondId").value("BOND-123"))
                .andExpect(jsonPath("$.projectName").value("Solar Project"));
    }

    @Test
    void shouldGetBondsByIssuer() throws Exception {
        // Given
        BondResponse response = createBondResponse();
        Page<BondResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(bondService.getBondsByIssuer(eq("issuer-123"), any())).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/bonds")
                        .header("X-User-Id", "issuer-123")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].bondId").value("BOND-123"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldTokenizeBondSuccessfully() throws Exception {
        // Given
        BondResponse response = createBondResponse();
        response.setStatus(BondStatus.TOKENIZED);
        response.setBlockchainTxHash("tx-hash-123");

        when(bondService.tokenizeBond("BOND-123", "issuer-123")).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/bonds/BOND-123/tokenize")
                        .header("X-User-Id", "issuer-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TOKENIZED"))
                .andExpect(jsonPath("$.blockchainTxHash").value("tx-hash-123"));
    }

    @Test
    void shouldUpdateBondStatus() throws Exception {
        // Given
        BondResponse response = createBondResponse();
        response.setStatus(BondStatus.VERIFIED);

        when(bondService.updateBondStatus("BOND-123", BondStatus.VERIFIED, "issuer-123"))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/api/v1/bonds/BOND-123/status")
                        .header("X-User-Id", "issuer-123")
                        .param("status", "VERIFIED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    private BondCreationRequest createBondRequest() {
        BondCreationRequest request = new BondCreationRequest();
        request.setProjectName("Solar Project");
        request.setProjectDescription("50MW Solar Plant");
        request.setBondType(BondType.SOLAR_ENERGY);
        request.setTotalSupply(new BigDecimal("1000000.0000"));
        request.setFaceValue(new BigDecimal("1000.0000"));
        request.setCouponRate(new BigDecimal("5.5000"));
        request.setMaturityDate(LocalDate.now().plusYears(5));
        request.setProjectWalletAddress("0xProjectWallet123");
        request.setEsgStandard("ICMA_GBP");
        request.setUseOfProceeds("Construction of solar power plant");
        return request;
    }

    private BondResponse createBondResponse() {
        return BondResponse.builder()
                .id("bond-uuid")
                .bondId("BOND-123")
                .projectName("Solar Project")
                .projectDescription("50MW Solar Plant")
                .bondType(BondType.SOLAR_ENERGY)
                .status(BondStatus.DRAFT)
                .totalSupply(new BigDecimal("1000000.0000"))
                .faceValue(new BigDecimal("1000.0000"))
                .couponRate(new BigDecimal("5.5000"))
                .maturityDate(LocalDate.now().plusYears(5))
                .issueDate(LocalDate.now())
                .issuerId("issuer-123")
                .issuerName("Solar Company Inc.")
                .projectWalletAddress("0xProjectWallet123")
                .esgStandard("ICMA_GBP")
                .useOfProceeds("Construction of solar power plant")
                .build();
    }
}