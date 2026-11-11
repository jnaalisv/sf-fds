package com.github.jnaalisv.sf_fds.http;

import com.github.jnaalisv.sf_fds.http.security.ApiKeyAuthenticationFilter;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** API keys configured for HTTP tests through {@code @TestPropertySource}. */
final class TestApiKeys {

	static final String PAYMENTS_GATEWAY_KEY = "test-key-payments-gateway";
	static final String CARD_ISSUER_KEY = "test-key-card-issuer";

	static final String PROPERTIES_PAYMENTS_GATEWAY_CLIENT = "fds.api-keys[0].client=payments-gateway";
	static final String PROPERTIES_PAYMENTS_GATEWAY_HASH =
			"fds.api-keys[0].sha256=03f78bb71cdb40537bc78df35d3659cbec4db5f69fbfe21210b050efd6621aef";
	static final String PROPERTIES_CARD_ISSUER_CLIENT = "fds.api-keys[1].client=card-issuer";
	static final String PROPERTIES_CARD_ISSUER_HASH =
			"fds.api-keys[1].sha256=17d5f77f2b2deda4b992ad7d6e7300446a64d022df0be434f61c85f6fce8f18c";

	private TestApiKeys() {
	}

	static RequestPostProcessor apiKey(String key) {
		return request -> {
			request.addHeader(ApiKeyAuthenticationFilter.HEADER, key);
			return request;
		};
	}

	static RequestPostProcessor validApiKey() {
		return apiKey(PAYMENTS_GATEWAY_KEY);
	}
}
