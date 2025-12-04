package com.asre.asre.infra.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for generating API keys.
 * Format: asre_sk_<22-char-random>
 */
@Component
public class ApiKeyGenerator {

    private static final String PREFIX = "asre_sk_";
    private static final int RANDOM_BYTES = 16; // 16 bytes = 22 chars in base64
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a new API key.
     * Format: asre_sk_<22-char-base64-random>
     */
    public String generateApiKey() {
        byte[] randomBytes = new byte[RANDOM_BYTES];
        secureRandom.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        return PREFIX + randomPart;
    }

    /**
     * Masks an API key for display.
     * Shows: asre_sk_***************abcd (last 4-6 chars visible)
     */
    public String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= PREFIX.length() + 6) {
            return "asre_sk_****";
        }
        int visibleChars = Math.min(6, apiKey.length() - PREFIX.length());
        String lastChars = apiKey.substring(apiKey.length() - visibleChars);
        return PREFIX + "*".repeat(Math.max(0, apiKey.length() - PREFIX.length() - visibleChars)) + lastChars;
    }
}
