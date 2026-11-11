package com.github.jnaalisv.sf_fds.domain.terminals;

import org.jspecify.annotations.Nullable;

public interface TerminalRepository {

    @Nullable Terminal findById(String id);
}
