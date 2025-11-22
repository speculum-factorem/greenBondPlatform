package com.esgbank.greenbond.blockchain.service;

import com.esgbank.greenbond.blockchain.exception.BlockchainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlockchainServiceTest {

    @Mock
    private Web3j web3j;

    @Mock
    private SmartContractService smartContractService;

    @Mock
    private TransactionService transactionService;

    private BlockchainService blockchainService;

    @BeforeEach
    void setUp() {
        blockchainService = new BlockchainService(web3j, smartContractService, transactionService);
    }

    @Test
    void shouldTokenizeBondSuccessfully() throws Exception {
        // Given
        String bondId = "bond-123";
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xTxHash123");
        receipt.setContractAddress("0xContract123");
        receipt.setBlockNumber(BigInteger.valueOf(1234567L));

        when(smartContractService.deployBondToken(
                any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(receipt);

        // When
        var result = blockchainService.tokenizeBond(
                bondId, "1000000", "1000", "5.5", "2028-12-31",
                "0xProjectWallet", "verifierHash123", "0xIssuerWallet"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash()).isEqualTo("0xTxHash123");
        assertThat(result.getContractAddress()).isEqualTo("0xContract123");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
    }

    @Test
    void shouldThrowExceptionWhenTokenizationFails() throws Exception {
        // Given
        String bondId = "bond-123";
        when(smartContractService.deployBondToken(
                any(), any(), any(), any(), any(), any(), any(), any()
        )).thenThrow(new RuntimeException("Blockchain error"));

        // When & Then
        assertThatThrownBy(() -> blockchainService.tokenizeBond(
                bondId, "1000000", "1000", "5.5", "2028-12-31",
                "0xProjectWallet", "verifierHash123", "0xIssuerWallet"
        )).isInstanceOf(BlockchainException.class)
                .hasMessageContaining("Failed to tokenize bond");
    }

    @Test
    void shouldGetBondStatusSuccessfully() throws Exception {
        // Given
        String bondId = "bond-123";
        var bondInfo = com.esgbank.greenbond.blockchain.model.BondInfo.builder()
                .bondId(bondId)
                .bondAddress("0xContract123")
                .status("ACTIVE")
                .build();

        when(smartContractService.getBondInfo(any(), any())).thenReturn(bondInfo);

        // When
        var result = blockchainService.getBondStatus(bondId, "0xTxHash");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBondId()).isEqualTo(bondId);
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldRecordImpactDataSuccessfully() throws Exception {
        // Given
        String bondId = "bond-123";
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xImpactTxHash");
        receipt.setBlockNumber(BigInteger.valueOf(1234568L));

        when(smartContractService.recordImpact(
                any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(receipt);

        // When
        var result = blockchainService.recordImpactData(
                bondId, "CO2_REDUCTION", 150.5, "tons",
                1234567890L, "IoT_Sensor", "dataHash123"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash()).isEqualTo("0xImpactTxHash");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
    }
}