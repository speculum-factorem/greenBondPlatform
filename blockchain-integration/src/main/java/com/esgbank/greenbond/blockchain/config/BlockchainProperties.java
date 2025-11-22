package com.esgbank.greenbond.blockchain.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "app.blockchain")
public class BlockchainProperties {
    private String nodeUrl;
    private Long chainId;
    private Long gasLimit;
    private String contractAddress;
    private String privateKey;
    private String adminWallet;

    // Bond tokenization contract
    private BondContract bondContract;

    // Impact registry contract
    private ImpactContract impactContract;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BondContract {
        private String address;
        private String abiPath;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImpactContract {
        private String address;
        private String abiPath;
    }
}