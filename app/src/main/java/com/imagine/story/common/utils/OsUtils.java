package com.imagine.story.common.utils;

import android.os.Build;

import java.io.UnsupportedEncodingException;

/**
 * Created by conquer on 2018/1/17.
 *
 */

public class OsUtils {

    /**
     * 获取手机型号
     */
    public static String model() {
        return Build.MODEL;
    }

    public static String device() {
        try {
            return HttpUtils.encode(model());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
