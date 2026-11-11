package com.github.jnaalisv.sf_fds.http.security;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class ApiKeyRegistryTests {

	private static final String GATEWAY_KEY = "gateway-secret";
	private static final String ISSUER_KEY = "issuer-secret";

	@Test
	void resolvesEachKeyToItsClient() {
		ApiKeyRegistry registry = registry(
				new ApiKeyProperties.ApiKey("payments-gateway", ApiKeyRegistry.sha256Hex(GATEWAY_KEY)),
				new ApiKeyProperties.ApiKey("card-issuer", ApiKeyRegistry.sha256Hex(ISSUER_KEY)));

		assertThat(registry.clientFor(GATEWAY_KEY)).contains("payments-gateway");
		assertThat(registry.clientFor(ISSUER_KEY)).contains("card-issuer");
	}

	@Test
	void unknownMissingOrEmptyKeyResolvesToNothing() {
		ApiKeyRegistry registry = registry(
				new ApiKeyProperties.ApiKey("payments-gateway", ApiKeyRegistry.sha256Hex(GATEWAY_KEY)));

		assertThat(registry.clientFor("other")).isEqualTo(Optional.empty());
		assertThat(registry.clientFor(null)).isEmpty();
		assertThat(registry.clientFor("")).isEmpty();
	}

	@Test
	void configuredHashIsNotItselfAValidKey() {
		String hash = ApiKeyRegistry.sha256Hex(GATEWAY_KEY);
		ApiKeyRegistry registry = registry(new ApiKeyProperties.ApiKey("payments-gateway", hash));

		assertThat(registry.clientFor(hash)).isEmpty();
	}

	@Test
	void acceptsUppercaseHexInConfiguration() {
		ApiKeyRegistry registry = registry(
				new ApiKeyProperties.ApiKey("payments-gateway", ApiKeyRegistry.sha256Hex(GATEWAY_KEY).toUpperCase()));

		assertThat(registry.clientFor(GATEWAY_KEY)).contains("payments-gateway");
	}

	@Test
	void sameClientMayHaveSeveralKeysDuringRotation() {
		ApiKeyRegistry registry = registry(
				new ApiKeyProperties.ApiKey("payments-gateway", ApiKeyRegistry.sha256Hex("old-key")),
				new ApiKeyProperties.ApiKey("payments-gateway", ApiKeyRegistry.sha256Hex("new-key")));

		assertThat(registry.clientFor("old-key")).contains("payments-gateway");
		assertThat(registry.clientFor("new-key")).contains("payments-gateway");
	}

	@Test
	void hashMatchesShasumOfLocalDevelopmentKey() {
		assertThat(ApiKeyRegistry.sha256Hex("local-dev-api-key-do-not-use-in-production"))
				.isEqualTo(ApiKeyRegistry.LOCAL_DEV_KEY_SHA256);
	}

	@Test
	void refusesToStartWithoutKeys() {
		assertThatIllegalStateException().isThrownBy(() -> new ApiKeyRegistry(new ApiKeyProperties(null)));
		assertThatIllegalStateException().isThrownBy(() -> new ApiKeyRegistry(new ApiKeyProperties(List.of())));
	}

	@Test
	void refusesMalformedHash() {
		assertThatIllegalStateException().isThrownBy(() ->
				registry(new ApiKeyProperties.ApiKey("payments-gateway", "plain-text-key")));
	}

	@Test
	void refusesMissingClientName() {
		assertThatIllegalStateException().isThrownBy(() ->
				registry(new ApiKeyProperties.ApiKey(" ", ApiKeyRegistry.sha256Hex(GATEWAY_KEY))));
	}

	@Test
	void refusesSameKeyForTwoClients() {
		String hash = ApiKeyRegistry.sha256Hex(GATEWAY_KEY);
		assertThatIllegalStateException().isThrownBy(() -> registry(
				new ApiKeyProperties.ApiKey("payments-gateway", hash),
				new ApiKeyProperties.ApiKey("card-issuer", hash)));
	}

	private static ApiKeyRegistry registry(ApiKeyProperties.ApiKey... keys) {
		return new ApiKeyRegistry(new ApiKeyProperties(List.of(keys)));
	}
}
