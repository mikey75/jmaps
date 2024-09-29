package net.wirelabs.jmaps;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.wirelabs.jmaps.map.cache.Cache;
import net.wirelabs.jmaps.map.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created 10/28/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {
    public static double roundDouble(double value, int decimalPlace) {

        if (decimalPlace > 10) decimalPlace = 10;
        if (decimalPlace < 0) decimalPlace = 0;

        double [] coeffs = {1.0,
                10.0,
                100.0,
                1000.0,
                10000.0,
                100000.0,
                1000000.0,
                10000000.0,
                100000000.0,
                1000000000.0,
                10000000000.0
        };

        double mask = coeffs[decimalPlace];
        return Math.round(value * mask) / mask;
    }

    public static String getRandomString(int len) {

        String chars = "abcdefghijklmnopqrstuwvxyzABCDEFGHIJKLMNOPQRSTUWVXYZ";
        Random random = new Random();
        StringBuilder resultString = new StringBuilder();

        for (int x = 0; x < len; x++) {
            int idx = random.nextInt(chars.length() - 1);
            resultString.append(chars.charAt(idx));
        }
        return resultString.toString();
    }

    public static void cacheCheckExistenceAndExpiration(Cache<String, BufferedImage> cache, String key, boolean exists, boolean expired) {
        if (exists) {
            assertThat(cache.get(key)).isNotNull();
        } else {
            assertThat(cache.get(key)).isNull();
        }
        assertThat(cache.keyExpired(key)).isEqualTo(expired);
    }

    public static void cacheAssertSameData(Cache<String,BufferedImage> cache, String key, BufferedImage img) {
        BufferedImage image = cache.get(key);
        assertThat(image).isNotNull();
        assertThat(ImageUtils.imagesEqual(image, img)).isTrue();
    }
}
