package net.wirelabs.jmaps.map.downloader;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class HostPrefixAlternator {

    // supported resolving for now: (both http and https)
    // [a|b|c].host.com -> a.host.com, b.host.com, c.host.com, original: host.com
    // [a].host.com -> a.host.com, original: host.com
    // [a||b].host.com -> invalid, throw exception
    // [a|].host.com -> invalid, throw exception
    // [a,b,c].host.com -> invalid, throw exception
    // multiple [...] sets -> invalid, throw exception

    private static final Pattern allowedElementsPattern = Pattern.compile("^[a-zA-Z0-9_-]+(\\|[a-zA-Z0-9_-]+)*$");

    public UrlSet resolvePrefixedUrl(String url) {


        String scheme = getScheme(url);

        int schemeLen = scheme.length();

        // Find bracket positions
        int open = url.indexOf('[', schemeLen);
        int close = url.indexOf(']', schemeLen);

        // No brackets = return untouched
        if (open == -1 && close == -1) {
            return new UrlSet(url, url);
        }

        // Catch mismatched brackets early (e.g. "https://]host.com" or "https://[host.com")
        // before the more general structural checks below.
        if (open == -1 || close == -1) {
            throw new IllegalArgumentException("Malformed URL: mismatched brackets");
        }

        // Enforce strict bracket rules:
        //  - '[' must appear exactly at the start of the host (right after the scheme)
        //  - ']' must come after '['
        //  - No second '[' or ']' anywhere in the URL (only one group allowed)
        if (open != schemeLen || close < open || url.indexOf('[', open + 1) != -1 || url.indexOf(']', close + 1) != -1) {
            throw new IllegalArgumentException("Malformed URL");
        }

        // Get inside content
        String alternatingElements = url.substring(open + 1, close);

        if (!allowedElementsPattern.matcher(alternatingElements).matches()) {
            throw new IllegalArgumentException("Invalid content inside [...]");
        }

        // split
        String[] parts = alternatingElements.split("\\|");
        // Choose 1 random value
        String chosen = parts[ThreadLocalRandom.current().nextInt(parts.length)];

        // Build expanded: substitute bracket
        String expanded = url.substring(0, open) + chosen + url.substring(close + 1);

        // Build original: remove bracket entirely
        String original = url.substring(0, open) + url.substring(close + 1);

        // Remove accidental leading dot after scheme: http://.host.com → http://host.com
        if (original.charAt(schemeLen) == '.') {
            original = original.substring(0, schemeLen) + original.substring(schemeLen + 1);
        }

        return new UrlSet(expanded, original);
    }


    private static String getScheme(String url) {
        if (url != null && url.startsWith("http://")) return "http://";
        if (url != null && url.startsWith("https://")) return "https://";
        throw new IllegalArgumentException("Invalid URL:" + url);
    }
}
