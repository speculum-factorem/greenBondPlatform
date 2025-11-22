package com.esgbank.greenbond.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BlockchainAddressValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface BlockchainAddress {
    String message() default "Invalid blockchain address";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    BlockchainType type() default BlockchainType.ETHEREUM;

    enum BlockchainType {
        ETHEREUM,
        BITCOIN,
        HYPERLEDGER
    }
}