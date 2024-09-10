package net.wirelabs.jmaps.map.layer;


import net.wirelabs.jmaps.map.geo.GeoUtils;
import net.wirelabs.jmaps.map.model.wmts.Capabilities;
import net.wirelabs.jmaps.model.map.LayerDocument;
import okhttp3.HttpUrl;

import java.awt.*;
import java.awt.geom.Point2D;

import static net.wirelabs.jmaps.map.readers.WMTSCapReader.loadCapabilities;


/**
 * Created 5/20/23 by Michał Szwaczko (mikey@wirelabs.net)
 * This class represents most common wmts layer
 * If you want to connect to more sophisticated wmts services
 * with custom urls etc you should extend this class into your own
 */

public class WMTSLayer extends Layer {

    private static final String DEFAULT_GET_CAPABILITIES_PATH = "?service=WMTS&request=GetCapabilities";
    private final Capabilities capabilities;

    protected String defaultTms;
    protected String defaultLayer;

    public WMTSLayer(LayerDocument.Layer layerDefinition) {

        super(layerDefinition);

        capabilities = loadCapabilities(getCapabilitiesUrl());

        defaultTms = layerDefinition.getTileMatrixSet();
        defaultLayer = layerDefinition.getWmtsLayer();


        if (isEmpty(defaultTms))
            defaultTms = capabilities.getContents().getLayer(0).getTileMatrixSetLink(0).getTileMatrixSet();
        if (isEmpty(defaultLayer))
            defaultLayer = capabilities.getContents().getLayer(0).getIdentifier();

        if (isEmpty(layerDefinition.getCrs())) {
            String crsName = GeoUtils.parseCrsUrn(capabilities.getContents().getTileMatrixSet(defaultTms).getSupportedCRS());
            setProjection(crsName);
            crs =  crsName;
        }
        setMaxZoom((capabilities.getContents().getTileMatrixSet(defaultTms).getTileMatrices().length - 1));
        setTileSize((capabilities.getContents().getTileMatrixSet(defaultTms).getTileMatrix(0).getTileWidth()));

    }


    boolean isEmpty(String s) {
        return (s == null || s.isBlank());
    }


    /**
     * Override this method if wmts service has nonstandard 'getCapabilities' path
     */
    protected String getCapabilitiesUrl() {
        return url + DEFAULT_GET_CAPABILITIES_PATH;
    }


    @Override
    public Dimension getSizeInTiles(int zoom) {

        int width = capabilities.getContents().getTileMatrixSet(defaultTms).getTileMatrix(zoom).getMatrixWidth();
        int height = capabilities.getContents().getTileMatrixSet(defaultTms).getTileMatrix(zoom).getMatrixHeight();
        return new Dimension(width, height);
    }

    // todo: add style and format (recognize from capabilities)
    @Override
    public String createTileUrl(int x, int y, int zoom) {

        return HttpUrl.parse(url).newBuilder().
                addQueryParameter("Service", "WMTS")
                .addQueryParameter("Request", "GetTile")
                .addQueryParameter("Layer", defaultLayer)
                .addQueryParameter("Version", "1.0.0")
                .addQueryParameter("format", "image/png")
                .addQueryParameter("style", "default")
                .addQueryParameter("TileMatrixSet", defaultTms)
                .addQueryParameter("TileMatrix", capabilities.getContents().getTileMatrixSet(defaultTms).getTileMatrix(zoom).getIdentifier())
                .addQueryParameter("TileRow", String.valueOf(y))
                .addQueryParameter("TileCol", String.valueOf(x))
                .toString();

    }

    @Override
    public Point2D getTopLeftCornerInMeters() {
        double[] tlc = capabilities.getContents().getTileMatrixSet(defaultTms)
                .getTileMatrix(0).getTopLeftCorner();
        if (swapAxis) {
            return new Point2D.Double(tlc[1], tlc[0]);
        } else {
            return new Point2D.Double(tlc[0], tlc[1]);
        }
    }


    @Override
    public double getMetersPerPixelAtZoom(int zoom) {
        return capabilities.getContents().getTileMatrixSet(defaultTms).getTileMatrix(zoom).getScaleDenominator() * 0.00028;
    }
}
