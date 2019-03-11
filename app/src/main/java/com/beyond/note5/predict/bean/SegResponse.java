package com.beyond.note5.predict.bean;

/**
 * 分词响应
 * @author beyondlov1
 * @date 2019/03/10
 */
public class SegResponse {
    private String status;
    private String input;
    private String[][] result;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String[][] getResult() {
        return result;
    }

    public void setResult(String[][] result) {
        this.result = result;
    }
}
