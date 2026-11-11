package com.github.jnaalisv.sf_fds.domain.frauddetection;

import com.github.jnaalisv.sf_fds.domain.parameters.FraudDetectionParameters;
import com.github.jnaalisv.sf_fds.domain.terminals.Terminal;

record FraudDetectionContext(
        FraudDetectionParameters parameters,
        Terminal terminal
        /* Card card, etc */) {
}