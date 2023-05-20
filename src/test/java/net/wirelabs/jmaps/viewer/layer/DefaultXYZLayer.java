package net.wirelabs.jmaps.viewer.layer;

import net.wirelabs.jmaps.viewer.map.layer.XYZLayer;

/**
 * Created 5/20/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class DefaultXYZLayer extends XYZLayer {

    public DefaultXYZLayer() {
        super("TestXYZ", "http://localhost/{z}/{x}/{y}.png");
    }

}
