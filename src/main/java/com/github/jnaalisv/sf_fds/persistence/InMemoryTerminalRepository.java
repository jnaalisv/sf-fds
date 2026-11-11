package com.github.jnaalisv.sf_fds.persistence;

import com.github.jnaalisv.sf_fds.domain.terminals.Terminal;
import com.github.jnaalisv.sf_fds.domain.terminals.TerminalRepository;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTerminalRepository implements TerminalRepository {

    private final Map<String, Terminal> terminals = new ConcurrentHashMap<>();

    public InMemoryTerminalRepository(Collection<Terminal> initialTerminals) {
        for (Terminal terminal : initialTerminals) {
            if (terminal.id() == null || terminal.id().isBlank()) {
                throw new IllegalStateException("Terminal entry without an id");
            }
            if (terminals.putIfAbsent(terminal.id(), terminal) != null) {
                throw new IllegalStateException("Terminal '" + terminal.id() + "' configured more than once");
            }
        }
    }

    public void add(Terminal terminal) {
        terminals.put(terminal.id(), terminal);
    }

    @Override
    public @Nullable Terminal findById(String id) {
        return terminals.get(id);
    }
}
