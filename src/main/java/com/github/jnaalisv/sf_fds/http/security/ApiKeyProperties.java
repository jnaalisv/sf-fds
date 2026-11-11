package com.github.jnaalisv.sf_fds.http.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * API keys accepted by the HTTP API, one or more per calling service.
 * Only the SHA-256 hash (hex) of each key is configured, never the key itself.
 */
@ConfigurationProperties("fds")
public record ApiKeyProperties(List<ApiKey> apiKeys) {

    public ApiKeyProperties {
        apiKeys = apiKeys == null ? List.of() : List.copyOf(apiKeys);
    }

    public record ApiKey(String client, String sha256) {
    }
}
