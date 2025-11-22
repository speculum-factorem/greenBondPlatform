package com.esgbank.greenbond.core.validation;

import com.esgbank.greenbond.core.util.ValidationUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class BlockchainAddressValidator implements ConstraintValidator<BlockchainAddress, String> {

    private BlockchainAddress.BlockchainType type;

    @Override
    public void initialize(BlockchainAddress constraintAnnotation) {
        this.type = constraintAnnotation.type();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null validation
        }

        try {
            switch (type) {
                case ETHEREUM:
                    return ValidationUtils.isValidEthereumAddress(value);
                case BITCOIN:
                    return isValidBitcoinAddress(value);
                case HYPERLEDGER:
                    return isValidHyperledgerAddress(value);
                default:
                    log.warn("Unsupported blockchain type: {}. RequestId: {}",
                            type, MDC.get("requestId"));
                    return false;
            }
        } catch (Exception e) {
            log.error("Error validating blockchain address: {} for type: {}. RequestId: {}",
                    value, type, MDC.get("requestId"), e);
            return false;
        }
    }

    private boolean isValidBitcoinAddress(String address) {
        // Basic Bitcoin address validation (simplified)
        return address != null &&
                (address.startsWith("1") || address.startsWith("3") || address.startsWith("bc1")) &&
                address.length() >= 26 && address.length() <= 62;
    }

    private boolean isValidHyperledgerAddress(String address) {
        // Hyperledger addresses are typically UUIDs or similar formats
        return address != null && address.matches("^[a-zA-Z0-9_-]{1,255}$");
    }
}