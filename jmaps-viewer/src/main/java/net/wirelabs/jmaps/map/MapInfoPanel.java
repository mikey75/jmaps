package net.wirelabs.jmaps.map;

import net.wirelabs.jmaps.map.layer.Layer;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class MapInfoPanel extends JPanel {

    private final MapViewer mapViewer;
    private final transient MapManager mapManager;

    public MapInfoPanel(MapViewer mapViewer, MapManager mapManager) {
        this.mapViewer = mapViewer;
        this.mapManager = mapManager;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setBorder(new TitledBorder("Layers"));
        setVisible(false); // invisible by default
    }

    public void addLayers() {

        removeAll();

        for (Layer layer: mapManager.getLayers()) {
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
