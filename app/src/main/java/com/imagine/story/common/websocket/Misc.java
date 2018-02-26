package com.imagine.story.common.websocket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by conquer on 2018/2/9.
 *
 */

class Misc {
    // 安全的随机数
    private static final SecureRandom sRandom = new SecureRandom();

    private Misc() {
    }

    /**
     * Get a UTF-8 byte array representation of the given string.
     */
    public static byte[] getBytesUTF8(String string) {
        if (string == null) {
            return null;
        }

        try {
            return string.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // This never happens.
            return null;
        }
    }

    // 提取host
    public static String extractHost(URI uri) {
        // Extract the host part from the URI.
        String host = uri.getHost();

        if (host != null) {
            return host;
        }

        // According to Issue#74, URI.getHost() method returns null in
        // the following environment when the host part of the URI is
        // a host name.
        //
        //   - Samsung Galaxy S3 + Android API 18
        //   - Samsung Galaxy S4 + Android API 21
        //
        // The following is a workaround for the issue.

        // Extract the host part from the authority part of the URI.
        host = extractHostFromAuthorityPart(uri.getRawAuthority());

        if (host != null) {
            return host;
        }

        // Extract the host part from the entire URI.
        return extractHostFromEntireUri(uri.toString());
    }

    private static String extractHostFromAuthorityPart(String authority) {
        // If the authority part is not available.
        if (authority == null) {
            // Hmm... This should not happen.
            return null;
        }

        // Parse the authority part. The expected format is "[id:password@]host[:port]".
        // 定位符用来描述字符串或单词的边界
        // ^ -- 字符串的开始--除非在方括号表达式中使用，此时它表示不接受该字符集合
        // $ -- 字符串的结束
        // * -- 匹配前面的子表达式零次或多次
        // () -- 标记一个子表达式的开始和结束位置
        // . -- 匹配除换行符\n之外的任何单字符
        // ? -- 匹配前面的子表达式零次或一次
        // + -- 匹配前面的子表达式一次或多次
        // [^:] -- 除了:以外的任何字符
        // 序列 '\\' 匹配 "\" -- \d -- 表示数字 -- [0-9]
        Matcher matcher = Pattern.compile("^(.*@)?([^:]+)(:\\d+)?$").matcher(authority);

        // If the authority part does not match the expected format.
        if (!matcher.matches()) {
            // Hmm... This should not happen.
            return null;
        }

        // Return the host part.
        return matcher.group(2);
    }

    private static String extractHostFromEntireUri(String uri) {
        if (uri == null) {
            // Hmm... This should not happen.
            return null;
        }

        // Parse the URI. The expected format is "scheme://[id:password@]host[:port][...]".
        // \w -- 匹配包括下划线的任何单词字符,等价于 [A-Z a-z 0-9_]

        Matcher matcher = Pattern.compile("^\\w+://([^@/]*@)?([^:/]+)(:\\d+)?(/.*)?$").matcher(uri);

        // If the URI does not match the expected format.
        if (!matcher.matches()) {
            // Hmm... This should not happen.
            return null;
        }

        // Return the host part.
        return matcher.group(2);
    }

    /**
     * Read a line from the given stream.
     */
    public static String readLine(InputStream in, String charset) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (true) {
            // Read one byte from the stream.
            int b = in.read();

            // If the end of the stream was reached.
            if (b == -1) {
                if (baos.size() == 0) {
                    // No more line.
                    return null;
                } else {
                    // The end of the line was reached.
                    break;
                }
            }

            if (b == '\n') {
                // The end of the line was reached.
                break;
            }

            if (b != '\r') {
                // Normal character.
                baos.write(b);
                continue;
            }

            // Read one more byte.
            int b2 = in.read();

            // If the end of the stream was reached.
            if (b2 == -1) {
                // Treat the '\r' as a normal character.
                baos.write(b);

                // The end of the line was reached.
                break;
            }

            // If '\n' follows the '\r'.
            if (b2 == '\n') {
                // The end of the line was reached.
                break;
            }

            // Treat the '\r' as a normal character.
            baos.write(b);

            // Append the byte which follows the '\r'.
            baos.write(b2);
        }

        // Convert the byte array to a string.
        return baos.toString(charset);
    }

    /**
     * Fill the given buffer with random bytes.
     */
    public static byte[] nextBytes(byte[] buffer) {
        sRandom.nextBytes(buffer);
        return buffer;
    }

    public static String join(Collection<?> values, String delimiter) {
        StringBuilder builder = new StringBuilder();
        join(builder, values, delimiter);
        return builder.toString();
    }

    private static void join(StringBuilder builder, Collection<?> values, String delimiter) {
        boolean first = true;

        for (Object value : values) {
            if (first) {
                first = false;
            } else {
                builder.append(delimiter);
            }

            builder.append(value.toString());
        }
    }

    public static String toStringUTF8(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return toStringUTF8(bytes, 0, bytes.length);
    }

    public static String toStringUTF8(byte[] bytes, int offset, int length) {
        if (bytes == null) {
            return null;
        }

        try {
            return new String(bytes, offset, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // This never happens.
            return null;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public static String toOpcodeName(int opcode) {
        switch (opcode) {
            case WebSocketOpcode.CONTINUATION:
                return "CONTINUATION";

            case WebSocketOpcode.TEXT:
                return "TEXT";

            case WebSocketOpcode.BINARY:
                return "BINARY";

            case WebSocketOpcode.CLOSE:
                return "CLOSE";

            case WebSocketOpcode.PING:
                return "PING";

            case WebSocketOpcode.PONG:
                return "PONG";

            default:
                break;
        }

        if (0x1 <= opcode && opcode <= 0x7) {
            return String.format("DATA(0x%X)", opcode);
        }

        if (0x8 <= opcode && opcode <= 0xF) {
            return String.format("CONTROL(0x%X)", opcode);
        }

        return String.format("0x%X", opcode);
    }

    /**
     * Create a buffer of the given size filled with random bytes.
     */
    public static byte[] nextBytes(int nBytes) {
        byte[] buffer = new byte[nBytes];
        return nextBytes(buffer);
    }

    public static int min(int[] values) {
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < values.length; ++i) {
            if (values[i] < min) {
                min = values[i];
            }
        }

        return min;
    }

    public static int max(int[] values) {
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < values.length; ++i) {
            if (max < values[i]) {
                max = values[i];
            }
        }

        return max;
    }

}
