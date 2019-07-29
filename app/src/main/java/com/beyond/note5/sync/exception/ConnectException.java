package com.beyond.note5.sync.exception;

/**
 * @author: beyond
 * @date: 2019/7/28
 */

public class ConnectException extends Exception {

    private String key;

    public ConnectException(String message, String key) {
        super(message);
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
