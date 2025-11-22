package com.esgbank.greenbond.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilsTest {

    @BeforeEach
    void setUp() {
        TestMdcUtils.setupTestMdc();
    }

    @Test
    void shouldGenerateRandomStrings() {
        // When
        String random1 = CryptoUtils.generateRandomString(16);
        String random2 = CryptoUtils.generateRandomString(16);

        // Then
        assertThat(random1).isNotBlank();
        assertThat(random2).isNotBlank();
        assertThat(random1).isNotEqualTo(random2);
        assertThat(random1.length()).isGreaterThanOrEqualTo(16);
    }

    @Test
    void shouldGenerateSecureTokens() {
        // When
        String token1 = CryptoUtils.generateSecureToken();
        String token2 = CryptoUtils.generateSecureToken();

        // Then
        assertThat(token1).isNotBlank();
        assertThat(token2).isNotBlank();
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    void shouldGenerateApiKeys() {
        // When
        String apiKey = CryptoUtils.generateApiKey();

        // Then
        assertThat(apiKey).startsWith("gb_");
        assertThat(apiKey.length()).isGreaterThan(40);
    }

    @Test
    void shouldHashStringsConsistently() {
        // Given
        String input = "test input string";

        // When
        String hash1 = CryptoUtils.hashSha256(input);
        String hash2 = CryptoUtils.hashSha256(input);

        // Then
        assertThat(hash1).isNotBlank();
        assertThat(hash2).isNotBlank();
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1.length()).isEqualTo(64); // SHA-256 produces 64 hex characters
    }

    @Test
    void shouldHashWithSalt() {
        // Given
        String input = "password";
        String salt = "random-salt";

        // When
        String hash1 = CryptoUtils.hashWithSalt(input, salt);
        String hash2 = CryptoUtils.hashWithSalt(input, salt);
        String hashDifferentSalt = CryptoUtils.hashWithSalt(input, "different-salt");

        // Then
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).isNotEqualTo(hashDifferentSalt);
    }

    @Test
    void shouldVerifyHashes() {
        // Given
        String input = "password";
        String salt = "random-salt";
        String hash = CryptoUtils.hashWithSalt(input, salt);

        // When & Then
        assertTrue(CryptoUtils.verifyHash(input, salt, hash));
        assertFalse(CryptoUtils.verifyHash("wrong-password", salt, hash));
        assertFalse(CryptoUtils.verifyHash(input, "wrong-salt", hash));
    }

    @Test
    void shouldGenerateDocumentHash() {
        // Given
        byte[] document = "test document content".getBytes();

        // When
        String hash = CryptoUtils.generateDocumentHash(document);

        // Then
        assertThat(hash).isNotBlank();
        assertThat(hash.length()).isEqualTo(64);
        assertTrue(CryptoUtils.isValidHash(hash));
    }

    @Test
    void shouldEncodeAndDecodeBase64() {
        // Given
        String original = "test data to encode";

        // When
        String encoded = CryptoUtils.base64Encode(original.getBytes());
        byte[] decoded = CryptoUtils.base64Decode(encoded);
        String decodedString = new String(decoded);

        // Then
        assertThat(decodedString).isEqualTo(original);
    }

    @Test
    void shouldNormalizeEthereumAddress() {
        // Given
        String mixedCase = "0x742d35Cc6634C0532925a3b8Dc9F5a4B5a7F8E9C";

        // When
        String normalized = CryptoUtils.normalizeEthereumAddress(mixedCase);

        // Then
        assertThat(normalized).isEqualTo(mixedCase.toLowerCase());
    }

    @Test
    void shouldValidateHashes() {
        // Given
        String validHash = "a".repeat(64);
        String invalidHash = "a".repeat(63) + "g"; // contains non-hex character
        String tooShort = "a".repeat(63);
        String tooLong = "a".repeat(65);

        // When & Then
        assertTrue(CryptoUtils.isValidHash(validHash));
        assertFalse(CryptoUtils.isValidHash(invalidHash));
        assertFalse(CryptoUtils.isValidHash(tooShort));
        assertFalse(CryptoUtils.isValidHash(tooLong));
        assertFalse(CryptoUtils.isValidHash(null));
    }
}