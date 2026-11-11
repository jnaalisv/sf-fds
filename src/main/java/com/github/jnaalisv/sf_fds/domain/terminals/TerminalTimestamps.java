package com.github.jnaalisv.sf_fds.domain.terminals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record TerminalTimestamps(
        String terminalId,
        List<Instant> timestamps) {

    public static TerminalTimestamps of(String terminalId) {
        return new TerminalTimestamps(terminalId, new ArrayList<>());
    }

    public synchronized void registerTransaction(Instant timestamp, int maxTimestamps) {
        if (timestamps.size() > maxTimestamps) {
            timestamps.removeFirst(); // to keep the list from growing infinitely
        }
        timestamps.add(timestamp);
    }

    public synchronized int transactionsSince(Instant cutOff) {
        timestamps.removeIf(t -> !t.isAfter(cutOff));
        return timestamps.size();
    }

}
