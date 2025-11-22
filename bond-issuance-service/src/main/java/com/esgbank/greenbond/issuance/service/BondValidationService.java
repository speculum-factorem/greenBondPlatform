package com.esgbank.greenbond.issuance.service;

import com.esgbank.greenbond.issuance.dto.BondCreationRequest;
import com.esgbank.greenbond.issuance.exception.BondIssuanceException;
import com.esgbank.greenbond.issuance.model.Bond;
import com.esgbank.greenbond.issuance.model.enums.BondStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class BondValidationService {

    public void validateBondCreation(BondCreationRequest request) {
        log.debug("Validating bond creation request for project: {}", request.getProjectName());

        if (request.getMaturityDate().isBefore(LocalDate.now().plusYears(1))) {
            throw new BondIssuanceException("Maturity date must be at least 1 year in the future");
        }

        if (request.getCouponRate().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BondIssuanceException("Coupon rate must be positive");
        }

        if (request.getTotalSupply().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BondIssuanceException("Total supply must be positive");
        }

        if (request.getFaceValue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BondIssuanceException("Face value must be positive");
        }

        // Validate wallet address format (basic check)
        if (!request.getProjectWalletAddress().startsWith("0x") ||
                request.getProjectWalletAddress().length() != 42) {
            throw new BondIssuanceException("Invalid project wallet address format");
        }

        log.debug("Bond creation request validation passed");
    }

    public void validateForTokenization(Bond bond) {
        log.debug("Validating bond for tokenization: {}", bond.getBondId());

        if (bond.getStatus() != BondStatus.VERIFIED) {
            throw new BondIssuanceException(
                    "Bond must be in VERIFIED status for tokenization. Current status: " + bond.getStatus());
        }

        if (bond.getVerifierReportHash() == null || bond.getVerifierReportHash().trim().isEmpty()) {
            throw new BondIssuanceException("Verifier report hash is required for tokenization");
        }

        log.debug("Bond tokenization validation passed");
    }

    public void validateForAllocation(Bond bond) {
        log.debug("Validating bond for allocation: {}", bond.getBondId());

        if (bond.getStatus() != BondStatus.TOKENIZED && bond.getStatus() != BondStatus.ACTIVE) {
            throw new BondIssuanceException(
                    "Bond must be in TOKENIZED or ACTIVE status for allocation. Current status: " + bond.getStatus());
        }

        if (bond.getBlockchainTxHash() == null || bond.getBlockchainTxHash().trim().isEmpty()) {
            throw new BondIssuanceException("Blockchain transaction hash is required for allocation");
        }

        log.debug("Bond allocation validation passed");
    }
}