package com.imagine.story.activity;

import com.imagine.story.IntentManager;
import com.imagine.story.R;
import com.imagine.story.common.base.BaseActivity;

import butterknife.ButterKnife;

/**
 * Created by conquer on 2018/2/25.
 *
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void initViews() {
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        IntentManager.goLogin(this);
        finish();
    }





}
