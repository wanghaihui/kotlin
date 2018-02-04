package com.imagine.story.common.utils;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by meijian on 2018/1/18.
 */

public class HttpUtils {
    public static final String UTF_8 = "UTF-8";

    public static String decode(String encode) throws IOException {
        return URLDecoder.decode(encode, UTF_8);
    }

    public static String encode(String parameter) throws IOException{
        return URLEncoder.encode(parameter, UTF_8);
    }
}
