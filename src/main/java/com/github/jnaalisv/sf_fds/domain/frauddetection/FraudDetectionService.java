package com.github.jnaalisv.sf_fds.domain.frauddetection;

import com.github.jnaalisv.sf_fds.domain.parameters.FraudDetectionParameters;
import com.github.jnaalisv.sf_fds.domain.parameters.FraudDetectionParametersRepository;
import com.github.jnaalisv.sf_fds.domain.terminals.Terminal;
import com.github.jnaalisv.sf_fds.domain.terminals.TerminalRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class FraudDetectionService {

    private final TerminalRepository terminalRepository;
    private final TerminalTxCountFraudDetector terminalTxCountFraudDetector;
    private final FraudDetectionParametersRepository fraudDetectionParametersRepository;

    public FraudDetectionService(TerminalRepository terminalRepository, TerminalTxCountFraudDetector terminalTxCountFraudDetector, FraudDetectionParametersRepository fraudDetectionParametersRepository) {
        this.terminalRepository = terminalRepository;
        this.terminalTxCountFraudDetector = terminalTxCountFraudDetector;
        this.fraudDetectionParametersRepository = fraudDetectionParametersRepository;
    }

    public FraudDetectionResponse detectFraud(FraudDetectionRequest fraudDetectionRequest) {
        FraudDetectionParameters parameters = fraudDetectionParametersRepository.load();
        Terminal terminal = terminalRepository.findById(fraudDetectionRequest.terminalId());
        var context = new FraudDetectionContext(parameters, terminal);
        return runFraudDetections(context,
                TerminalExistenceFraudDetector::checkTerminalExistence,
                terminalTxCountFraudDetector::checkTerminalTransactionCounts
                // other possible fraud detections
        );
    }

    FraudDetectionResponse runFraudDetections(FraudDetectionContext context, FraudDetector...fraudDetectors) {
        for (FraudDetector fraudDetector : fraudDetectors) {
            var response = fraudDetector.detectFraud(context);
            if (response != null) {
                return response;
            }
        }
        return new FraudDetectionResponse("OK", "OK", 0);
    }

    @FunctionalInterface
    interface FraudDetector {
        @Nullable FraudDetectionResponse detectFraud(FraudDetectionContext ctx);
    }
}
