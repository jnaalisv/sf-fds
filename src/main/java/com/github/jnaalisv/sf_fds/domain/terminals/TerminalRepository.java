package com.github.jnaalisv.sf_fds.domain.terminals;

import org.jspecify.annotations.Nullable;

public interface TerminalRepository {

    // TODO: candidate for caching
    @Nullable Terminal findById(String id);
}
