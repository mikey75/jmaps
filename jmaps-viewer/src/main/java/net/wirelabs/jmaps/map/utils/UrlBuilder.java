package net.wirelabs.jmaps.map.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
/**
 * URL Builder - build string urls
 * (not URL encoded - that is the work of entity using the url like httpclient and such)
 * Can be chained: parse(baseurl).addQueryParam(...).addQueryParam(...).build()
 */
public class UrlBuilder {

    private final StringBuilder finalUrl = new StringBuilder();
    private int paramCount = 0;

    /**
     * Parse baseUrl
     * @param url baseUrl
     * @return self instance for chaining
     */
    public UrlBuilder parse(String url) {
        // reset buffer and count at the beginning of the url creation
        finalUrl.setLength(0);
        paramCount = 0;
        finalUrl.append(url);
        return this;
    }

    /**
     * Adds query param
     * @param param parameter
     * @param value value
     * @return self instance for chaining
     */
    public UrlBuilder addQueryParam(String param, String value) {
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
    /**
     * returns String value of the generated URL
     */
    public String build() {
        // reset param count at the end
        paramCount = 0;
        return finalUrl.toString();
    }
}
