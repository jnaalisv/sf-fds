package com.github.jnaalisv.sf_fds.http;

import com.github.jnaalisv.sf_fds.services.FraudDetectionRequest;
import com.github.jnaalisv.sf_fds.services.FraudDetectionResponse;
import com.github.jnaalisv.sf_fds.services.FraudDetectionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FraudDetectionController {
    private final FraudDetectionService fraudDetectionService;

    public FraudDetectionController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @PostMapping(
            value = "/api/v1/detect-frauds",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FraudDetectionResponse detectFraud(@RequestBody FraudDetectionRequest fraudDetectionRequest) {
        return fraudDetectionService.detectFraud(fraudDetectionRequest);
    }
}
