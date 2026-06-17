package com.alextim.lora.client.ble.constants;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionStats {

    private final AtomicLong connectionStartTime = new AtomicLong(0);
    private final AtomicInteger sentMessagesCounter = new AtomicInteger(0);
    private final AtomicInteger receivedMessageCounter = new AtomicInteger(0);
    private final AtomicInteger errorCounter = new AtomicInteger(0);
    private final AtomicInteger currentRssi = new AtomicInteger(0);

    public void reset() {
        connectionStartTime.set(0);
        sentMessagesCounter.set(0);
        receivedMessageCounter.set(0);
        errorCounter.set(0);
        currentRssi.set(0);
    }

    public void setConnectionStartTime(long time) {
        connectionStartTime.set(time);
    }

    public long getConnectionStartTime() {
        return connectionStartTime.get();
    }

    public long getConnectionDurationSec() {
        long start = connectionStartTime.get();
        return start > 0 ? (System.currentTimeMillis() - start) / 1000 : 0;
    }

    public void incrementSentMessagesCounter() {
        sentMessagesCounter.incrementAndGet();
    }

    public int getSentMessagesCounter() {
        return sentMessagesCounter.get();
    }

    public void incrementReceivedMessagesCounter() {
        receivedMessageCounter.incrementAndGet();
    }

    public int getReceivedMessageCounter() {
        return receivedMessageCounter.get();
    }

    public void incrementErrorsCounter() {
        errorCounter.incrementAndGet();
    }

    public int getErrorsCounter() {
        return errorCounter.get();
    }

    public void setCurrentRssi(int rssi) {
        currentRssi.set(rssi);
    }

    public int getCurrentRssi() {
        return currentRssi.get();
    }
}