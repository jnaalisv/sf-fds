package com.github.jnaalisv.sf_fds.services;

import org.jspecify.annotations.Nullable;

public interface TerminalRepository {

    @Nullable Terminal findById(String id);
}
