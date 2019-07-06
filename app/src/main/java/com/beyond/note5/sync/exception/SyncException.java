package com.beyond.note5.sync.exception;

public class SyncException extends Exception {
    private int failIndex;

    public SyncException(Throwable cause, int failIndex) {
        super(cause);
        this.failIndex = failIndex;
    }

    public int getFailIndex() {
        return failIndex;
    }

    public void setFailIndex(int failIndex) {
        this.failIndex = failIndex;
    }
}
