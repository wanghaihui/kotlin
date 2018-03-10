package com.imagine.story.feed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.imagine.story.R;
import com.imagine.story.activity.ShortVideoActivity;
import com.imagine.story.common.base.BaseAdapter;
import com.imagine.story.common.glide.GlideApp;
import com.imagine.story.common.utils.ScreenUtils;

/**
 * Created by conquer on 2018/2/3.
 *
 */

public class FeedViewHolderImage extends FeedViewHolderBase {
    private static final int VALUE_KEY_NOT_FOUND = -1;

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
    protected void bindContentView(int position) {
        // 瀑布流实现
        if (((FeedAdapter) getAdapter()).getHeightCache().get(position, VALUE_KEY_NOT_FOUND) == VALUE_KEY_NOT_FOUND) {
            GlideApp.with(context)
                    .asBitmap()
                    .load(feed.getImageUrl())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                            float scale = (float) bitmap.getHeight() / (float) bitmap.getWidth();
                            int height = (int) (ScreenUtils.getFeedImageWidthPixels(context) * scale);
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) feedImage.getLayoutParams();
                            layoutParams.height = height;
                            feedImage.setLayoutParams(layoutParams);
                            ((FeedAdapter) getAdapter()).getHeightCache().put(position, height);
                        }
                    });
        } else {
            int height = ((FeedAdapter) getAdapter()).getHeightCache().get(position);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) feedImage.getLayoutParams();
            layoutParams.height = height;
            feedImage.setLayoutParams(layoutParams);
        }

        GlideApp.with(context)
                .load(feed.getImageUrl())
                .into(feedImage);

        feedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShortVideoActivity.class);
                context.startActivity(intent);
            }
        });
    }
}
