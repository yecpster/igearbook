package com.igearbook.entities;

import java.io.Serializable;

public class ImageVo implements Serializable {
    private static final long serialVersionUID = -3847321538691386074L;
    private String url;
    private int width;
    private int height;

    public ImageVo() {
    }

    public ImageVo(final String url, final int width, final int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(final int height) {
        this.height = height;
    }

}
