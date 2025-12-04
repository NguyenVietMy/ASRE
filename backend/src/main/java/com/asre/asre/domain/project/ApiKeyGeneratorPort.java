package com.asre.asre.domain.project;

/**
 * Domain port for API key generation.
 * Abstracts the underlying implementation details.
 */
public interface ApiKeyGeneratorPort {
    /**
     * Generates a new API key.
     * Format: asre_sk_<22-char-random>
     */
    String generateApiKey();

    /**
     * Masks an API key for display.
     * Shows: asre_sk_***************abcd (last 4-6 chars visible)
     */
    String maskApiKey(String apiKey);
}

