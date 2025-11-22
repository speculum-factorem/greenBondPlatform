package com.esgbank.greenbond.issuance.service;

import com.esgbank.greenbond.issuance.dto.BondCreationRequest;
import com.esgbank.greenbond.issuance.exception.BondIssuanceException;
import com.esgbank.greenbond.issuance.exception.BondNotFoundException;
import com.esgbank.greenbond.issuance.integration.BlockchainService;
import com.esgbank.greenbond.issuance.mapper.BondMapper;
import com.esgbank.greenbond.issuance.mapper.BondMapperImpl;
import com.esgbank.greenbond.issuance.model.Bond;
import com.esgbank.greenbond.issuance.model.enums.BondStatus;
import com.esgbank.greenbond.issuance.model.enums.BondType;
import com.esgbank.greenbond.issuance.repository.BondRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BondServiceTest {

    @Mock
    private BondRepository bondRepository;

    @Mock
    private BlockchainService blockchainService;

    @Mock
    private BondValidationService bondValidationService;

    private BondMapper bondMapper = new BondMapperImpl();

    private BondService bondService;

    @BeforeEach
    void setUp() {
        bondService = new BondService(bondRepository, bondMapper, blockchainService, bondValidationService);
    }

    @Test
    void shouldCreateBondSuccessfully() {
        // Given
        BondCreationRequest request = createBondRequest();
        String issuerId = "issuer-123";
        String issuerName = "Solar Company Inc.";

        Bond bond = createBondEntity();

        when(bondRepository.save(any(Bond.class))).thenReturn(bond);
        doNothing().when(bondValidationService).validateBondCreation(any());

        // When
        var result = bondService.createBond(request, issuerId, issuerName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProjectName()).isEqualTo("Solar Project");
        assertThat(result.getIssuerId()).isEqualTo(issuerId);
        assertThat(result.getStatus()).isEqualTo(BondStatus.DRAFT);

        verify(bondRepository).save(any(Bond.class));
        verify(bondValidationService).validateBondCreation(request);
    }

    @Test
    void shouldThrowExceptionWhenBondNotFound() {
        // Given
        String bondId = "non-existent-bond";
        when(bondRepository.findByBondId(bondId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bondService.getBond(bondId))
                .isInstanceOf(BondNotFoundException.class)
                .hasMessageContaining("Bond not found");
    }

    @Test
    void shouldGetBondsByIssuer() {
        // Given
        String issuerId = "issuer-123";
        Pageable pageable = PageRequest.of(0, 10);
        Bond bond = createBondEntity();
        Page<Bond> bondPage = new PageImpl<>(List.of(bond), pageable, 1);

        when(bondRepository.findByIssuerId(issuerId, pageable)).thenReturn(bondPage);

        // When
        Page<Bond> result = bondRepository.findByIssuerId(issuerId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getIssuerId()).isEqualTo(issuerId);
    }

    @Test
    void shouldTokenizeBondSuccessfully() {
        // Given
        String bondId = "BOND-123";
        String issuerId = "issuer-123";
        Bond bond = createBondEntity();
        bond.setStatus(BondStatus.VERIFIED);
        bond.setVerifierReportHash("verifier-hash");

        when(bondRepository.findByBondId(bondId)).thenReturn(Optional.of(bond));
        when(blockchainService.tokenizeBond(bond)).thenReturn("tx-hash-123");
        when(bondRepository.save(any(Bond.class))).thenReturn(bond);
        doNothing().when(bondValidationService).validateForTokenization(bond);

        // When
        var result = bondService.tokenizeBond(bondId, issuerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBlockchainTxHash()).isEqualTo("tx-hash-123");
        assertThat(result.getStatus()).isEqualTo(BondStatus.TOKENIZED);

        verify(blockchainService).tokenizeBond(bond);
        verify(bondRepository).save(bond);
    }

    @Test
    void shouldThrowExceptionWhenUnauthorizedTokenization() {
        // Given
        String bondId = "BOND-123";
        String unauthorizedIssuer = "unauthorized-issuer";
        Bond bond = createBondEntity();
        bond.setIssuerId("authorized-issuer");

        when(bondRepository.findByBondId(bondId)).thenReturn(Optional.of(bond));

        // When & Then
        assertThatThrownBy(() -> bondService.tokenizeBond(bondId, unauthorizedIssuer))
                .isInstanceOf(BondIssuanceException.class)
                .hasMessageContaining("Unauthorized access");
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

    private Bond createBondEntity() {
        return Bond.builder()
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