package com.esgbank.greenbond.blockchain.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Slf4j
@Configuration
public class BlockchainConfig {

    @Value("${app.blockchain.node.url:http://localhost:8545}")
    private String blockchainNodeUrl;

    @Value("${app.blockchain.chain.id:1337}")
    private Long chainId;

    @Value("${app.blockchain.gas.limit:3000000}")
    private Long gasLimit;

    @Bean
    public Web3j web3j() {
        log.info("Initializing Web3j connection to: {}", blockchainNodeUrl);
        try {
            Web3j web3j = Web3j.build(new HttpService(blockchainNodeUrl));
            // Test connection with timeout
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            log.info("Successfully connected to Ethereum node: {}", clientVersion);
            return web3j;
        } catch (Exception e) {
            log.error("Failed to connect to Ethereum node: {}", e.getMessage());
            log.warn("Blockchain integration will operate in degraded mode. Some features may be unavailable.");
            // Return a null-safe wrapper instead of throwing to allow service to start
            // The service can check connection status and handle gracefully
            throw new RuntimeException("Blockchain node connection failed", e);
        }
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(blockchainNodeUrl)
                .build();
    }

    @Bean
    public BlockchainProperties blockchainProperties() {
        return BlockchainProperties.builder()
                .nodeUrl(blockchainNodeUrl)
                .chainId(chainId)
                .gasLimit(gasLimit)
                .build();
    }
}