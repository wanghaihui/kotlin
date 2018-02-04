package com.imagine.story.activity;

import android.view.Window;
import android.view.WindowManager;

import com.imagine.story.common.base.BaseActivity;
import com.imagine.story.fragment.GirlFragment;


/**
 * Created by conquer on 2018/1/19.
 *
 */

public class GirlActivity extends BaseActivity {

    @Override
    protected void initViews() {
        // 去标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 去状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getSupportFragmentManager().beginTransaction().add(android.R.id.content, new GirlFragment()).commit();

    }

}
