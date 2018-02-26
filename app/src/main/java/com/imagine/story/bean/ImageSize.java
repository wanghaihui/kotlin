package com.imagine.story.bean;

/**
 * Created by conquer on 2018/2/6.
 *
 */

public class ImageSize {
    private int mWidth;
    private int mHeight;

    public ImageSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void setWidth(int width) {
        mWidth = width;
    }
    public int getWidth() {
        return mWidth;
    }

    public void setHeight(int height) {
        mHeight = height;
    }
    public int getHeight() {
        return mHeight;
    }
}
