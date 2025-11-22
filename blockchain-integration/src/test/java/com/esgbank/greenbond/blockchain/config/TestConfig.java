package com.esgbank.greenbond.blockchain.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    public Web3j web3j() throws IOException {
        Web3j web3j = mock(Web3j.class);

        // Mock web3ClientVersion
        Web3ClientVersion clientVersion = new Web3ClientVersion();
        clientVersion.setResult("TestNode/1.0");

        Request<?, Web3ClientVersion> clientVersionRequest = mock(Request.class);
        when(clientVersionRequest.send()).thenReturn(clientVersion);
        when(web3j.web3ClientVersion()).thenReturn((Request<?, Web3ClientVersion>) clientVersionRequest);

        // Mock ethBlockNumber
        EthBlockNumber blockNumber = new EthBlockNumber();
        blockNumber.setResult(BigInteger.valueOf(1234567));

        Request<?, EthBlockNumber> blockNumberRequest = mock(Request.class);
        when(blockNumberRequest.send()).thenReturn(blockNumber);
        when(web3j.ethBlockNumber()).thenReturn((Request<?, EthBlockNumber>) blockNumberRequest);

        // Mock ethGasPrice
        EthGasPrice gasPrice = new EthGasPrice();
        gasPrice.setResult(BigInteger.valueOf(20000000000L));

        Request<?, EthGasPrice> gasPriceRequest = mock(Request.class);
        when(gasPriceRequest.send()).thenReturn(gasPrice);
        when(web3j.ethGasPrice()).thenReturn((Request<?, EthGasPrice>) gasPriceRequest);

        return web3j;
    }

    @Bean
    public ContractGasProvider gasProvider() {
        return new StaticGasProvider(BigInteger.valueOf(20000000000L), BigInteger.valueOf(6721975));
    }

    @Bean
    public BlockchainProperties blockchainProperties() {
        return BlockchainProperties.builder()
                .nodeUrl("http://localhost:8545")
                .chainId(1337L)
                .gasLimit(3000000L)
                .contractAddress("0xTestContractAddress")
                .privateKey("testPrivateKey")
                .adminWallet("0xTestAdminWallet")
                .build();
    }
}