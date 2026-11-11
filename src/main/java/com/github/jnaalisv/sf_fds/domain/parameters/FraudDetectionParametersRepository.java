package com.github.jnaalisv.sf_fds.domain.parameters;

public interface FraudDetectionParametersRepository {

    // TODO: candidate for caching
    FraudDetectionParameters load();
}
