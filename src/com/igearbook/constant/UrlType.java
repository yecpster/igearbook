package com.igearbook.constant;

public enum UrlType {
    Team(1), Blog(2);
    private final int type;

    private UrlType(final int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
