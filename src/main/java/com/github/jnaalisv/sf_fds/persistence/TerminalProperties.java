package com.github.jnaalisv.sf_fds.persistence;

import com.github.jnaalisv.sf_fds.domain.terminals.Terminal;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Terminals loaded into the in-memory terminal repository at startup ({@code fds.terminals}).
 * An empty list is allowed: the app then knows no terminals and denies every request.
 */
@ConfigurationProperties("fds")
public record TerminalProperties(List<Terminal> terminals) {

    public TerminalProperties {
        terminals = terminals == null ? List.of() : List.copyOf(terminals);
    }
}
