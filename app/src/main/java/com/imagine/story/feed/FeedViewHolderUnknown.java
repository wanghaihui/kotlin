package com.imagine.story.feed;

import com.imagine.story.R;
import com.imagine.story.common.base.BaseAdapter;

/**
 * Created by conquer on 2018/2/4.
 *
 */

public class FeedViewHolderUnknown extends FeedViewHolderBase {

    public FeedViewHolderUnknown(BaseAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.feed_item_unknown;
    }

    @Override
    protected void inflateContentView() {

    }

    @Override
    protected void bindContentView(int position) {

    }
}
