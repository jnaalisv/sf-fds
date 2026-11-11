package com.github.jnaalisv.sf_fds.domain.frauddetection;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public record FraudDetectionRequest(
        BigDecimal amount,
        String currency,
        String terminalId,
        int terminalThreatScore,
        String cardNumber) {

    public FraudDetectionRequest {
        requireNonNull(amount, "amount must not be null");
        requireNonNull(currency, "currency must not be null");
        requireNonNull(terminalId, "terminalId must not be null");
        requireNonNull(cardNumber, "cardNumber must not be null");
        if (terminalThreatScore < 0 || terminalThreatScore > 100) {
            throw new IllegalArgumentException("terminalThreatScore must be between 0 and 100");
        }
    }
}
