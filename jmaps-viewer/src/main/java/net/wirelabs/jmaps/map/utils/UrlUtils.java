package net.wirelabs.jmaps.map.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.URI;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtils {

    public static String urlToStringPath(String url) throws IOException {

        URI uri = URI.create(url);
        StringBuilder sb = new StringBuilder();

        String host = uri.getHost();
        String query = uri.getQuery();
        String path = uri.getPath();

        if (query != null) {
            // replace all ?param=value or &param=value with only value - the file path will be shorter but should be still unique
            query = query.replaceAll("\\b([^&=?]+)=", "");
            // replace all "/" to ":" -> so image/png becomes image:png - will help later when we create file path where "/" is the file separator
            query = query.replace("/", ":");
        }


        if (host != null) {
            sb.append(host);
        }
        if (path != null) {
            sb.append(path);
        }
        if (query != null) {
            sb.append('?');
            sb.append(query);
        }

        String name;

        final int maxLen = 250; // should perhaps be 255, that is the max filename length on Windows/Linux/Mac

        if (sb.length() < maxLen) {
            name = sb.toString();
        } else {
            throw new IOException("Cache key too long!");
        }
        name = normalizeUrl(name);

        return name;
    }

    private static String normalizeUrl(String name) {
        // replace & and ? with / and all dots for _
        name = name.replaceAll("[?|&]", "/");
        name = name.replace(".","_");
        return name;
    }
}


