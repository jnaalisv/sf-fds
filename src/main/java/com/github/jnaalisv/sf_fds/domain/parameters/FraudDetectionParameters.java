package com.github.jnaalisv.sf_fds.domain.parameters;

import java.time.Duration;

public record FraudDetectionParameters(
        Duration transactionWindow,
        int transactionLimitPerTerminal){
}
