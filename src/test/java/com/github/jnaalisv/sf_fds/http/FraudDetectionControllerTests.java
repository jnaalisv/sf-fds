package com.github.jnaalisv.sf_fds.http;

import com.github.jnaalisv.sf_fds.persistence.InMemoryTerminalRepository;
import com.github.jnaalisv.sf_fds.domain.parameters.FraudDetectionParameters;
import com.github.jnaalisv.sf_fds.domain.parameters.FraudDetectionParametersRepository;
import com.github.jnaalisv.sf_fds.domain.terminals.Terminal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FraudDetectionControllerTests {

	private static final String DETECT_FRAUDS_URL = "/api/v1/detect-frauds";
	private static final Terminal terminal1 = new Terminal("TERM-001", "ES");

	private static final String VALID_REQUEST = """
			{
			  "amount": 125.50,
			  "currency": "EUR",
			  "terminalId": "TERM-001",
			  "terminalThreatScore": 42,
			  "cardNumber": "4111111111111111"
			}
			""";

	private static final String OK_RESPONSE = """
			{
			  "status": "OK",
			  "message": "OK",
			  "fraudScore": 0
			}
			""";

	private static final String TOO_MANY_TRANSACTIONS_RESPONSE = """
			{
			  "status": "DENIED",
			  "message": "Too many transactions",
			  "fraudScore": 99
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

	private Instant now = Instant.parse("2026-11-11T12:00:00Z");

	@BeforeEach
	void setUp() {
		when(fraudDetectionParametersRepository.load())
				.thenReturn(new FraudDetectionParameters(Duration.ofMinutes(5), 100));
		when(instantSource.instant()).thenAnswer(invocation -> now);
	}

	@Test
	void transactionsUpToLimitAreAccepted() throws Exception {
		Terminal terminal = registerTerminal("TERM-LIMIT-OK");
		when(fraudDetectionParametersRepository.load())
				.thenReturn(new FraudDetectionParameters(Duration.ofMinutes(5), 3));

		for (int i = 0; i < 3; i++) {
			detectFraudOn(terminal)
					.andExpect(status().isOk())
					.andExpect(content().json(OK_RESPONSE, JsonCompareMode.STRICT));
		}
	}

	@Test
	void transactionOverLimitIsDenied() throws Exception {
		Terminal terminal = registerTerminal("TERM-LIMIT-EXCEEDED");
		when(fraudDetectionParametersRepository.load())
				.thenReturn(new FraudDetectionParameters(Duration.ofMinutes(5), 3));

		for (int i = 0; i < 3; i++) {
			detectFraudOn(terminal).andExpect(status().isOk());
		}

		detectFraudOn(terminal)
				.andExpect(status().isOk())
				.andExpect(content().json(TOO_MANY_TRANSACTIONS_RESPONSE, JsonCompareMode.STRICT));
	}

	@Test
	void transactionsOutsideWindowAreNotCounted() throws Exception {
		Terminal terminal = registerTerminal("TERM-WINDOW-EXPIRED");
		when(fraudDetectionParametersRepository.load())
				.thenReturn(new FraudDetectionParameters(Duration.ofMinutes(5), 3));

		detectFraudOn(terminal).andExpect(status().isOk());
		now = now.plus(Duration.ofMinutes(1));
		detectFraudOn(terminal).andExpect(status().isOk());

		now = now.plus(Duration.ofMinutes(4)).plusSeconds(30);
		detectFraudOn(terminal)
				.andExpect(status().isOk())
				.andExpect(content().json(OK_RESPONSE, JsonCompareMode.STRICT));
	}

	@Test
	void transactionsInsideSlidingWindowAreCounted() throws Exception {
		Terminal terminal = registerTerminal("TERM-WINDOW-SLIDING");
		when(fraudDetectionParametersRepository.load())
				.thenReturn(new FraudDetectionParameters(Duration.ofMinutes(5), 2));

		detectFraudOn(terminal).andExpect(status().isOk());
		now = now.plus(Duration.ofMinutes(2));
		detectFraudOn(terminal).andExpect(status().isOk());
		now = now.plus(Duration.ofMinutes(2));

		detectFraudOn(terminal)
				.andExpect(status().isOk())
				.andExpect(content().json(TOO_MANY_TRANSACTIONS_RESPONSE, JsonCompareMode.STRICT));
	}

	@Test
	void transactionLimitIsCountedPerTerminal() throws Exception {
		Terminal busyTerminal = registerTerminal("TERM-BUSY");
		Terminal quietTerminal = registerTerminal("TERM-QUIET");
		when(fraudDetectionParametersRepository.load())
				.thenReturn(new FraudDetectionParameters(Duration.ofMinutes(5), 1));

		detectFraudOn(busyTerminal).andExpect(status().isOk());

		detectFraudOn(quietTerminal)
				.andExpect(status().isOk())
				.andExpect(content().json(OK_RESPONSE, JsonCompareMode.STRICT));
	}

	private Terminal registerTerminal(String id) {
		Terminal terminal = new Terminal(id, "ES");
		terminalRepository.add(terminal);
		return terminal;
	}

	private ResultActions detectFraudOn(Terminal terminal) throws Exception {
		return mockMvc.perform(post(DETECT_FRAUDS_URL)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "amount": 125.50,
						  "currency": "EUR",
						  "terminalId": "%s",
						  "terminalThreatScore": 42,
						  "cardNumber": "4111111111111111"
						}
						""".formatted(terminal.id())));
	}

	@Test
	void validRequestReturnsFraudDetectionResponse() throws Exception {
		terminalRepository.add(terminal1);

		mockMvc.perform(post(DETECT_FRAUDS_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content(VALID_REQUEST))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json("""
						{
						  "status": "OK",
						  "message": "OK",
						  "fraudScore": 0
						}
						""", JsonCompareMode.STRICT));
	}

	@Test
	void missingTerminalRequestReturnsFraudDetectionResponse() throws Exception {
		terminalRepository.add(terminal1);

		mockMvc.perform(post(DETECT_FRAUDS_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)
						.content("""
				{
				  "amount": 125.50,
				  "currency": "EUR",
				  "terminalId": "TERM-002",
				  "terminalThreatScore": 42,
				  "cardNumber": "4111111111111111"
				}"""))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(content().json("""
						{
						  "status": "DENIED",
						  "message": "unknown terminal",
						  "fraudScore": 99
						}
						""", JsonCompareMode.STRICT));
	}

	@Test
	void missingRequiredFieldIsRejected() throws Exception {
		mockMvc.perform(post(DETECT_FRAUDS_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "amount": 125.50,
								  "currency": "EUR",
								  "terminalId": "TERM-001",
								  "terminalThreatScore": 42
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void malformedJsonIsRejected() throws Exception {
		mockMvc.perform(post(DETECT_FRAUDS_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"amount\": "))
				.andExpect(status().isBadRequest());
	}

	@Test
	void emptyBodyIsRejected() throws Exception {
		mockMvc.perform(post(DETECT_FRAUDS_URL)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	void nonJsonContentTypeIsRejected() throws Exception {
		mockMvc.perform(post(DETECT_FRAUDS_URL)
						.contentType(MediaType.TEXT_PLAIN)
						.content(VALID_REQUEST))
				.andExpect(status().isUnsupportedMediaType());
	}

	@Test
	void nonJsonAcceptHeaderIsRejected() throws Exception {
		mockMvc.perform(post(DETECT_FRAUDS_URL)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_XML)
						.content(VALID_REQUEST))
				.andExpect(status().isNotAcceptable());
	}

	@Test
	void getIsNotAllowed() throws Exception {
		mockMvc.perform(get(DETECT_FRAUDS_URL))
				.andExpect(status().isMethodNotAllowed());
	}
}
