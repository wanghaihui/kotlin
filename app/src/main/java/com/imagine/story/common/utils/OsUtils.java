package com.imagine.story.common.utils;

import android.os.Build;

/**
 * Created by meijian on 2018/1/17.
 */

public class OsUtils {

    /**
     * 获取手机型号
     */
    public static String model() {
        return Build.MODEL;
    }
}
