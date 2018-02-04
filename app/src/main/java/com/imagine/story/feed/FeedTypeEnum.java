package com.imagine.story.feed;

/**
 * Created by meijian on 2018/2/4.
 */

public enum  FeedTypeEnum {
    text(0),
    image(1);

    private final int value;

    FeedTypeEnum(int value) {
        this.value = value;
    }

    public final int getValue() {
        return this.value;
    }
}
