package com.esgbank.greenbond.blockchain.exception;

import lombok.Getter;

@Getter
public class BlockchainException extends RuntimeException {

    private final String errorCode;

    public BlockchainException(String message) {
        super(message);
        this.errorCode = "BLOCKCHAIN_ERROR";
    }

    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BLOCKCHAIN_ERROR";
    }

    public BlockchainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}