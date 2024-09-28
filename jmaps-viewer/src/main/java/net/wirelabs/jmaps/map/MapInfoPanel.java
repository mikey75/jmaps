package net.wirelabs.jmaps.map;

import net.wirelabs.jmaps.map.layer.Layer;

import javax.swing.*;
import javax.swing.border.*;

public class MapInfoPanel extends JPanel {

    private final MapViewer mapViewer;

    public MapInfoPanel(MapViewer mapViewer) {
        this.mapViewer = mapViewer;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBorder(new TitledBorder("Layers"));
        setVisible(false); // invisible by default
    }

    public void addLayers() {

        removeAll();

        for (Layer layer: mapViewer.getCurrentMap().getLayers()) {
            JCheckBox layerCheckbox = new JCheckBox(layer.getName());
            layerCheckbox.setSelected(layer.isEnabled());
            layerCheckbox.addActionListener( e -> {
                layer.setEnabled(layerCheckbox.isSelected());
                mapViewer.repaint();
            });
            add(layerCheckbox);
        }
        updateUI(); // hack to update overlay panel
    }
}
