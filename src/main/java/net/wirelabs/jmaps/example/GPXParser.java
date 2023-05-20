package net.wirelabs.jmaps.example;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.gpx.GpxType;
import net.wirelabs.jmaps.gpx.TrkType;
import net.wirelabs.jmaps.gpx.TrksegType;
import net.wirelabs.jmaps.gpx.WptType;
import net.wirelabs.jmaps.viewer.geo.Coordinate;

/**
 * Created 8/3/22 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public
class GPXParser {

    private Unmarshaller unmarshaller;

    public GPXParser() {

        try {
            JAXBContext jc = JAXBContext.newInstance("net.wirelabs.jmaps.gpx");
            this.unmarshaller = jc.createUnmarshaller();
        } catch (JAXBException e) {
            log.error("JAXB exception {}", e.getMessage(), e);
        }

    }


    /**
     * Parses gpx file in geoposition format
     *
     * @param file input file
     * @return list of waypoints in GeoPosition format
     */
    public List<Coordinate> parseToGeoPosition(File file) {

        return parseGpxFile(file).stream()
                .map(trackPoint -> new Coordinate(trackPoint.getLon().doubleValue(), trackPoint.getLat().doubleValue(), trackPoint.getEle().doubleValue()))
                .collect(Collectors.toList());

    }

    /**
     * Parses gpx file (note: all tracks and all segments are merged into one set of waypoints)
     *
     * @param file input file
     * @return list of waypoints in GPX's own Wpt format
     */
    public List<WptType> parseGpxFile(File file) {
        try {
            JAXBElement<GpxType> root = (JAXBElement<GpxType>) unmarshaller.unmarshal(file);

            List<TrkType> tracks = root.getValue().getTrk();
            List<WptType> result = new ArrayList<>();

            if (!tracks.isEmpty()) {
                for (TrkType track : tracks) {
                    track.getTrkseg().stream()
                            .map(TrksegType::getTrkpt)
                            .forEach(result::addAll);
                }
                return result;
            }

        } catch (JAXBException e) {
            log.warn("File does not contain a gpx track");
        }
        return Collections.emptyList();
    }


}




    