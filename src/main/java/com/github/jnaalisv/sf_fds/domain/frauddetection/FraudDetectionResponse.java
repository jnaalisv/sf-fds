package com.github.jnaalisv.sf_fds.domain.frauddetection;

public record FraudDetectionResponse(
        String status,
        String message,
        int fraudScore) {

    public FraudDetectionResponse {

    }
}
