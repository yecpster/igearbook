package com.igearbook.constant;

public enum ImageSize {
    ORIGINAL(0, 0, 0), LOGO(1000, 80, 80), SIZE_480(1, 480, 320), SIZE_800(2, 800, 600);

    private final int sizeNum;
    private final int width;
    private final int height;

    private ImageSize(final int sizeNum, final int width, final int height) {
        this.sizeNum = sizeNum;
        this.width = width;
        this.height = height;
    }

    public int getSizeNum() {
        return sizeNum;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
