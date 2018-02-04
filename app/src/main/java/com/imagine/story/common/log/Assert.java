package com.imagine.story.common.log;

import android.support.annotation.NonNull;

/**
 * Created by conquer on 2018/1/15.
 */

public class Assert {
    private static final String LOG_FILE_PATH = "/imagine/story/log.txt";

    public static void log(@NonNull String e) {
        log(new Throwable(e), "");
    }

    private static void log(@NonNull Throwable e, String stackTrace) {
        writeToFile(e, stackTrace);
    }

    private static void writeToFile(@NonNull Throwable e, String stackTrace) {

    }

}
