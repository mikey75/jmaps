package net.wirelabs.jmaps.map.utils;

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
            finalUrl.append("?").append(param)
                    .append("=").append(value);
        } else {
            // following first
            finalUrl.append("&").append(param)
                    .append("=").append(value);
        }
        paramCount++;
        return this;
    }
    /**
     * returns String value of the generated URL
     */
    public String build() {
        // reset paramcount at the end
        paramCount = 0;
        return finalUrl.toString();
    }
}
