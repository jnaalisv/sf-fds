package com.github.jnaalisv.sf_fds.services;

import org.springframework.stereotype.Service;

@Service
public class FraudDetectionService {

    private final TerminalRepository terminalRepository;

    public FraudDetectionService(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
    }

    public FraudDetectionResponse detectFraud(FraudDetectionRequest fraudDetectionRequest) {
        Terminal terminal = terminalRepository.findById(fraudDetectionRequest.terminalId());
        if (terminal == null) {
            return new FraudDetectionResponse("DENIED", "unknown terminal", 99);
        }
        return new FraudDetectionResponse("OK", "OK", 0);
    }
}
