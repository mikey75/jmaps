package net.wirelabs.jmaps.map.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberUtils {

    /**
     * Rounds double number to nth (n=0..10) decimal place
     * Should be pretty fast
     * @param value value to round
     * @param decimalPlace decimalPoint
     * @return rounded number
     */
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
}