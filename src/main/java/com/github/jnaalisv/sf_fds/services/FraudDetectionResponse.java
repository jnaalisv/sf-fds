package com.github.jnaalisv.sf_fds.services;

import java.util.Objects;

public record FraudDetectionResponse(
        String status,
        String message,
        int fraudScore) {

    public FraudDetectionResponse {

    }
}
