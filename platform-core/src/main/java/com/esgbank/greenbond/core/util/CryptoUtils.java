package com.esgbank.greenbond.core.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@UtilityClass
public class CryptoUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final HexFormat HEX_FORMAT = HexFormat.of();
    private static final String SHA_256 = "SHA-256";
    private static final String SHA_512 = "SHA-512";

    public static String generateRandomString(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String generateSecureToken() {
        return generateRandomString(32);
    }

    public static String generateApiKey() {
        return "gb_" + generateRandomString(40);
    }

    public static String hashSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HEX_FORMAT.formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available. RequestId: {}", MDC.get("requestId"), e);
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    public static String hashSha512(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_512);
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HEX_FORMAT.formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-512 algorithm not available. RequestId: {}", MDC.get("requestId"), e);
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    public static String hashWithSalt(String input, String salt) {
        return hashSha256(input + salt);
    }

    public static boolean verifyHash(String input, String salt, String expectedHash) {
        String actualHash = hashWithSalt(input, salt);
        return MessageDigest.isEqual(
                actualHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static String generateDocumentHash(byte[] documentBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(documentBytes);
            return HEX_FORMAT.formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate document hash. RequestId: {}", MDC.get("requestId"), e);
            throw new RuntimeException("Document hashing failed", e);
        }
    }

    public static String base64Encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] base64Decode(String data) {
        return Base64.getDecoder().decode(data);
    }

    public static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    public static byte[] base64UrlDecode(String data) {
        return Base64.getUrlDecoder().decode(data);
    }

    public static String generateBlockchainTxHash() {
        return "0x" + generateRandomString(64);
    }

    public static boolean isValidHash(String hash) {
        if (hash == null || hash.length() != 64) {
            return false;
        }
        return hash.matches("^[a-fA-F0-9]{64}$");
    }

    public static String normalizeEthereumAddress(String address) {
        if (address == null || !address.toLowerCase().startsWith("0x")) {
            return address;
        }
        return address.toLowerCase();
    }
}