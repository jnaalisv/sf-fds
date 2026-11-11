package com.github.jnaalisv.sf_fds.http;

import com.github.jnaalisv.sf_fds.domain.parameters.FraudDetectionParameters;
import com.github.jnaalisv.sf_fds.domain.parameters.FraudDetectionParametersRepository;
import com.github.jnaalisv.sf_fds.domain.terminals.Terminal;
import com.github.jnaalisv.sf_fds.persistence.InMemoryTerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;

import static com.github.jnaalisv.sf_fds.http.TestApiKeys.CARD_ISSUER_KEY;
import static com.github.jnaalisv.sf_fds.http.TestApiKeys.PAYMENTS_GATEWAY_KEY;
import static com.github.jnaalisv.sf_fds.http.TestApiKeys.apiKey;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		TestApiKeys.PROPERTIES_PAYMENTS_GATEWAY_CLIENT, TestApiKeys.PROPERTIES_PAYMENTS_GATEWAY_HASH,
		TestApiKeys.PROPERTIES_CARD_ISSUER_CLIENT, TestApiKeys.PROPERTIES_CARD_ISSUER_HASH})
class ApiKeyAuthenticationTests {

	private static final String DETECT_FRAUDS_URL = "/api/v1/detect-frauds";

	private static final String VALID_REQUEST = """
			{
			  "amount": 125.50,
			  "currency": "EUR",
			  "terminalId": "TERM-AUTH",
			  "terminalThreatScore": 42,
			  "cardNumber": "4111111111111111"
			}
			""";

	private static final String UNAUTHORIZED_RESPONSE = """
			{
			  "error": "unauthorized",
			  "message": "missing or invalid API key"
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InMemoryTerminalRepository terminalRepository;

	@MockitoBean
	private FraudDetectionParametersRepository fraudDetectionParametersRepository;

	@MockitoBean
	private InstantSource instantSource;

	@BeforeEach
	void setUp() {
		when(fraudDetectionParametersRepository.load())
				.thenReturn(new FraudDetectionParameters(Duration.ofMinutes(5), 1_000));
		when(instantSource.instant()).thenReturn(Instant.parse("2026-11-11T12:00:00Z"));
		terminalRepository.add(new Terminal("TERM-AUTH", "ES"));
	}

	@Test
	void requestWithoutApiKeyIsUnauthorized() throws Exception {
		expectUnauthorized(perform(validRequest()));
	}

	@Test
	void requestWithEmptyApiKeyIsUnauthorized() throws Exception {
		expectUnauthorized(perform(validRequest().with(apiKey(""))));
	}

	@Test
	void requestWithUnknownApiKeyIsUnauthorized() throws Exception {
		expectUnauthorized(perform(validRequest().with(apiKey("not-a-configured-key"))));
	}

	@Test
	void localDevelopmentKeyIsRejectedWhenKeysAreOverridden() throws Exception {
		expectUnauthorized(perform(validRequest().with(apiKey("local-dev-api-key-do-not-use-in-production"))));
	}

	@Test
	void apiKeyIsCaseSensitive() throws Exception {
		expectUnauthorized(perform(validRequest().with(apiKey(PAYMENTS_GATEWAY_KEY.toUpperCase()))));
	}

	@Test
	void eachConfiguredCallerIsAccepted() throws Exception {
		for (String key : new String[] {PAYMENTS_GATEWAY_KEY, CARD_ISSUER_KEY}) {
			perform(validRequest().with(apiKey(key)))
					.andExpect(status().isOk())
					.andExpect(content().json("""
							{ "status": "OK", "message": "OK", "fraudScore": 0 }
							""", JsonCompareMode.STRICT));
		}
	}

	@Test
	void authenticationIsCheckedBeforeTheBodyIsParsed() throws Exception {
		expectUnauthorized(perform(post(DETECT_FRAUDS_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"amount\": ")));
	}

	@Test
	void unauthenticatedGetIsUnauthorizedRatherThanMethodNotAllowed() throws Exception {
		expectUnauthorized(mockMvc.perform(
				get(DETECT_FRAUDS_URL)));
	}

	@Test
	void unknownPathWithoutApiKeyIsUnauthorized() throws Exception {
		expectUnauthorized(perform(post("/api/v1/unknown")));
	}

	private MockHttpServletRequestBuilder validRequest() {
		return post(DETECT_FRAUDS_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(VALID_REQUEST);
	}

	private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request);
	}

	private static void expectUnauthorized(ResultActions result) throws Exception {
		result.andExpect(status().isUnauthorized())
				.andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "X-API-Key"))
				.andExpect(content().json(UNAUTHORIZED_RESPONSE, JsonCompareMode.STRICT));
	}
}
