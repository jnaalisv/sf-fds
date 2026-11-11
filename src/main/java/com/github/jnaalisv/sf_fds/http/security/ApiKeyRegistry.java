package com.github.jnaalisv.sf_fds.http.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Resolves a presented API key to the name of the calling service.
 * Keys are high-entropy random values, so a single SHA-256 is enough; a slow
 * password hash would only add latency to every request.
 */
public class ApiKeyRegistry {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyRegistry.class);
    private static final Pattern SHA256_HEX = Pattern.compile("[0-9a-f]{64}");

    /** SHA-256 of the well-known local development key in application.properties. */
    static final String LOCAL_DEV_KEY_SHA256 = "d5f0236bb84a5cf975aa95f2a41c9938f0e7cacf8e00591dead5d3ea1d053e0d";

    private final Map<String, String> clientsByKeyHash;

    public ApiKeyRegistry(ApiKeyProperties properties) {
        if (properties.apiKeys().isEmpty()) {
            throw new IllegalStateException("No API keys configured (fds.api-keys); refusing to start");
        }
        Map<String, String> byHash = new HashMap<>();
        for (ApiKeyProperties.ApiKey apiKey : properties.apiKeys()) {
            String client = apiKey.client();
            if (client == null || client.isBlank()) {
                throw new IllegalStateException("API key entry without a client name");
            }
            String hash = apiKey.sha256() == null ? "" : apiKey.sha256().trim().toLowerCase();
            if (!SHA256_HEX.matcher(hash).matches()) {
                throw new IllegalStateException("API key for client '" + client + "' is not a SHA-256 hex digest");
            }
            String previous = byHash.putIfAbsent(hash, client);
            if (previous != null) {
                throw new IllegalStateException(
                        "Same API key configured for clients '" + previous + "' and '" + client + "'");
            }
        }
        if (byHash.containsKey(LOCAL_DEV_KEY_SHA256)) {
            log.warn("The local development API key is enabled. Override fds.api-keys outside local development.");
        }
        this.clientsByKeyHash = Map.copyOf(byHash);
    }

    public Optional<String> clientFor(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(clientsByKeyHash.get(sha256Hex(apiKey)));
    }

    static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
