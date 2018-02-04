package com.imagine.story.feed;

import android.widget.ImageView;

import com.imagine.story.R;
import com.imagine.story.adapter.BaseAdapter;
import com.imagine.story.common.glide.GlideApp;

/**
 * Created by conquer on 2018/2/3.
 *
 */

public class FeedViewHolderImage extends FeedViewHolderBase {

    private ImageView feedImage;

    public FeedViewHolderImage(BaseAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.feed_item_image;
    }

    @Override
    protected void inflateContentView() {
        feedImage = view.findViewById(R.id.feed_image);
    }

    @Override
    protected void bindContentView() {
        GlideApp.with(context)
                .load(feed.getImageUrl())
                .into(feedImage);
    }
}
