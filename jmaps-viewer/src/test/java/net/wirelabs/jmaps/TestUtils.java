package net.wirelabs.jmaps;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Random;

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
}
