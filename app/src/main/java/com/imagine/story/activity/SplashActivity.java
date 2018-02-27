package com.imagine.story.activity;

import android.content.Intent;

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

        goLogin();
        // goHome();
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void goLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
