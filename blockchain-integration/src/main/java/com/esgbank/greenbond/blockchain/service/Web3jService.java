package com.esgbank.greenbond.blockchain.service;

import com.esgbank.greenbond.blockchain.exception.BlockchainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class Web3jService {

    private final Web3j web3j;

    public BigInteger getCurrentBlockNumber() {
        try {
            EthBlockNumber blockNumber = web3j.ethBlockNumber().send();
            return blockNumber.getBlockNumber();
        } catch (Exception e) {
            log.error("Failed to get current block number: {}", e.getMessage());
            throw new BlockchainException("Failed to get block number", e);
        }
    }

    public BigInteger getGasPrice() {
        try {
            EthGasPrice gasPrice = web3j.ethGasPrice().send();
            return gasPrice.getGasPrice();
        } catch (Exception e) {
            log.error("Failed to get gas price: {}", e.getMessage());
            throw new BlockchainException("Failed to get gas price", e);
        }
    }

    public BigInteger getBalance(String address) {
        try {
            EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            return balance.getBalance();
        } catch (Exception e) {
            log.error("Failed to get balance for address: {}. Error: {}", address, e.getMessage());
            throw new BlockchainException("Failed to get balance for address: " + address, e);
        }
    }

    public boolean isConnected() {
        try {
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion();
            log.debug("Blockchain node connection active: {}", clientVersion);
            return true;
        } catch (Exception e) {
            log.warn("Blockchain node connection failed: {}", e.getMessage());
            return false;
        }
    }
}