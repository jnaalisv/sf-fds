package com.github.jnaalisv.sf_fds.services;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class FraudDetectionRequestTests {

	@ParameterizedTest
	@ValueSource(ints = {0, 100})
	void terminalThreatScoreAtBoundariesIsAccepted(int score) {
		FraudDetectionRequest request = requestWithThreatScore(score);

		assertThat(request.terminalThreatScore()).isEqualTo(score);
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, 101})
	void terminalThreatScoreOutOfRangeIsRejected(int score) {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> requestWithThreatScore(score))
				.withMessage("terminalThreatScore must be between 0 and 100");
	}

	private static FraudDetectionRequest requestWithThreatScore(int score) {
		return new FraudDetectionRequest(new BigDecimal("125.50"), "EUR", "TERM-001", score, "4111111111111111");
	}

}
