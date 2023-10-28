package net.wirelabs.jmaps.map.layer;



import net.wirelabs.jmaps.map.model.wmts.Capabilities;
import net.wirelabs.jmaps.map.geo.Coordinate;
import net.wirelabs.jmaps.map.geo.ProjectionEngine;
import net.wirelabs.jmaps.map.geo.GeoUtils;
import net.wirelabs.jmaps.map.MapReader;
import okhttp3.HttpUrl;


import java.awt.Dimension;
import java.awt.geom.Point2D;


/**
 * Created 5/20/23 by Michał Szwaczko (mikey@wirelabs.net)
 * This class represents most common wmts layer
 * If you want to connect to more sophisticated wmts services
 * with custom urls etc you should extend this class into your own
 */

public class WMTSLayer extends Layer {

    private static final String DEFAULT_GET_CAPABILITIES_PATH = "?service=WMTS&request=GetCapabilities";
    private final MapReader mapReader;
    private Capabilities capabilities;

    protected String tileMatrixSetName;
    protected String wmtsLayerName;
   

    public WMTSLayer(String name, String url, String layer, String tileMatrixSet) {

        super(name, url);
        this.tileMatrixSetName = tileMatrixSet;
        this.wmtsLayerName = layer;
        this.mapReader = new MapReader();
        configureFromCapabilities();
    }

    public WMTSLayer(String name, String url) {
        this(name, url, null, null);
    }


    /**
     * Override this method if wmts service has nonstandard 'getCapabilities' path
     */
    protected String getCapabilitiesUrl() {
        return url + DEFAULT_GET_CAPABILITIES_PATH;
    }

    private void configureFromCapabilities() {

        capabilities = mapReader.loadCapabilities(getCapabilitiesUrl());

        // defaults to first layer and first tms when not specified
        if (wmtsLayerName == null) wmtsLayerName = capabilities.getContents().getLayer(0).getIdentifier();
        if (tileMatrixSetName == null) tileMatrixSetName = capabilities.getContents().getLayer(0).getTileMatrixSetLink(0).getTileMatrixSet();

        // else use specified layer and tms
        maxZoom = (capabilities.getContents().getTileMatrixSet(tileMatrixSetName).getTileMatrices().length - 1);
        tileSize = (capabilities.getContents().getTileMatrixSet(tileMatrixSetName).getTileMatrix(0).getTileWidth());
        projectionEngine = (new ProjectionEngine(GeoUtils.parseCrsUrn(capabilities.getContents().getTileMatrixSet(tileMatrixSetName).getSupportedCRS())));
    }

    @Override
    public Dimension getMapSize(int zoom) {

        int width = capabilities.getContents().getTileMatrixSet(tileMatrixSetName).getTileMatrix(zoom).getMatrixWidth();
        int height = capabilities.getContents().getTileMatrixSet(tileMatrixSetName).getTileMatrix(zoom).getMatrixHeight();
        return new Dimension(width, height);
    }

    // todo: add style and format (recognize from capabilities)
    @Override
    public String createTileUrl(int x, int y, int zoom) {

        return HttpUrl.parse(url).newBuilder().
                addQueryParameter("Service", "WMTS")
                .addQueryParameter("Request", "GetTile")
                .addQueryParameter("Layer", wmtsLayerName)
                .addQueryParameter("Version", "1.0.0")
                .addQueryParameter("format", "image/png")
                .addQueryParameter("style", "default")
                .addQueryParameter("TileMatrixSet", tileMatrixSetName)
                .addQueryParameter("TileMatrix", capabilities.getContents().getTileMatrixSet(tileMatrixSetName).getTileMatrix(zoom).getIdentifier())
                .addQueryParameter("TileRow", String.valueOf(y))
                .addQueryParameter("TileCol", String.valueOf(x))
                .toString();

    }

    @Override
    public Point2D getTopLeftCorner() {
        double[] tlc = capabilities.getContents().getTileMatrixSet(tileMatrixSetName)
                .getTileMatrix(0).getTopLeftCorner();
        if (swapAxis) {
            return new Point2D.Double(tlc[1], tlc[0]);
        } else {
            return new Point2D.Double(tlc[0], tlc[1]);
        }
    }

    @Override
    public Point2D latLonToPixel(Coordinate latLon, int zoom) {

        Coordinate coord = getProjectionEngine().project(latLon);
        Point2D tlc = getTopLeftCorner();

        double longitude = (coord.getLongitude() - tlc.getX()) / getMetersPerPixel(zoom);
        double latitude = (tlc.getY() - coord.getLatitude()) / getMetersPerPixel(zoom);

        return new Point2D.Double(longitude, latitude);
    }

    @Override
    public Coordinate pixelToLatLon(Point2D pixel, int zoom) {
        Point2D tlc = getTopLeftCorner();

        Coordinate coord = new Coordinate(
                tlc.getX() + (pixel.getX() * getMetersPerPixel(zoom)),
                tlc.getY() - (pixel.getY() * getMetersPerPixel(zoom)));

        return getProjectionEngine().unproject(coord);
    }

    private double getMetersPerPixel(int zoom) {
        return capabilities.getContents().getTileMatrixSet(tileMatrixSetName).getTileMatrix(zoom).getScaleDenominator() * 0.00028;
    }
}
