package com.github.jnaalisv.sf_fds.persistence;

import com.github.jnaalisv.sf_fds.domain.parameters.FraudDetectionParameters;
import com.github.jnaalisv.sf_fds.domain.parameters.FraudDetectionParametersRepository;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class InMemoryFraudDetectionParametersRepository implements FraudDetectionParametersRepository {

    @Override
    public FraudDetectionParameters load() {
        return new FraudDetectionParameters(
                Duration.ofMinutes(5),
                100
        );
    }
}
