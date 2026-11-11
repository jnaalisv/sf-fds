package com.github.jnaalisv.sf_fds.domain.terminals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TerminalTimestamps {

    private final List<Instant> timestamps;

    public TerminalTimestamps(List<Instant> timestamps) {
        this.timestamps =  timestamps;
    }
    
    public synchronized int registerTxAndCountTxsSince(Instant txTimestamp, int maxTimestamps, Instant cutOff) {
        if (timestamps.size() > maxTimestamps) {
            timestamps.removeFirst(); // to keep the list from growing infinitely
        }
        timestamps.add(txTimestamp);
        timestamps.removeIf(t -> !t.isAfter(cutOff));
        return timestamps.size();
    }
}
