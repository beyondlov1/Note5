package com.beyond.note5.sync.exception;

import java.util.List;

public class SaveException extends Exception {

    private String key;
    private  List<String>  successIds;

    public SaveException(Throwable cause, String key, List<String> successIds) {
        super(cause);
        this.key = key;
        this.successIds = successIds;
    }

    public List<String> getSuccessIds() {
        return successIds;
    }

    public String getKey() {
        return key;
    }
}
