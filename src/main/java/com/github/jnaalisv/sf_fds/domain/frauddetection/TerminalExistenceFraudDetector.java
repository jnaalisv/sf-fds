package com.github.jnaalisv.sf_fds.domain.frauddetection;

import org.jspecify.annotations.Nullable;


public class TerminalExistenceFraudDetector {

    public static @Nullable FraudDetectionResponse checkTerminalExistence(FraudDetectionContext ctx) {
        return ctx.terminal() == null ? FraudDetectionResponse.denied("unknown terminal") : null;
    }
}
