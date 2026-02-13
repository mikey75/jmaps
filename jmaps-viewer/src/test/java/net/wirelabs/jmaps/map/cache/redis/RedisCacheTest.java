package net.wirelabs.jmaps.map.cache.redis;

import net.wirelabs.jmaps.map.utils.ImageUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RedisCacheTest {


    private static GenericContainer<?> container;
    private static RedisCache redisCache;
    // default low-time expiration (won't interfere with normal get/put but useful for testing expiration
    private static final Duration EXPIRATION = Duration.ofSeconds(2);
    private static final String TEST_IMAGE_KEY = "tile";

    @BeforeAll
    @SuppressWarnings("resource")
    static void beforeAll() {
        container = new GenericContainer<>(DockerImageName.parse("redis:latest")).waitingFor(Wait.forListeningPort()).withExposedPorts(6379);
        container.start();
        redisCache = new RedisCache(container.getHost(), container.getMappedPort(6379), EXPIRATION, 100);
    }

    @AfterAll
    static void afterAll() {
        if (container != null) container.stop();
    }

    @Test
    void shouldTestCachePutWithTimeoutScenarios() throws IOException {
        BufferedImage img = ImageIO.read(new File("src/test/resources/tiles/tile.png"));
        redisCache.put(TEST_IMAGE_KEY, img);

        // expiration time not reached - image is fetched from cache
        Awaitility.waitAtMost(Duration.ofSeconds(1)).untilAsserted(() -> assertThat(doesCachedImageExist(img)).isTrue());

        // expiration time will be reached so image is not fetched from cache
        Awaitility.waitAtMost(Duration.ofSeconds(3)).untilAsserted(() -> assertThat(doesCachedImageExist(img)).isFalse());

        // now put the same key again and check if it is timeout is renewed
        redisCache.put(TEST_IMAGE_KEY, img);
        Awaitility.waitAtMost(Duration.ofSeconds(1)).untilAsserted(() -> assertThat(doesCachedImageExist(img)).isTrue());

    }

    @Test
    void shouldNotGetNonexistentImage() {
        // get nonexistent entry
        assertThat(redisCache.get("nonexistent")).isNull();
    }

    @Test
    void shouldThrowWhenPuttingNullValue() {
        assertThatThrownBy(() -> redisCache.put(TEST_IMAGE_KEY, null)).isInstanceOf(IllegalArgumentException.class);
    }

    private boolean doesCachedImageExist(BufferedImage img) {
        BufferedImage retrieved = redisCache.get(TEST_IMAGE_KEY);
        return retrieved != null && ImageUtils.imagesEqual(retrieved, img);
    }
}
