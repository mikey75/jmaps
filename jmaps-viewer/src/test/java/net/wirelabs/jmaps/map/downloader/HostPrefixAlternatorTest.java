package net.wirelabs.jmaps.map.downloader;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
class HostPrefixAlternatorTest {

    HostPrefixAlternator alternator = new HostPrefixAlternator();

    @Test
    void shouldPassThru() {
        // pass through - no alternation
        UrlSet urlSet = alternator.resolvePrefixedUrl("http://a.tile.openstreetmap.org/1/2/3.png");
        assertThat(urlSet.url()).isEqualTo("http://a.tile.openstreetmap.org/1/2/3.png");
        assertThat(urlSet.originalUrl()).isEqualTo("http://a.tile.openstreetmap.org/1/2/3.png");

        urlSet = alternator.resolvePrefixedUrl("https://a.tile.openstreetmap.org/1/2/3.png");
        assertThat(urlSet.url()).isEqualTo("https://a.tile.openstreetmap.org/1/2/3.png");
        assertThat(urlSet.originalUrl()).isEqualTo("https://a.tile.openstreetmap.org/1/2/3.png");

    }

    @Test
    void shouldExtractLegalUrls() {
        // classic one letter OSM style
        UrlSet urlSet = alternator.resolvePrefixedUrl("http://[a|b|c].tile.openstreetmap.org/1/2/3.png");
        assertThat(urlSet.url()).matches("(" +
                "http://a\\.tile\\.openstreetmap\\.org/1/2/3\\.png|" +
                "http://b\\.tile\\.openstreetmap\\.org/1/2/3\\.png|" +
                "http://c\\.tile\\.openstreetmap\\.org/1/2/3\\.png" +
                ")"
        );
        assertThat(urlSet.originalUrl()).isEqualTo("http://tile.openstreetmap.org/1/2/3.png");

        // multiple letters, and https
        urlSet = alternator.resolvePrefixedUrl("https://[host1|host2|host3].tile.openstreetmap.org/1/2/3.png");
        assertThat(urlSet.url()).matches("(" +
                "https://host1\\.tile\\.openstreetmap\\.org/1/2/3\\.png|" +
                "https://host2\\.tile\\.openstreetmap\\.org/1/2/3\\.png|" +
                "https://host3\\.tile\\.openstreetmap\\.org/1/2/3\\.png" +
                ")"
        );
        assertThat(urlSet.originalUrl()).isEqualTo("https://tile.openstreetmap.org/1/2/3.png");


    }

    @Test
    void shouldEctractOneElement() {
        // one host alternation (legal but useless ;))
        UrlSet urlSet = alternator.resolvePrefixedUrl("https://[a].tile.openstreetmap.org/1/2/3.png");
        assertThat(urlSet.url()).isEqualTo("https://a.tile.openstreetmap.org/1/2/3.png");
    }
    @Test
    void shouldThrowOnInvalidUrls() {
        // url does not contain http:// or https:// -> throw exception
        // [a||b].host.com -> invalid, throw exception
        // [a|].host.com -> invalid, throw exception
        // [a,b,c].host.com -> invalid, throw exception
        // multiple [...] sets -> invalid, throw exception
        // mismatched brackets e.g "https://]host.com" or "https://[host.com") -> throw exception
        // [...] does not appear immediately after the http:// or https://

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("[a|b].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("http://[a||b].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("http://[a| |b].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("https://[a|].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("http://[a,b,c].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("https://[a|b,c].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("http://[a|b|c].tile[a|b|c|d].openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("https://]].openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("https://][.tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("https://[.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("https://].openstreetmap.org/1/2/3.png");
        });


        assertThrows(IllegalArgumentException.class, () -> {
            alternator.resolvePrefixedUrl("http://a.tile[a|b|c|d].openstreetmap.org/1/2/3.png");
        });

    }
}
