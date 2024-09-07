package net.wirelabs.jmaps.map;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class MapFileValidatorTest {

    private final File MAPFILE_OK = new File("src/test/resources/map.xml");
    private final File MAPFILE_BAD = new File("src/test/resources/map-bad.xml");
    private final MapFileValidator mapFileValidator = new MapFileValidator();

    @Test
    void testMaps() {
        assertThat(mapFileValidator.validateMapFile(MAPFILE_OK)).isTrue();
        assertThat(mapFileValidator.validateMapFile(MAPFILE_BAD)).isFalse();
    }
}
