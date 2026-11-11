package com.github.jnaalisv.sf_fds.services;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TerminalTransactionCounter {

    private final InstantSource instantSource;
    private final Map<String, TerminalTimestamps> terminalTimestampsByTerminalId = new ConcurrentHashMap<>();

    public TerminalTransactionCounter(InstantSource instantSource) {
        this.instantSource = instantSource;
    }

    public int terminalTransactionsIn(String terminalId, FraudDetectionParameters fraudDetectionParameters) {
        TerminalTimestamps terminalTimestamps = terminalTimestampsByTerminalId.computeIfAbsent(terminalId, k -> TerminalTimestamps.of(terminalId));
        terminalTimestamps.registerTransaction(instantSource.instant(), fraudDetectionParameters.transactionLimitPerTerminal());
        Instant cutOff = instantSource.instant().minus(fraudDetectionParameters.transactionWindow());
        return terminalTimestamps.transactionsSince(cutOff);
    }
}
