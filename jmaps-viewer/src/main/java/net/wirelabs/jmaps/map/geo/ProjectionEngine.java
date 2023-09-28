package net.wirelabs.jmaps.map.geo;

import lombok.Getter;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;

import static net.wirelabs.jmaps.map.utils.GeoUtils.TWO_PI;

/**
 * Created 5/22/23 by Michał Szwaczko (mikey@wirelabs.net)
 * Contains projections and transformations for given layer
 * uses proj4j - great library from osgeo.
 */
@Getter
public class ProjectionEngine {

    private final CoordinateReferenceSystem crs;
    private final ProjCoordinate projectionResult = new ProjCoordinate();
    private final ProjCoordinate projectionSource = new ProjCoordinate();
    private final CRSFactory csFactory = new CRSFactory();

    public ProjectionEngine(String crsName) {
        crs = csFactory.createFromName(crsName);
    }

    //Projects geographical coordinates into coordinates in this crs units
    public Coordinate project(Coordinate coord) {
        projectionSource.setValue(coord.longitude, coord.latitude, coord.altitude);
        crs.getProjection().project(projectionSource, projectionResult);
        return new Coordinate(projectionResult.x, projectionResult.y, projectionResult.z);
    }

    //Given a projected coordinate returns the corresponding LatLng .
    public Coordinate unproject(Coordinate coord) {
        projectionSource.setValue(coord.longitude, coord.latitude, coord.altitude);
        crs.getProjection().inverseProject(projectionSource, projectionResult);
        return new Coordinate(projectionResult.x, projectionResult.y, projectionResult.z);

    }

    // get the the equator length (2PI*Radius) where radius is
    // the equatorial radius or semi-major axis
    public double getEquatorLength() {
        return crs.getProjection().getEllipsoid().equatorRadius * TWO_PI;
    }

    //get the polar meridian length (2PI*Radius) where radius is
    // the polar radius or semi-minor axis
    public double getPolarLength() {
        return crs.getProjection().getEllipsoid().poleRadius * TWO_PI;
    }




}
