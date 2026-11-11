package com.github.jnaalisv.sf_fds.services;

import java.time.Duration;

public record FraudDetectionParameters(
        Duration transactionWindow,
        int transactionLimitPerTerminal){
}
