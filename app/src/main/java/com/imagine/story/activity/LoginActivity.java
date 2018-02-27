package com.imagine.story.activity;

import android.view.View;
import android.widget.FrameLayout;

import com.imagine.story.IntentManager;
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

    @BindView(R.id.weChat)
    FrameLayout weChat;

    @Override
    protected void initViews() {
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // startScroll();
        weChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentManager.goHome(LoginActivity.this);
            }
        });
    }

    private void startScroll() {
        scrollingBackground.stop();
        scrollingBackground.start();
    }

}
