package net.wirelabs.jmaps.map.downloader;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HostPrefixAlternatorTest {

    HostPrefixAlternator alternator = new HostPrefixAlternator();

    String CORRECT_FOR_ALTERNATING = "http://[A:B:C].openstreetmap.org/1/2/3.png";
    String NO_ALTERNATING = "http://a.openstreetmap.org/1/2/3.png";
    String ILLEGAL_1 = "http://[a|b|c].openstreetmap.org/1/2/3.png";
    String ILLEGAL_2 = "http://[a].openstreetmap.org/1/2/3.png";


    @Test
    void shouldExtractOnlyLegalTag() {

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            alternator.resolveHostPrefixUrl(ILLEGAL_1);
        });
        assertThat(exception).hasMessage("Host alteration without separator");

        exception = assertThrows(IllegalStateException.class, () -> {
            alternator.resolveHostPrefixUrl(ILLEGAL_2);
        });
        assertThat(exception).hasMessage("Host alteration without separator");
    }

    @Test
    void shouldAlternateHostPrefixInSupportedURLs() {
        String s = alternator.resolveHostPrefixUrl(CORRECT_FOR_ALTERNATING);
        assertThat(s).matches("(http://A.openstreetmap.org/1/2/3.png|http://B.openstreetmap.org/1/2/3.png|http://C.openstreetmap.org/1/2/3.png)");


    }
    @Test
    void shouldNotAlternateInUnsupportedUrls() {
        String url = alternator.resolveHostPrefixUrl(NO_ALTERNATING);
        assertThat(url).isEqualTo(NO_ALTERNATING);
    }




}
