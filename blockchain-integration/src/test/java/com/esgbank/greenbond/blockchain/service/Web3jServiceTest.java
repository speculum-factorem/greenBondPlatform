package com.esgbank.greenbond.blockchain.service;

import com.esgbank.greenbond.blockchain.exception.BlockchainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;

import java.io.IOException;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Web3jServiceTest {

    @Mock
    private Web3j web3j;

    private Web3jService web3jService;

    @BeforeEach
    void setUp() {
        web3jService = new Web3jService(web3j);
    }

    @Test
    void shouldGetCurrentBlockNumber() throws Exception {
        // Given
        EthBlockNumber blockNumber = new EthBlockNumber();
        blockNumber.setResult(BigInteger.valueOf(1234567));

        Request<?, EthBlockNumber> request = mock(Request.class);
        when(request.send()).thenReturn(blockNumber);
        when(web3j.ethBlockNumber()).thenReturn((Request<?, EthBlockNumber>) request);

        // When
        BigInteger result = web3jService.getCurrentBlockNumber();

        // Then
        assertThat(result).isEqualTo(BigInteger.valueOf(1234567));
    }

    @Test
    void shouldThrowExceptionWhenBlockNumberFails() throws Exception {
        // Given
        Request<?, EthBlockNumber> request = mock(Request.class);
        when(request.send()).thenThrow(new IOException("Network error"));
        when(web3j.ethBlockNumber()).thenReturn((Request<?, EthBlockNumber>) request);

        // When & Then
        assertThatThrownBy(() -> web3jService.getCurrentBlockNumber())
                .isInstanceOf(BlockchainException.class)
                .hasMessageContaining("Failed to get block number");
    }

    @Test
    void shouldGetGasPrice() throws Exception {
        // Given
        EthGasPrice gasPrice = new EthGasPrice();
        gasPrice.setResult(BigInteger.valueOf(20000000000L));

        Request<?, EthGasPrice> request = mock(Request.class);
        when(request.send()).thenReturn(gasPrice);
        when(web3j.ethGasPrice()).thenReturn((Request<?, EthGasPrice>) request);

        // When
        BigInteger result = web3jService.getGasPrice();

        // Then
        assertThat(result).isEqualTo(BigInteger.valueOf(20000000000L));
    }

    @Test
    void shouldCheckConnection() throws Exception {
        // Given
        Web3ClientVersion clientVersion = new Web3ClientVersion();
        clientVersion.setResult("TestNode/1.0");

        Request<?, Web3ClientVersion> request = mock(Request.class);
        when(request.send()).thenReturn(clientVersion);
        when(web3j.web3ClientVersion()).thenReturn((Request<?, Web3ClientVersion>) request);

        // When
        boolean result = web3jService.isConnected();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenConnectionFails() throws Exception {
        // Given
        Request<?, Web3ClientVersion> request = mock(Request.class);
        when(request.send()).thenThrow(new IOException("Connection failed"));
        when(web3j.web3ClientVersion()).thenReturn((Request<?, Web3ClientVersion>) request);

        // When
        boolean result = web3jService.isConnected();

        // Then
        assertThat(result).isFalse();
    }
}