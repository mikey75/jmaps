package net.wirelabs.jmaps.map.downloader;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HostPrefixAlternator {

    private static final String EXTRACTION_REGEX = ".*\\[(.*?)].*";
    private static final String SEPARATOR = ":";
    private final Pattern pattern = Pattern.compile(EXTRACTION_REGEX);
    private final Random random = new Random();

    public String resolveHostPrefixUrl(String url) {

        Matcher m = pattern.matcher(url);
        if (m.find()) {
            String content = m.group(1); // whole matched expression
            if (content.contains(SEPARATOR)) {
                String[] splited = content.split(SEPARATOR);
                String hostPrefix = splited[random.nextInt(splited.length)];
                return url.replaceAll("\\[(.*?)]", hostPrefix);
            } else {
                throw new IllegalStateException("Host alteration without separator");
            }
        }
        return url;
    }
}
