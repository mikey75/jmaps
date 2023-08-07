package net.wirelabs.jmaps.utils;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;



/**
 * Created 5/23/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
class NumberUtilsTest {

    @Test
    void test(){

        double number = 748754.6993614006;

        Assertions.assertThat(NumberUtils.roundDouble(number, 0)).isEqualTo(748755);
        Assertions.assertThat(NumberUtils.roundDouble(number, 1)).isEqualTo(748754.7);
        Assertions.assertThat(NumberUtils.roundDouble(number, 2)).isEqualTo(748754.7);
        Assertions.assertThat(NumberUtils.roundDouble(number, 3)).isEqualTo(748754.699);
        Assertions.assertThat(NumberUtils.roundDouble(number, 4)).isEqualTo(748754.6994);
        Assertions.assertThat(NumberUtils.roundDouble(number, 5)).isEqualTo(748754.69936);
        Assertions.assertThat(NumberUtils.roundDouble(number, 6)).isEqualTo(748754.699361);
        Assertions.assertThat(NumberUtils.roundDouble(number, 7)).isEqualTo(748754.6993614);
        Assertions.assertThat(NumberUtils.roundDouble(number, 8)).isEqualTo(748754.6993614);
        Assertions.assertThat(NumberUtils.roundDouble(number, 9)).isEqualTo(748754.699361401);




    }


}