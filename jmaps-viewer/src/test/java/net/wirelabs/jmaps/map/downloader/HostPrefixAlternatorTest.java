package net.wirelabs.jmaps.map.downloader;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Slf4j
class HostPrefixAlternatorTest {

    HostPrefixAlternator alternator;

    @Test
    void shouldPassThru() {
        // pass through - no alternation
        alternator = new HostPrefixAlternator("http://a.tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(alternator.getAll()).containsOnly("http://a.tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(alternator.getCacheUrl()).isEqualTo("http://a.tile.openstreetmap.org/{z}/{x}/{y}.png");


        alternator = new HostPrefixAlternator("https://a.tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(alternator.getAll()).containsOnly("https://a.tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(alternator.getCacheUrl()).isEqualTo("https://a.tile.openstreetmap.org/{z}/{x}/{y}.png");


    }

    @Test
    void shouldExtractLegalMultiUrls() {
        // classic one letter OSM style

        alternator = new HostPrefixAlternator("http://[a|b|c].tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(alternator.getAll()).containsOnly(
                "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
                "http://b.tile.openstreetmap.org/{z}/{x}/{y}.png",
                "http://c.tile.openstreetmap.org/{z}/{x}/{y}.png"
        );
        assertThat(alternator.getCacheUrl()).isEqualTo("http://tile.openstreetmap.org/{z}/{x}/{y}.png");

        // multiple letters, and https
        alternator = new HostPrefixAlternator("https://[host1|host2|host3].tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(alternator.getAll()).containsOnly(
                "https://host1.tile.openstreetmap.org/{z}/{x}/{y}.png",
                "https://host2.tile.openstreetmap.org/{z}/{x}/{y}.png",
                "https://host3.tile.openstreetmap.org/{z}/{x}/{y}.png"
        );
        assertThat(alternator.getCacheUrl()).isEqualTo("https://tile.openstreetmap.org/{z}/{x}/{y}.png");


    }
    @Test
    void shouldReturnRandomUrlFromMultihost() {
        alternator = new HostPrefixAlternator("http://[a|b|c].tile.openstreetmap.org/{z}/{x}/{y}.png");
        Awaitility.waitAtMost(Duration.ofSeconds(2)).untilAsserted(() -> {
            String url1 = alternator.getDownloadUrl();
            String url2 = alternator.getDownloadUrl();
            assertThat(url1).isNotEqualTo(url2);
        });
    }

    @Test
    void shouldEctractOneElement() {
        // one host alternation (legal but useless ;))
        alternator = new HostPrefixAlternator("https://[a].tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(alternator.getAll()).containsOnly("https://a.tile.openstreetmap.org/{z}/{x}/{y}.png");
        assertThat(alternator.getCacheUrl()).isEqualTo("https://tile.openstreetmap.org/{z}/{x}/{y}.png");
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
            alternator = new HostPrefixAlternator("[a|b].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("http://[a||b].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("http://[a| |b].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("https://[a|].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("http://[a,b,c].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("https://[a|b,c].tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("http://[a|b|c].tile[a|b|c|d].openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("https://]].openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("https://][.tile.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("https://[.openstreetmap.org/1/2/3.png");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("https://].openstreetmap.org/1/2/3.png");
        });


        assertThrows(IllegalArgumentException.class, () -> {
            alternator= new HostPrefixAlternator("http://a.tile[a|b|c|d].openstreetmap.org/1/2/3.png");
        });

    }
}
