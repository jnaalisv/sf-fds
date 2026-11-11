package com.github.jnaalisv.sf_fds.domain.frauddetection;

import com.github.jnaalisv.sf_fds.domain.parameters.FraudDetectionParameters;
import com.github.jnaalisv.sf_fds.domain.terminals.Terminal;

public record FraudDetectionContext(
        FraudDetectionParameters parameters,
        Terminal terminal
        /* Card card, etc */) {
}