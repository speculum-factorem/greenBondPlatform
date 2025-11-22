package com.esgbank.greenbond.blockchain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmartContractServiceTest {

    @Mock
    private Web3j web3j;

    @Mock
    private ContractGasProvider gasProvider;

    private SmartContractService smartContractService;

    @BeforeEach
    void setUp() {
        smartContractService = new SmartContractService(new Web3jService(web3j), gasProvider);
    }

    @Test
    void shouldDeployBondToken() throws Exception {
        // When
        TransactionReceipt result = smartContractService.deployBondToken(
                "bond-123", "1000000", "1000", "5.5", "2028-12-31",
                "0xProjectWallet", "verifierHash123", "0xIssuerWallet"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash()).isNotNull();
        assertThat(result.getContractAddress()).isNotNull();
    }

    @Test
    void shouldGetBondInfo() throws Exception {
        // When
        var result = smartContractService.getBondInfo("bond-123", "0xTxHash");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBondId()).isEqualTo("bond-123");
        assertThat(result.getBondAddress()).isNotNull();
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldRecordImpactData() throws Exception {
        // When
        TransactionReceipt result = smartContractService.recordImpact(
                "bond-123", "CO2_REDUCTION", 150.5, "tons",
                1234567890L, "IoT_Sensor", "dataHash123"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionHash()).isNotNull();
    }

    @Test
    void shouldVerifyFundUsage() throws Exception {
        // When
        var result = smartContractService.verifyFundUsage(
                "bond-123", "0xTxHash", "50000",
                "0xRecipient", "Solar Panels", "docHash123"
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isVerified()).isTrue();
        assertThat(result.getVerificationHash()).isNotNull();
    }
}