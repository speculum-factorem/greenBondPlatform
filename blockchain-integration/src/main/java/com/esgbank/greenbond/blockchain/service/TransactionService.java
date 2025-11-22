package com.esgbank.greenbond.blockchain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final Web3j web3j;

    public CompletableFuture<TransactionReceipt> waitForTransactionReceipt(String transactionHash) {
        log.info("Waiting for transaction receipt: {}", transactionHash);

        return web3j.ethGetTransactionReceipt(transactionHash).sendAsync()
                .thenCompose(transactionReceipt -> {
                    if (transactionReceipt.getTransactionReceipt().isPresent()) {
                        return CompletableFuture.completedFuture(transactionReceipt.getTransactionReceipt().get());
                    } else {
                        // Poll until receipt is available
                        return CompletableFuture.supplyAsync(() -> {
                            try {
                                Thread.sleep(1000); // Wait 1 second
                                return waitForTransactionReceipt(transactionHash).get();
                            } catch (Exception e) {
                                throw new BlockchainException("Failed to get transaction receipt", e);
                            }
                        });
                    }
                });
    }

    public boolean isTransactionConfirmed(String transactionHash, int requiredConfirmations) {
        try {
            TransactionReceipt receipt = web3j.ethGetTransactionReceipt(transactionHash).send().getTransactionReceipt()
                    .orElseThrow(() -> new BlockchainException("Transaction not found"));

            BigInteger currentBlock = web3j.ethBlockNumber().send().getBlockNumber();
            BigInteger txBlock = receipt.getBlockNumber();

            return currentBlock.subtract(txBlock).compareTo(BigInteger.valueOf(requiredConfirmations)) >= 0;

        } catch (Exception e) {
            log.error("Failed to check transaction confirmation: {}", e.getMessage());
            return false;
        }
    }
}