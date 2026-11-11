package com.github.jnaalisv.sf_fds.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TerminalProperties.class)
public class PersistenceConfig {

    private static final Logger log = LoggerFactory.getLogger(PersistenceConfig.class);

    @Bean
    InMemoryTerminalRepository terminalRepository(TerminalProperties properties) {
        log.info("Loaded {} terminal(s) from fds.terminals", properties.terminals().size());
        return new InMemoryTerminalRepository(properties.terminals());
    }
}
