package com.esgbank.greenbond.verification.service;

import com.esgbank.greenbond.verification.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockchainService {

    public void recordDocumentVerification(Document document) {
        log.info("Recording document verification on blockchain: {}", document.getDocumentId());

        try {
            // In a real implementation, this would call the blockchain integration service
            // to record the document verification hash on the blockchain

            // Mock implementation
            String verificationHash = generateVerificationHash(document);

            log.info("Document verification recorded on blockchain: {}, hash: {}",
                    document.getDocumentId(), verificationHash);

        } catch (Exception e) {
            log.error("Failed to record document verification on blockchain: {}. Error: {}",
                    document.getDocumentId(), e.getMessage(), e);
            // Don't throw exception to avoid blocking the verification process
        }
    }

    private String generateVerificationHash(Document document) {
        String data = document.getDocumentId() +
                document.getBondId() +
                document.getFileHash() +
                document.getVerifiedAt().toString();

        return "VERIFY-" + Integer.toHexString(data.hashCode());
    }
}