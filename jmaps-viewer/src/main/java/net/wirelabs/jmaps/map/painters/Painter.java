package net.wirelabs.jmaps.map.painters;

import lombok.Getter;
import lombok.Setter;
import net.wirelabs.jmaps.map.geo.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;

/**
 * Created 6/8/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Getter
@Setter
public abstract class Painter<T> {

    private List<Coordinate> objects = new ArrayList<>();   // objects being painted - in map coords
    public abstract void doPaint(Graphics2D graphics, T object, int width, int height);

}
