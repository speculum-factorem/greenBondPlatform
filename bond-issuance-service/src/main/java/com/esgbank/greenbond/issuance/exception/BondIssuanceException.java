package com.esgbank.greenbond.issuance.exception;

import lombok.Getter;

@Getter
public class BondIssuanceException extends RuntimeException {

    private final String errorCode;

    public BondIssuanceException(String message) {
        super(message);
        this.errorCode = "BOND_ISSUANCE_ERROR";
    }

    public BondIssuanceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BOND_ISSUANCE_ERROR";
    }

    public BondIssuanceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}