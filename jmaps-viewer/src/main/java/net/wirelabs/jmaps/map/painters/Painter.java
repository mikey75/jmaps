package net.wirelabs.jmaps.map.painters;

import java.awt.*;

/**
 * Created 6/8/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public interface Painter<T> {
    void doPaint(Graphics2D graphics, T object, int width, int height);
}
