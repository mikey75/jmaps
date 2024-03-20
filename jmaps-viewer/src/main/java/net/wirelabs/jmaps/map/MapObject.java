package net.wirelabs.jmaps.map;

import lombok.Getter;
import lombok.Setter;
import net.wirelabs.jmaps.map.layer.Layer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
public class MapObject {

    @Setter private String mapName = "[no name defined]";
    @Setter private String mapCopyrightAttribution = "[no attribution in definition]";
    private final List<Layer> layers = new CopyOnWriteArrayList<>();

    public Layer getBaseLayer() {
        return layers.get(0);
    }

    public boolean layersPresent() {
        return !layers.isEmpty();
    }

    public List<Layer> getEnabledLayers() {
        return layers.stream()
                .filter(Layer::isEnabled)
                .collect(Collectors.toList());
    }

    public int getMaxZoom() {
        return layers.stream()
                .map(layer -> layer.getMaxZoom() - layer.getZoomOffset())
                .mapToInt(v -> v)
                .min().orElse(0);
    }

    public int getMinZoom() {
        return layers
                .stream()
                .map(Layer::getMinZoom)
                .mapToInt(val -> val)
                .max().orElse(0);
    }

    public boolean isMultilayer() {
        return layers.size() > 1;
    }

    public void addLayer(Layer layer) {
        layers.add(layer);
    }
}
