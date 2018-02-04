package com.imagine.story.feed;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by conquer on 2018/2/4.
 *
 */

public class FeedViewHolderFactory {

    public static Class<? extends FeedViewHolderBase> getViewHolderByType(Feed feed) {
        if (feed.getFeedType() == FeedTypeEnum.image) {
            return FeedViewHolderImage.class;
        } else {
            return FeedViewHolderUnknown.class;
        }
    }

    public static List<Class<? extends FeedViewHolderBase>> getAllViewHolders() {
        List<Class<? extends FeedViewHolderBase>> list = new ArrayList<>();
        list.add(FeedViewHolderUnknown.class);
        list.add(FeedViewHolderImage.class);
        return list;
    }
}
