package com.imagine.story;

import android.content.Context;
import android.content.Intent;

import com.imagine.story.activity.HomeActivity;
import com.imagine.story.activity.LoginActivity;

/**
 * Created by conquer on 2018/2/27.
 *
 */

public class IntentManager {

    public static void goHome(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
    }

    public static void goLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

}
