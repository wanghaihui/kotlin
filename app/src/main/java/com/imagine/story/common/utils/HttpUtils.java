package com.imagine.story.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by conquer on 2018/1/18.
 *
 */

public class HttpUtils {
    private static final String UTF_8 = "UTF-8";

    public static String decode(String encode) throws UnsupportedEncodingException {
        return URLDecoder.decode(encode, UTF_8);
    }

    public static String encode(String parameter) throws UnsupportedEncodingException {
        return URLEncoder.encode(parameter, UTF_8);
    }
}
