package net.wirelabs.jmaps.map.layer;

import net.wirelabs.jmaps.model.map.LayerDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuadLayerTest {

    private LayerDocument.Layer layerDefinition;

    @BeforeEach
    void before() {
        layerDefinition = LayerDocument.Layer.Factory.newInstance();
        layerDefinition.setType(String.valueOf(LayerType.QUAD));
        layerDefinition.setName("TestQuad");
    }

    @Test
    void shouldCreateCorrectQuadUrl() {
        layerDefinition.setUrl("http://quad-tile-service.net/tiles/{quad}.png");
        Layer quadLayer = new QuadLayer(layerDefinition);

        String finalUrl = quadLayer.createTileUrl(11, 12, 14);
        assertThat(finalUrl).isEqualTo("http://quad-tile-service.net/tiles/00000000003211.png");
    }

    @Test
    void shouldCreateVirtEarthQuadCharScalingAdditionUrl() {
        // quadchar - this is automatic scaling mechanism,
        // hosts numbers are taken from the last digit of a quadString
        // this is used by virtual earth maps - not part of quad spec per se

        layerDefinition.setUrl("http://host{quadchar}-quad-tile-service.net/tiles/{quad}.png");
        Layer quadLayer = new QuadLayer(layerDefinition);

        String finalUrl = quadLayer.createTileUrl(18, 12, 12);
        assertThat(finalUrl).isEqualTo("http://host0-quad-tile-service.net/tiles/000000012210.png");

    }

}