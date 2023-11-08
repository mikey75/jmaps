package net.wirelabs.jmaps.map;

import net.wirelabs.jmaps.map.layer.Layer;
import net.wirelabs.jmaps.map.layer.LayerManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class LayersPanel extends JPanel {

    private final MapViewer mapViewer;
    private final transient LayerManager layerManager;

    public LayersPanel(MapViewer mapViewer, LayerManager layerManager) {
        this.mapViewer = mapViewer;
        this.layerManager = layerManager;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBorder(new TitledBorder("Layers"));
        setVisible(false); // invisible by default
    }

    public void addLayers() {

        removeAll();

        for (Layer layer: layerManager.getLayers()) {
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
