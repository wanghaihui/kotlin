package com.imagine.story.common.websocket;

class Token {
    /**
     * Check if the given string conforms to the rules described
     * in "<a href="http://tools.ietf.org/html/rfc2616#section-2.2"
     * >2.2 Basic Rules</a>" of <a href="http://tools.ietf.org/html/rfc2616"
     * >RFC 2616</a>.
     */
    public static boolean isValid(String token) {
        if (token == null || token.length() == 0) {
            return false;
        }

        int len = token.length();

        for (int i = 0; i < len; ++i) {
            if (isSeparator(token.charAt(i))) {
                return false;
            }
        }

        return true;
    }


    public static boolean isSeparator(char ch) {
        switch (ch) {
            case '(':
            case ')':
            case '<':
            case '>':
            case '@':
            case ',':
            case ';':
            case ':':
            case '\\':
            case '"':
            case '/':
            case '[':
            case ']':
            case '?':
            case '=':
            case '{':
            case '}':
            case ' ':
            case '\t':
                return true;

            default:
                return false;
        }
    }


    public static String unquote(String text) {
        if (text == null) {
            return null;
        }

        int len = text.length();

        if (len < 2 || text.charAt(0) != '"' || text.charAt(len-1) != '"') {
            return text;
        }

        text = text.substring(1, len-1);

        return unescape(text);
    }


    public static String unescape(String text) {
        if (text == null) {
            return null;
        }

        if (text.indexOf('\\') < 0) {
            return text;
        }

        int len = text.length();
        boolean escaped = false;
        StringBuilder builder = new StringBuilder();


        for (int i = 0; i < len; ++i) {
            char ch = text.charAt(i);

            if (ch == '\\' && escaped == false) {
                escaped = true;
                continue;
            }

            escaped = false;
            builder.append(ch);
        }

        return builder.toString();
    }
}

