package com.github.jnaalisv.sf_fds.services;

import org.springframework.stereotype.Service;

@Service
public class FraudDetectionService {

    public FraudDetectionResponse detectFraud(FraudDetectionRequest fraudDetectionRequest) {
        return new FraudDetectionResponse("OK", "OK", 0);
    }
}
