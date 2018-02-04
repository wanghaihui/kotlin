package com.imagine.story.feed;

/**
 * Created by conquer on 2018/2/4.
 *
 * Feed数据结构
 */

public class Feed {
    private String feedId;
    private FeedTypeEnum feedType;
    // 图片url
    private String imageUrl;

    public Feed(String feedId, FeedTypeEnum feedType, String imageUrl) {
        this.feedId = feedId;
        this.feedType = feedType;
        this.imageUrl = imageUrl;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }
    public String getFeedId() {
        return feedId;
    }

    public void setFeedType(FeedTypeEnum feedType) {
        this.feedType = feedType;
    }
    public FeedTypeEnum getFeedType() {
        return feedType;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getImageUrl() {
        return imageUrl;
    }
}

