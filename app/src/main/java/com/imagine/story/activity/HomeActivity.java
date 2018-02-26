package com.imagine.story.activity;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.imagine.story.R;
import com.imagine.story.feed.FeedAdapter;
import com.imagine.story.feed.Feed;
import com.imagine.story.common.base.BaseActivity;
import com.imagine.story.common.config.Config;
import com.imagine.story.feed.FeedTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends BaseActivity {
    private static final String TAG = HomeActivity.class.getSimpleName();

    @BindView(R.id.drawer)
    DrawerLayout drawer;

    @BindView(R.id.feed_recycler_view)
    RecyclerView feedRecyclerView;
    StaggeredGridLayoutManager layoutManager;
    FeedAdapter feedAdapter;

    private List<Feed> feeds;

    @Override
    protected void initViews() {
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        // android 3.0开始提供
        // 对状态栏的动态显示或隐藏的操作
        // SYSTEM_UI_FLAG_LAYOUT_STABLE--保持Layout不变--布局稳定
        // SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION--底部导航栏
        // SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN--隐藏状态栏
        drawer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        loadData();

        feedAdapter = new FeedAdapter(feedRecyclerView, feeds);
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        feedRecyclerView.setLayoutManager(layoutManager);

        feedRecyclerView.setAdapter(feedAdapter);
    }

    private void loadData() {
        feeds = new ArrayList<>();
        // 绿
        for (int i = 0; i < 11; i++) {
            feeds.add(new Feed(UUID.randomUUID().toString(), FeedTypeEnum.image, getUrl(i + 1)));
        }
        // 黄
        for (int i = 11; i < 24; i++) {
            feeds.add(new Feed(UUID.randomUUID().toString(), FeedTypeEnum.image, getUrl(i + 1)));
        }
    }

    private String getUrl(int prefix) {
        return Config.BASE_IMAGE_URL + prefix + Config.IMAGE_URL_SUFFIX;
    }

}
