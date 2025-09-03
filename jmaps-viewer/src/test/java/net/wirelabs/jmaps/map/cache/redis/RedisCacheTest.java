package net.wirelabs.jmaps.map.cache.redis;

import com.redis.testcontainers.RedisContainer;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.cache.redis.RedisClient;
import net.wirelabs.jmaps.map.utils.ImageUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.DockerImageName;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class RedisCacheTest {

    private static RedisContainer container;
    private static RedisCache redisCache;
    // default low-time expiration (won't interfere with normal get/put but useful for testing expiration
    private static final Duration expiration = Duration.ofSeconds(2);

    @BeforeAll
    static void before() {
        container = new RedisContainer(DockerImageName.parse("redis:latest"));
        container.start();
        Awaitility.waitAtMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(container.isRunning()).isTrue());
        redisCache = new RedisCache("localhost", container.getRedisPort(), expiration, 100);
    }

    @Test
    void putimage() throws IOException {
        // put image with short ttl, get it - it should be available momentarily
        // then wait some time and get it again, should be null
        BufferedImage img = ImageIO.read(new File("src/test/resources/tiles/tile.png"));
        redisCache.put("dupa", img);

        // timeout (2s) not reached - get should be successful
        Awaitility.waitAtMost(Duration.ofMillis(500)).pollDelay(Duration.ofMillis(250)).untilAsserted(() -> {
            BufferedImage g = redisCache.get("dupa");
            assertThat(g).isNotNull();
            assertThat(ImageUtils.imagesEqual(g, img)).isTrue();
        });

        // timeout expired - get should return null
        Awaitility.waitAtMost(Duration.ofSeconds(3)).pollDelay(Duration.ofMillis(200)).untilAsserted(() -> {
            BufferedImage image = redisCache.get("dupa");
            assertThat(image).isNull();
        });

    }

    @Test
    void getImage()  {
        // get nonexistent entry
        assertThat(redisCache.get("kaka")).isNull();

    }
}
