package com.esgbank.greenbond.core.model.id;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.UUID;

/**
 * Custom ID generator that produces URL-safe UUIDs
 */
public class CustomIdGenerator implements IdentifierGenerator {

    private static final Logger log = LoggerFactory.getLogger(CustomIdGenerator.class);

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        String uuid = generateUrlSafeUuid();
        log.debug("Generated custom ID: {}", uuid);
        return uuid;
    }

    private String generateUrlSafeUuid() {
        return UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
}