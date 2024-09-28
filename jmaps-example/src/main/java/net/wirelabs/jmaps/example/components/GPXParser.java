package net.wirelabs.jmaps.example.components;

import com.topografix.gpx.x1.x1.*;
import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.geo.Coordinate;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created 8/3/22 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class GPXParser {

    /**
     * Parses gpx file in geoposition format
     *
     * @param file input file
     * @return list of waypoints in GeoPosition format
     */
    public List<Coordinate> parseToGeoPosition(File file) {

        return parseGpxFile(file).stream()
                .map(this::trackPointToCoordinate)
                .collect(Collectors.toList());

    }

    private Coordinate trackPointToCoordinate(WptType trackPoint) {
        Coordinate coordinate;

        if (trackPoint.getEle() == null) {
            // gpx routes have no elevation
            coordinate = new Coordinate(trackPoint.getLon().doubleValue(), trackPoint.getLat().doubleValue());
        } else {
            // but tracks have lat/lon/ele
            coordinate = new Coordinate(trackPoint.getLon().doubleValue(), trackPoint.getLat().doubleValue(), trackPoint.getEle().doubleValue());
        }
        return coordinate;
    }

    /**
     * Parses gpx file (note: all tracks and all segments are merged into one set of waypoints)
     *
     * @param file input file
     * @return list of waypoints in GPX's own Wpt format
     */
    public List<WptType> parseGpxFile(File file) {
        try {
            GpxDocument gpxDocument = GpxDocument.Factory.parse(file);

            List<TrkType> tracks = gpxDocument.getGpx().getTrkList();
            List<WptType> result = new ArrayList<>();

            if (!tracks.isEmpty()) {
                for (TrkType track : tracks) {
                    track.getTrksegList().stream()
                            .map(TrksegType::getTrkptList)
                            .forEach(result::addAll);
                }
                return result;
            }

        } catch (IOException | XmlException e) {
            log.warn("File does not contain a gpx track");
        }
        return Collections.emptyList();
    }


}
