package net.wirelabs.jmaps.map.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlBuilder {

    private final StringBuilder finalUrl = new StringBuilder();
    private int paramCount = 0;

    public UrlBuilder parse(String url) {
        // reset buffer and count at the beginning of the url creation
        finalUrl.setLength(0);
        paramCount = 0;
        finalUrl.append(url);
        return this;
    }

    public UrlBuilder addParam(String param, String value) {
        if (paramCount == 0) {
            // first
            finalUrl.append("?").append(URLEncoder.encode(param, StandardCharsets.UTF_8))
                    .append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        } else {
            // following first
            finalUrl.append("&").append(URLEncoder.encode(param, StandardCharsets.UTF_8))
                    .append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
        paramCount++;
        return this;
    }

    public String build() {
        // reset paramcount at the end
        paramCount = 0;
        return finalUrl.toString();
    }
}
