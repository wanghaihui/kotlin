package com.imagine.story.common.utils;

import android.content.Context;

/**
 * Created by conquer on 2018/2/6.
 *
 */

public class ScreenUtils {

    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int getFeedImageWidthPixels(Context context) {
        return (context.getResources().getDisplayMetrics().widthPixels - dp2px(context, 4 * 2)) / 2;
    }

}
