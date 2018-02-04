package com.imagine.story.activity;

import com.imagine.story.R;
import com.imagine.story.common.base.BaseActivity;
import com.imagine.story.common.view.ScrollingImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by conquer on 2018/1/23.
 *
 */

public class LoginActivity extends BaseActivity {

    @BindView(R.id.scrolling_background)
    ScrollingImageView scrollingBackground;

    @Override
    protected void initViews() {
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        scrollingBackground.stop();
        scrollingBackground.start();
    }
}
