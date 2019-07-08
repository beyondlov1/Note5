package com.beyond.note5.view.custom;

public class BitmapInfo {
    private String filePath;
    private Integer width;
    private Integer height;

    public BitmapInfo(String filePath, Integer width, Integer height) {
        this.filePath = filePath;
        this.width = width;
        this.height = height;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
