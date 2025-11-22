package com.esgbank.greenbond.issuance.service;

import com.esgbank.greenbond.issuance.dto.BondCreationRequest;
import com.esgbank.greenbond.issuance.dto.BondResponse;
import com.esgbank.greenbond.issuance.exception.BondNotFoundException;
import com.esgbank.greenbond.issuance.exception.BondIssuanceException;
import com.esgbank.greenbond.issuance.integration.BlockchainService;
import com.esgbank.greenbond.issuance.mapper.BondMapper;
import com.esgbank.greenbond.issuance.model.Bond;
import com.esgbank.greenbond.issuance.model.enums.BondStatus;
import com.esgbank.greenbond.issuance.repository.BondRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BondService {

    private final BondRepository bondRepository;
    private final BondMapper bondMapper;
    private final BlockchainService blockchainService;
    private final BondValidationService bondValidationService;

    @Transactional
    public BondResponse createBond(BondCreationRequest request, String issuerId, String issuerName) {
        String requestId = MDC.get("requestId");
        log.info("Creating new bond for project: {}, issuer: {}, requestId: {}",
                request.getProjectName(), issuerId, requestId);

        try {
            // Validate bond creation request
            bondValidationService.validateBondCreation(request);

            // Create bond entity
            Bond bond = bondMapper.toEntity(request);
            bond.setIssuerId(issuerId);
            bond.setIssuerName(issuerName);
            bond.setStatus(BondStatus.DRAFT);

            Bond savedBond = bondRepository.save(bond);
            log.debug("Bond saved to database with id: {}", savedBond.getId());

            BondResponse response = bondMapper.toResponse(savedBond);
            log.info("Bond created successfully: {}, requestId: {}", savedBond.getBondId(), requestId);

            return response;

        } catch (Exception e) {
            log.error("Failed to create bond for project: {}. Error: {}",
                    request.getProjectName(), e.getMessage(), e);
            throw new BondIssuanceException("Bond creation failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public BondResponse tokenizeBond(String bondId, String issuerId) {
        log.info("Tokenizing bond: {}, issuer: {}", bondId, issuerId);

        Bond bond = bondRepository.findByBondId(bondId)
                .orElseThrow(() -> new BondNotFoundException("Bond not found: " + bondId));

        // Verify ownership
        if (!bond.getIssuerId().equals(issuerId)) {
            throw new BondIssuanceException("Unauthorized access to bond: " + bondId);
        }

        // Validate bond for tokenization
        bondValidationService.validateForTokenization(bond);

        try {
            // Tokenize bond on blockchain
            String transactionHash = blockchainService.tokenizeBond(bond);

            // Update bond status
            bond.setStatus(BondStatus.TOKENIZED);
            bond.setBlockchainTxHash(transactionHash);
            bond.setBondContractAddress(transactionHash); // In real implementation, this would be the contract address

            Bond updatedBond = bondRepository.save(bond);
            log.info("Bond tokenized successfully: {}, txHash: {}", bondId, transactionHash);

            return bondMapper.toResponse(updatedBond);

        } catch (Exception e) {
            log.error("Bond tokenization failed for bond: {}. Error: {}", bondId, e.getMessage(), e);
            throw new BondIssuanceException("Bond tokenization failed: " + e.getMessage(), e);
        }
    }

    public BondResponse getBond(String bondId) {
        log.debug("Fetching bond details for id: {}", bondId);

        Bond bond = bondRepository.findByBondId(bondId)
                .orElseThrow(() -> new BondNotFoundException("Bond not found: " + bondId));

        return bondMapper.toResponse(bond);
    }

    public BondResponse getBondById(String id) {
        log.debug("Fetching bond details for internal id: {}", id);

        Bond bond = bondRepository.findById(id)
                .orElseThrow(() -> new BondNotFoundException("Bond not found with id: " + id));

        return bondMapper.toResponse(bond);
    }

    public Page<BondResponse> getBondsByIssuer(String issuerId, Pageable pageable) {
        log.debug("Fetching bonds for issuer: {}, page: {}", issuerId, pageable.getPageNumber());

        Page<Bond> bonds = bondRepository.findByIssuerId(issuerId, pageable);
        return bonds.map(bondMapper::toResponse);
    }

    public Page<BondResponse> getBondsByStatus(BondStatus status, Pageable pageable) {
        log.debug("Fetching bonds with status: {}, page: {}", status, pageable.getPageNumber());

        Page<Bond> bonds = bondRepository.findByStatus(status, pageable);
        return bonds.map(bondMapper::toResponse);
    }

    @Transactional
    public BondResponse updateBondStatus(String bondId, BondStatus status, String issuerId) {
        log.info("Updating bond status: {}, new status: {}, issuer: {}", bondId, status, issuerId);

        Bond bond = bondRepository.findByBondId(bondId)
                .orElseThrow(() -> new BondNotFoundException("Bond not found: " + bondId));

        // Verify ownership
        if (!bond.getIssuerId().equals(issuerId)) {
            throw new BondIssuanceException("Unauthorized access to bond: " + bondId);
        }

        bond.setStatus(status);
        Bond updatedBond = bondRepository.save(bond);

        log.info("Bond status updated successfully: {}, new status: {}", bondId, status);
        return bondMapper.toResponse(updatedBond);
    }

    @Transactional
    public void deleteBond(String bondId, String issuerId) {
        log.info("Deleting bond: {}, issuer: {}", bondId, issuerId);

        Bond bond = bondRepository.findByBondId(bondId)
                .orElseThrow(() -> new BondNotFoundException("Bond not found: " + bondId));

        // Verify ownership and check if bond can be deleted
        if (!bond.getIssuerId().equals(issuerId)) {
            throw new BondIssuanceException("Unauthorized access to bond: " + bondId);
        }

        if (bond.getStatus().ordinal() > BondStatus.DRAFT.ordinal()) {
            throw new BondIssuanceException("Cannot delete bond with status: " + bond.getStatus());
        }

        bondRepository.delete(bond);
        log.info("Bond deleted successfully: {}", bondId);
    }
}