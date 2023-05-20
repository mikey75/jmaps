package net.wirelabs.jmaps.viewer.utils;

import net.wirelabs.jmaps.viewer.utils.NumberUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class NumberUtilsTest {

    @Test
    void test(){

        double number = 748754.6993614006;

        assertThat(NumberUtils.roundDouble(number, 0)).isEqualTo(748755);
        assertThat(NumberUtils.roundDouble(number, 1)).isEqualTo(748754.7);
        assertThat(NumberUtils.roundDouble(number, 2)).isEqualTo(748754.7);
        assertThat(NumberUtils.roundDouble(number, 3)).isEqualTo(748754.699);
        assertThat(NumberUtils.roundDouble(number, 4)).isEqualTo(748754.6994);
        assertThat(NumberUtils.roundDouble(number, 5)).isEqualTo(748754.69936);
        assertThat(NumberUtils.roundDouble(number, 6)).isEqualTo(748754.699361);
        assertThat(NumberUtils.roundDouble(number, 7)).isEqualTo(748754.6993614);
        assertThat(NumberUtils.roundDouble(number, 8)).isEqualTo(748754.6993614);
        assertThat(NumberUtils.roundDouble(number, 9)).isEqualTo(748754.699361401);




    }


}