package net.wirelabs.jmaps.map.utils;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ImageUtilsTest {

    @Test
    void convertBufferedImageToByteAndBack() throws IOException {

        File imgFile = new File("src/test/resources/tiles/tile.png");
        BufferedImage img = ImageIO.read(imgFile);

        byte[] bytes = ImageUtils.imageToBytes(img);
        // now make bytes back into buffered image
        BufferedImage img2 = ImageUtils.imageFromBytes(bytes);
        // and check if they are the same data
        assertThat(ImageUtils.imagesEqual(img,img2)).isTrue();

    }

    @Test
    void shouldCompareImages() throws IOException {
        File imgFile1 = new File("src/test/resources/tiles/tile.png");
        File imgFile2 = new File("src/test/resources/tiles/tile-other.png");
        BufferedImage img1 = ImageIO.read(imgFile1);
        BufferedImage img2 = ImageIO.read(imgFile2);

        assertThat(ImageUtils.imagesEqual(img1,img2)).isFalse();


    }

}