package com.beyond.note5.sync.model;

/**
 * @author: beyond
 * @date: 2019/8/2
 */

public enum SyncState {
    SUCCESS(1),FAIL(0),NULL(null);

    SyncState(Integer value) {
        this.value = value;
    }

    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
