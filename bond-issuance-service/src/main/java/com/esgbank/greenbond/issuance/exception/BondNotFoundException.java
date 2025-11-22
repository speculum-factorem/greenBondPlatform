package com.esgbank.greenbond.issuance.exception;

public class BondNotFoundException extends BondIssuanceException {

    public BondNotFoundException(String message) {
        super("BOND_NOT_FOUND", message);
    }
}