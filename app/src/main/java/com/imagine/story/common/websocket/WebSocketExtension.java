package com.imagine.story.common.websocket;


import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class to hold the name and the parameters of a WebSocket extension.
 */
public class WebSocketExtension {
    /**
     * The name of <code>permessage-deflate</code> extension that is
     * defined in <a href="https://tools.ietf.org/html/rfc7692#section-7"
     * >7&#46; The "permessage-deflate" Extension</a> in <a href=
     * "https://tools.ietf.org/html/rfc7692">RFC 7692</a>.
     */
    public static final String PERMESSAGE_DEFLATE = "permessage-deflate";

    private final String mName;
    private final Map<String, String> mParameters;

    public WebSocketExtension(String name) {
        // Check the validity of the name.
        if (!Token.isValid(name)) {
            // The name is not a valid token.
            throw new IllegalArgumentException("'name' is not a valid token.");
        }

        mName = name;
        mParameters = new LinkedHashMap<>();
    }

    public WebSocketExtension(WebSocketExtension source) {
        if (source == null) {
            // If the given instance is null.
            throw new IllegalArgumentException("'source' is null.");
        }

        mName = source.getName();
        mParameters = new LinkedHashMap<>(source.getParameters());
    }

    public String getName() {
        return mName;
    }

    public Map<String, String> getParameters() {
        return mParameters;
    }

    public boolean containsParameter(String key) {
        return mParameters.containsKey(key);
    }

    public String getParameter(String key) {
        return mParameters.get(key);
    }

    public WebSocketExtension setParameter(String key, String value) {
        // Check the validity of the key.
        if (!Token.isValid(key)) {
            // The key is not a valid token.
            throw new IllegalArgumentException("'key' is not a valid token.");
        }

        // If the value is not null.
        if (value != null) {
            // Check the validity of the value.
            if (!Token.isValid(value)) {
                // The value is not a valid token.
                throw new IllegalArgumentException("'value' is not a valid token.");
            }
        }

        mParameters.put(key, value);

        return this;
    }


    /**
     * Stringify this object into the format "{name}[; {key}[={value}]]*".
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(mName);

        for (Map.Entry<String, String> entry : mParameters.entrySet()) {
            // "; {key}"
            builder.append("; ").append(entry.getKey());

            String value = entry.getValue();

            if (value != null && value.length() != 0) {
                // "={value}"
                builder.append("=").append(value);
            }
        }

        return builder.toString();
    }


    /**
     * Validate this instance. This method is expected to be overridden.
     */
    void validate() throws WebSocketException {
    }

    public static WebSocketExtension parse(String string) {
        if (string == null) {
            return null;
        }

        // Split the string by semi-colons.
        String[] elements = string.trim().split("\\s*;\\s*");

        if (elements.length == 0) {
            // Even an extension name is not included.
            return null;
        }

        // The first element is the extension name.
        String name = elements[0];

        if (!Token.isValid(name)) {
            // The extension name is not a valid token.
            return null;
        }

        // Create an instance for the extension name.
        WebSocketExtension extension = createInstance(name);

        // For each "{key}[={value}]".
        for (int i = 1; i < elements.length; ++i) {
            // Split by '=' to get the key and the value.
            String[] pair = elements[i].split("\\s*=\\s*", 2);

            // If {key} is not contained.
            if (pair.length == 0 || pair[0].length() == 0) {
                // Ignore.
                continue;
            }

            // The name of the parameter.
            String key = pair[0];

            if (!Token.isValid(key)) {
                // The parameter name is not a valid token.
                // Ignore this parameter.
                continue;
            }

            // The value of the parameter.
            String value = extractValue(pair);

            if (value != null) {
                if (!Token.isValid(value)) {
                    // The parameter value is not a valid token.
                    // Ignore this parameter.
                    continue;
                }
            }

            // Add the pair of the key and the value.
            extension.setParameter(key, value);
        }

        return extension;
    }


    private static String extractValue(String[] pair) {
        if (pair.length != 2) {
            return null;
        }

        return Token.unquote(pair[1]);
    }


    private static WebSocketExtension createInstance(String name) {
        if (PERMESSAGE_DEFLATE.equals(name)) {
            return new PerMessageDeflateExtension(name);
        }

        return new WebSocketExtension(name);
    }
}
