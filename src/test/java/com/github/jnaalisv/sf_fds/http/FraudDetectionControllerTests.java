package com.github.jnaalisv.sf_fds.http;

import com.github.jnaalisv.sf_fds.persistence.InMemoryTerminalRepository;
import com.github.jnaalisv.sf_fds.services.Terminal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

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

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private InMemoryTerminalRepository terminalRepository;

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
