package com.esgbank.greenbond.blockchain.service;

import com.esgbank.greenbond.blockchain.exception.BlockchainException;
import com.esgbank.greenbond.blockchain.model.BlockchainTransactionResult;
import com.esgbank.greenbond.blockchain.model.BondInfo;
import com.esgbank.greenbond.blockchain.model.FundVerificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    private final Web3j web3j;
    private final SmartContractService smartContractService;
    private final TransactionService transactionService;

    // Токенизация облигации на блокчейне: создание смарт-контракта для облигации
    public BlockchainTransactionResult tokenizeBond(
            String bondId,
            String totalSupply,
            String faceValue,
            String couponRate,
            String maturityDate,
            String projectWallet,
            String verifierReportHash,
            String issuerWallet) {

        log.info("Tokenizing bond: {} with supply: {}, face value: {}",
                bondId, totalSupply, faceValue);

        try {
            // Деплоим смарт-контракт для токенизации облигации на Ethereum
            TransactionReceipt receipt = smartContractService.deployBondToken(
                    bondId,
                    totalSupply,
                    faceValue,
                    couponRate,
                    maturityDate,
                    projectWallet,
                    verifierReportHash,
                    issuerWallet
            );

            // Возвращаем результат транзакции с хешем и адресом контракта
            return BlockchainTransactionResult.builder()
                    .transactionHash(receipt.getTransactionHash())
                    .contractAddress(receipt.getContractAddress())
                    .blockNumber(receipt.getBlockNumber().longValue())
                    .status("SUCCESS")
                    .gasUsed(receipt.getGasUsed().toString())
                    .build();

        } catch (Exception e) {
            log.error("Bond tokenization failed for bond: {}. Error: {}", bondId, e.getMessage(), e);
            throw new BlockchainException("Failed to tokenize bond: " + bondId, e);
        }
    }

    // Получение информации об облигации из блокчейна
    public BondInfo getBondStatus(String bondId, String transactionHash) {
        log.debug("Getting bond status for bond: {}", bondId);

        try {
            // Читаем данные облигации из смарт-контракта
            return smartContractService.getBondInfo(bondId, transactionHash);
        } catch (Exception e) {
            log.error("Failed to get bond status for bond: {}. Error: {}", bondId, e.getMessage());
            throw new BlockchainException("Failed to get bond status: " + bondId, e);
        }
    }

    // Запись ESG-метрики в блокчейн для неизменяемости данных
    public BlockchainTransactionResult recordImpactData(
            String bondId,
            String metricType,
            double value,
            String unit,
            long timestamp,
            String source,
            String dataHash) {

        log.info("Recording impact data for bond: {}, metric: {}, value: {}",
                bondId, metricType, value);

        try {
            // Записываем метрику в смарт-контракт на блокчейне
            TransactionReceipt receipt = smartContractService.recordImpact(
                    bondId,
                    metricType,
                    value,
                    unit,
                    timestamp,
                    source,
                    dataHash
            );

            // Возвращаем результат транзакции
            return BlockchainTransactionResult.builder()
                    .transactionHash(receipt.getTransactionHash())
                    .blockNumber(receipt.getBlockNumber().longValue())
                    .status("SUCCESS")
                    .gasUsed(receipt.getGasUsed().toString())
                    .build();

        } catch (Exception e) {
            log.error("Impact data recording failed for bond: {}. Error: {}", bondId, e.getMessage(), e);
            throw new BlockchainException("Failed to record impact data for bond: " + bondId, e);
        }
    }

    public FundVerificationResult verifyFundUsage(
            String bondId,
            String transactionHash,
            String amount,
            String recipient,
            String purpose,
            String documentHash) {

        log.info("Verifying fund usage for bond: {}, amount: {}, recipient: {}",
                bondId, amount, recipient);

        try {
            return smartContractService.verifyFundUsage(
                    bondId,
                    transactionHash,
                    amount,
                    recipient,
                    purpose,
                    documentHash
            );
        } catch (Exception e) {
            log.error("Fund usage verification failed for bond: {}. Error: {}", bondId, e.getMessage(), e);
            throw new BlockchainException("Failed to verify fund usage for bond: " + bondId, e);
        }
    }

    public String getNetworkInfo() {
        try {
            return web3j.netVersion().send().getNetVersion();
        } catch (Exception e) {
            log.error("Failed to get network info: {}", e.getMessage());
            throw new BlockchainException("Failed to get network info", e);
        }
    }
}