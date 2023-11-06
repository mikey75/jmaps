package net.wirelabs.jmaps.map.downloader;

import lombok.Getter;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class HostPrefixAlternator {

    private static final Pattern ALLOWED_ELEMENTS = Pattern.compile("^[a-zA-Z0-9_-]+(\\|[a-zA-Z0-9_-]+)*$");

    private final String prefix;
    private final String suffix;
    @Getter
    private final String cacheUrl;
    private final String[] parts;
    @Getter
    private final List<String> all;


    public HostPrefixAlternator(String url) {
        String scheme = getScheme(url);
        int schemeLen = scheme.length();

        int open = url.indexOf('[', schemeLen);
        int close = url.indexOf(']', schemeLen);

        if (open == -1 && close == -1) {
            this.prefix = url;
            this.suffix = "";
            this.cacheUrl = url;
            this.parts = new String[]{url};
            this.all = getAllHosts();
            return;
        }

        if (open == -1 || close == -1) {
            throw new IllegalArgumentException("Malformed URL: mismatched brackets");
        }

        if (open != schemeLen || close < open || url.indexOf('[', open + 1) != -1 || url.indexOf(']', close + 1) != -1) {
            throw new IllegalArgumentException("Malformed URL");
        }

        String alternatingElements = url.substring(open + 1, close);
        if (!ALLOWED_ELEMENTS.matcher(alternatingElements).matches()) {
            throw new IllegalArgumentException("Invalid content inside [...]");
        }
        this.parts = alternatingElements.split("\\|");
        this.prefix = url.substring(0, open);
        this.suffix = url.substring(close + 1);
        this.cacheUrl = prefix + (suffix.startsWith(".") ? suffix.substring(1) : suffix);
        this.all = getAllHosts();
    }

    public String getDownloadUrl() {
        if (parts.length == 1 && parts[0].equals(cacheUrl)) return cacheUrl;
        return prefix + parts[ThreadLocalRandom.current().nextInt(parts.length)] + suffix;
    }

    private List<String> getAllHosts() {
        if (parts.length == 1 && parts[0].equals(cacheUrl)) return List.of(cacheUrl);
        return Arrays.stream(parts).map(p -> prefix + p + suffix).toList();
    }

    private static String getScheme(String url) {
        if (url != null && url.startsWith("http://")) return "http://";
        if (url != null && url.startsWith("https://")) return "https://";
        throw new IllegalArgumentException("Invalid URL: " + url);
    }
}