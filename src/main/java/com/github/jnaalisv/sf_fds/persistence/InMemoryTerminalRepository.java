package com.github.jnaalisv.sf_fds.persistence;

import com.github.jnaalisv.sf_fds.services.Terminal;
import com.github.jnaalisv.sf_fds.services.TerminalRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTerminalRepository implements TerminalRepository {

    private final Map<String, Terminal> terminals = new ConcurrentHashMap<>();

    public InMemoryTerminalRepository(Map<String, Terminal> terminals) {

    }

    public void add(Terminal terminal) {
        terminals.put(terminal.id(), terminal);
    }

    @Override
    public @Nullable Terminal findById(String id) {
        return terminals.get(id);
    }
}
