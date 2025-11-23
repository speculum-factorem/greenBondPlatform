package com.esgbank.greenbond.issuance.integration;

import com.esgbank.greenbond.blockchain.proto.BondTokenizationServiceGrpc;
import com.esgbank.greenbond.blockchain.proto.TokenizeBondRequest;
import com.esgbank.greenbond.blockchain.proto.TokenizeBondResponse;
import com.esgbank.greenbond.issuance.exception.BondIssuanceException;
import com.esgbank.greenbond.issuance.model.Bond;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    @GrpcClient("blockchain-service")
    private BondTokenizationServiceGrpc.BondTokenizationServiceBlockingStub bondTokenizationStub;

    // Токенизация облигации через gRPC вызов к blockchain-integration сервису
    public String tokenizeBond(Bond bond) {
        log.info("Sending bond tokenization request to blockchain for bond: {}", bond.getBondId());

        try {
            // Создаем gRPC запрос с данными облигации для токенизации
            TokenizeBondRequest request = TokenizeBondRequest.newBuilder()
                    .setBondId(bond.getBondId())
                    .setTotalSupply(bond.getTotalSupply().toString())
                    .setFaceValue(bond.getFaceValue().toString())
                    .setCouponRate(bond.getCouponRate().toString())
                    .setMaturityDate(bond.getMaturityDate().toString())
                    .setProjectWallet(bond.getProjectWalletAddress())
                    .setVerifierReportHash(bond.getVerifierReportHash())
                    .setIssuerWallet("0xIssuerWallet") // В реальной реализации должно браться из профиля эмитента
                    .build();

            // Вызываем gRPC метод токенизации на blockchain-integration сервисе
            TokenizeBondResponse response = bondTokenizationStub.tokenizeBond(request);

            // Проверяем статус ответа
            if (!"SUCCESS".equals(response.getStatus())) {
                throw new BondIssuanceException("Blockchain tokenization failed: " + response.getMessage());
            }

            log.info("Bond tokenization successful on blockchain. TxHash: {}", response.getTransactionHash());
            return response.getTransactionHash();

        } catch (StatusRuntimeException e) {
            // Обработка ошибок gRPC
            log.error("gRPC call failed for bond: {}. Status: {}, Error: {}",
                    bond.getBondId(), e.getStatus(), e.getMessage());
            throw new BondIssuanceException("Blockchain integration failed: " + e.getMessage(), e);
        } catch (Exception e) {
            // Обработка неожиданных ошибок
            log.error("Unexpected error during bond tokenization: {}. Error: {}",
                    bond.getBondId(), e.getMessage(), e);
            throw new BondIssuanceException("Bond tokenization failed: " + e.getMessage(), e);
        }
    }
}