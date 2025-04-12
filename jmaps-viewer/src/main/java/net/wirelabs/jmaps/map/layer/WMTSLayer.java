package net.wirelabs.jmaps.map.layer;


import lombok.extern.slf4j.Slf4j;
import net.opengis.ows.x11.DatasetDescriptionSummaryBaseType;
import net.opengis.wmts.x10.CapabilitiesDocument;
import net.opengis.wmts.x10.TileMatrixSetDocument;
import net.wirelabs.jmaps.map.geo.GeoUtils;
import net.wirelabs.jmaps.map.utils.UrlBuilder;
import net.wirelabs.jmaps.model.map.LayerDocument;

import java.awt.*;
import java.awt.geom.*;
import java.util.Optional;

import static net.wirelabs.jmaps.map.readers.WMTSCapReader.loadCapabilities;


/**
 * Created 5/20/23 by Michał Szwaczko (mikey@wirelabs.net)
 * This class represents most common wmts layer
 * If you want to connect to more sophisticated wmts services
 * with custom urls etc you should extend this class into your own
 */
@Slf4j
public class WMTSLayer extends Layer {

    private static final String DEFAULT_GET_CAPABILITIES_PATH = "?service=WMTS&request=GetCapabilities";
    private final CapabilitiesDocument.Capabilities capabilities = loadCapabilities(getCapabilitiesUrl());
    private TileMatrixSetDocument.TileMatrixSet tms;
    private final UrlBuilder urlBuilder = new UrlBuilder();

    protected String tmsName;
    protected String layerName;

    public WMTSLayer(LayerDocument.Layer layerDefinition) {

        super(layerDefinition);

        tmsName = layerDefinition.getTileMatrixSet();
        layerName = layerDefinition.getWmtsLayer();

        // if tms/layer is not specified
        // or tms/layer values given are not found in the capabilities file
        // set first ones found in capabilities file
        if (isEmpty(tmsName) || tmsNotExistent(tmsName))
            tmsName = capabilities.getContents().getTileMatrixSetList().get(0).getIdentifier().getStringValue();
        if (isEmpty(layerName) || layerNotExistent(layerName))
            layerName = capabilities.getContents().getDatasetDescriptionSummaryList().get(0).getIdentifier().getStringValue();

        findTileMatrixSetById(tmsName).ifPresentOrElse(tileMatrixSet -> {
            tms = tileMatrixSet;
            if (isEmpty(layerDefinition.getCrs())) {
                String crsName = GeoUtils.parseCrsUrn(tileMatrixSet.getSupportedCRS());
                setProjection(crsName);
                crs = crsName;
            }
            setMaxZoom(tileMatrixSet.getTileMatrixList().size() - 1);
            setTileSize(tileMatrixSet.getTileMatrixList().get(0).getTileWidth().intValue());
        }, () -> {
            log.warn("Cannot parse/setup layer " + layerDefinition.getName());
            log.warn("Setting global best-effort defaults");
            setMaxZoom(LayerDefaults.MAX_ZOOM);
            setTileSize(LayerDefaults.TILE_SIZE);
            setCrs(LayerDefaults.CRS);

        });

    }

    private Optional<TileMatrixSetDocument.TileMatrixSet> findTileMatrixSetById(String id) {

        return capabilities.getContents().getTileMatrixSetList()
                .stream()
                .filter(x -> x.getIdentifier().getStringValue().equals(id))
                .findFirst();
    }

    private Optional<DatasetDescriptionSummaryBaseType> findLayerById(String id) {
        //capabilities.getContents().getDatasetDescriptionSummaryList().get(0).getIdentifier().getStringValue(
        return capabilities.getContents().getDatasetDescriptionSummaryList()
                .stream()
                .filter(x -> x.getIdentifier().getStringValue().equals(id))
                .findFirst();
    }


    boolean tmsNotExistent(String tms) {
        Optional<TileMatrixSetDocument.TileMatrixSet> xx = findTileMatrixSetById(tms);
        if (xx.isEmpty()) {
            log.info("TileMatrixSet {} not existing in Capabilities. Setting default", tms);
        }
        return xx.isEmpty();
    }

    boolean layerNotExistent(String layer) {
        Optional<DatasetDescriptionSummaryBaseType> xx = findLayerById(layer);
        if (xx.isEmpty()) {
            log.info("Layer  {} not existing in Capabilities. Setting default", layer);
        }
        return xx.isEmpty();
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

        int width = tms.getTileMatrixList().get(zoom).getMatrixWidth().intValue();
        int height = tms.getTileMatrixList().get(zoom).getMatrixHeight().intValue();
        return new Dimension(width, height);
    }

    // todo: add style and format (recognize from capabilities)
    @Override
    public String createTileUrl(int x, int y, int zoom) {
        return urlBuilder.parse(url)
                .addParam("Service", "WMTS")
                .addParam("Request", "GetTile")
                .addParam("Layer", layerName)
                .addParam("Version", "1.0.0")
                .addParam("format", "image/png")
                .addParam("style", "default")
                .addParam("TileMatrixSet", tmsName)
                .addParam("TileMatrix", tms.getTileMatrixList().get(zoom).getIdentifier().getStringValue())
                .addParam("TileRow", String.valueOf(y))
                .addParam("TileCol", String.valueOf(x))
                .build();

    }


    @Override
    public Point2D getTopLeftCornerInMeters() {

        double x = (double) tms.getTileMatrixList().get(0).getTopLeftCorner().get(0);
        double y = (double) tms.getTileMatrixList().get(0).getTopLeftCorner().get(1);
        if (swapAxis) {
            return new Point2D.Double(y, x);
        } else {
            return new Point2D.Double(x, y);
        }
    }


    @Override
    public double getMetersPerPixelAtZoom(int zoom) {
        return tms.getTileMatrixList().get(zoom).getScaleDenominator() * 0.00028;
    }
}
