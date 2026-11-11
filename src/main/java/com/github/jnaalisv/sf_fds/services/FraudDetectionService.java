package com.github.jnaalisv.sf_fds.services;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;

@Service
public class FraudDetectionService {

    private final TerminalRepository terminalRepository;
    private final TerminalTransactionCounter terminalTransactionCounter;
    private final FraudDetectionParametersRepository fraudDetectionParametersRepository;

    public FraudDetectionService(TerminalRepository terminalRepository, TerminalTransactionCounter terminalTransactionCounter, FraudDetectionParametersRepository fraudDetectionParametersRepository) {
        this.terminalRepository = terminalRepository;
        this.terminalTransactionCounter = terminalTransactionCounter;
        this.fraudDetectionParametersRepository = fraudDetectionParametersRepository;
    }

    public FraudDetectionResponse detectFraud(FraudDetectionRequest fraudDetectionRequest) {
        FraudDetectionParameters fraudDetectionParameters = fraudDetectionParametersRepository.load();
        Terminal terminal = terminalRepository.findById(fraudDetectionRequest.terminalId());
        if (terminal == null) {
            return new FraudDetectionResponse("DENIED", "unknown terminal", 99);
        }
        int transactionCount = terminalTransactionCounter.terminalTransactionsIn(terminal.id(), fraudDetectionParameters);
        if (transactionCount > fraudDetectionParameters.transactionLimitPerTerminal()) {
            return new FraudDetectionResponse("DENIED", "Too many transactions", 99);
        }
        return new FraudDetectionResponse("OK", "OK", 0);
    }
}
