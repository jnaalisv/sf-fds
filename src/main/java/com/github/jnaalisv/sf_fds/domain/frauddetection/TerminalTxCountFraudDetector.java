package com.github.jnaalisv.sf_fds.domain.frauddetection;

import com.github.jnaalisv.sf_fds.domain.parameters.FraudDetectionParametersRepository;
import com.github.jnaalisv.sf_fds.domain.terminals.TerminalRepository;
import com.github.jnaalisv.sf_fds.domain.terminals.TerminalTransactionCounter;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class TerminalTxCountFraudDetector {

    private final TerminalTransactionCounter terminalTransactionCounter;

    public TerminalTxCountFraudDetector(TerminalRepository terminalRepository, TerminalTransactionCounter terminalTransactionCounter, FraudDetectionParametersRepository fraudDetectionParametersRepository) {
        this.terminalTransactionCounter = terminalTransactionCounter;
    }

    @Nullable FraudDetectionResponse checkTerminalTransactionCounts(FraudDetectionContext ctx) {
        int transactionCount = terminalTransactionCounter.terminalTransactionsIn(ctx.terminal().id(), ctx.parameters());
        if (transactionCount > ctx.parameters().transactionLimitPerTerminal()) {
            return FraudDetectionResponse.denied("Too many transactions");
        }
        return null;
    }

}
