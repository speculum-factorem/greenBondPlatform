package com.esgbank.greenbond.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Arrays;

@Slf4j
public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

    private Class<? extends Enum<?>> enumClass;
    private boolean ignoreCase;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
        this.ignoreCase = constraintAnnotation.ignoreCase();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null validation
        }

        try {
            Enum<?>[] enumConstants = enumClass.getEnumConstants();
            if (enumConstants == null) {
                log.warn("Enum class {} has no constants. RequestId: {}",
                        enumClass.getSimpleName(), MDC.get("requestId"));
                return false;
            }

            if (ignoreCase) {
                return Arrays.stream(enumConstants)
                        .anyMatch(e -> e.name().equalsIgnoreCase(value));
            } else {
                return Arrays.stream(enumConstants)
                        .anyMatch(e -> e.name().equals(value));
            }
        } catch (Exception e) {
            log.error("Error validating enum value: {} for enum: {}. RequestId: {}",
                    value, enumClass.getSimpleName(), MDC.get("requestId"), e);
            return false;
        }
    }
}