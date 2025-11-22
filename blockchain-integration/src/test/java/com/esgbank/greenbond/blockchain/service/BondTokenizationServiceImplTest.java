package com.esgbank.greenbond.blockchain.service;

import com.esgbank.greenbond.blockchain.proto.*;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BondTokenizationServiceImplTest {

    @Mock
    private BlockchainService blockchainService;

    @Mock
    private StreamObserver<TokenizeBondResponse> tokenizeResponseObserver;

    @Mock
    private StreamObserver<BondStatusResponse> statusResponseObserver;

    @Mock
    private StreamObserver<ImpactDataResponse> impactResponseObserver;

    @Mock
    private StreamObserver<FundUsageResponse> fundUsageResponseObserver;

    @Captor
    private ArgumentCaptor<TokenizeBondResponse> tokenizeResponseCaptor;

    @Captor
    private ArgumentCaptor<BondStatusResponse> statusResponseCaptor;

    @Captor
    private ArgumentCaptor<ImpactDataResponse> impactResponseCaptor;

    @Captor
    private ArgumentCaptor<FundUsageResponse> fundUsageResponseCaptor;

    private BondTokenizationServiceImpl bondTokenizationService;

    @BeforeEach
    void setUp() {
        bondTokenizationService = new BondTokenizationServiceImpl(blockchainService);
    }

    @Test
    void shouldTokenizeBondSuccessfully() {
        // Given
        var request = TokenizeBondRequest.newBuilder()
                .setBondId("bond-123")
                .setTotalSupply("1000000")
                .setFaceValue("1000")
                .setCouponRate("5.5")
                .setMaturityDate("2028-12-31")
                .setProjectWallet("0xProjectWallet")
                .setVerifierReportHash("verifierHash123")
                .setIssuerWallet("0xIssuerWallet")
                .build();

        var blockchainResult = com.esgbank.greenbond.blockchain.model.BlockchainTransactionResult.builder()
                .transactionHash("0xTxHash123")
                .contractAddress("0xContract123")
                .blockNumber(1234567L)
                .status("SUCCESS")
                .build();

        when(blockchainService.tokenizeBond(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(blockchainResult);

        // When
        bondTokenizationService.tokenizeBond(request, tokenizeResponseObserver);

        // Then
        verify(tokenizeResponseObserver).onNext(tokenizeResponseCaptor.capture());
        verify(tokenizeResponseObserver).onCompleted();

        TokenizeBondResponse response = tokenizeResponseCaptor.getValue();
        assertThat(response.getTransactionHash()).isEqualTo("0xTxHash123");
        assertThat(response.getBondAddress()).isEqualTo("0xContract123");
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getBlockNumber()).isEqualTo(1234567L);
    }

    @Test
    void shouldHandleTokenizationFailure() {
        // Given
        var request = TokenizeBondRequest.newBuilder()
                .setBondId("bond-123")
                .setTotalSupply("1000000")
                .build();

        when(blockchainService.tokenizeBond(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Blockchain error"));

        // When
        bondTokenizationService.tokenizeBond(request, tokenizeResponseObserver);

        // Then
        verify(tokenizeResponseObserver).onNext(tokenizeResponseCaptor.capture());
        verify(tokenizeResponseObserver).onCompleted();

        TokenizeBondResponse response = tokenizeResponseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getMessage()).contains("Tokenization failed");
    }

    @Test
    void shouldGetBondStatusSuccessfully() {
        // Given
        var request = BondStatusRequest.newBuilder()
                .setBondId("bond-123")
                .setTransactionHash("0xTxHash")
                .build();

        var bondInfo = com.esgbank.greenbond.blockchain.model.BondInfo.builder()
                .bondId("bond-123")
                .bondAddress("0xContract123")
                .status("ACTIVE")
                .transactionHash("0xTxHash")
                .blockNumber(1234567L)
                .owner("0xOwner")
                .totalSupply("1000000")
                .build();

        when(blockchainService.getBondStatus(any(), any())).thenReturn(bondInfo);

        // When
        bondTokenizationService.getBondStatus(request, statusResponseObserver);

        // Then
        verify(statusResponseObserver).onNext(statusResponseCaptor.capture());
        verify(statusResponseObserver).onCompleted();

        BondStatusResponse response = statusResponseCaptor.getValue();
        assertThat(response.getBondId()).isEqualTo("bond-123");
        assertThat(response.getBondAddress()).isEqualTo("0xContract123");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldRecordImpactDataSuccessfully() {
        // Given
        var request = ImpactDataRequest.newBuilder()
                .setBondId("bond-123")
                .setMetricType("CO2_REDUCTION")
                .setValue(150.5)
                .setUnit("tons")
                .setTimestamp(1234567890L)
                .setSource("IoT_Sensor")
                .setDataHash("dataHash123")
                .build();

        var blockchainResult = com.esgbank.greenbond.blockchain.model.BlockchainTransactionResult.builder()
                .transactionHash("0xImpactTxHash")
                .blockNumber(1234568L)
                .status("SUCCESS")
                .build();

        when(blockchainService.recordImpactData(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(blockchainResult);

        // When
        bondTokenizationService.recordImpactData(request, impactResponseObserver);

        // Then
        verify(impactResponseObserver).onNext(impactResponseCaptor.capture());
        verify(impactResponseObserver).onCompleted();

        ImpactDataResponse response = impactResponseCaptor.getValue();
        assertThat(response.getTransactionHash()).isEqualTo("0xImpactTxHash");
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getImpactId()).isNotNull();
    }

    @Test
    void shouldVerifyFundUsageSuccessfully() {
        // Given
        var request = FundUsageRequest.newBuilder()
                .setBondId("bond-123")
                .setTransactionHash("0xTxHash")
                .setAmount("50000")
                .setRecipient("0xRecipient")
                .setPurpose("Solar Panels")
                .setDocumentHash("docHash123")
                .build();

        var verificationResult = com.esgbank.greenbond.blockchain.model.FundVerificationResult.builder()
                .verificationHash("verifyHash123")
                .verified(true)
                .message("Funds verified successfully")
                .blockNumber(1234569L)
                .build();

        when(blockchainService.verifyFundUsage(any(), any(), any(), any(), any(), any()))
                .thenReturn(verificationResult);

        // When
        bondTokenizationService.verifyFundUsage(request, fundUsageResponseObserver);

        // Then
        verify(fundUsageResponseObserver).onNext(fundUsageResponseCaptor.capture());
        verify(fundUsageResponseObserver).onCompleted();

        FundUsageResponse response = fundUsageResponseCaptor.getValue();
        assertThat(response.getVerificationHash()).isEqualTo("verifyHash123");
        assertThat(response.getIsVerified()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Funds verified successfully");
    }
}