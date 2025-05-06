package net.wirelabs.jmaps.map.cache;

import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.Defaults;
import net.wirelabs.jmaps.map.utils.ImageUtils;
import java.awt.image.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Duration;

@Slf4j
public class DBCache extends BaseCache implements Cache<String, BufferedImage> {

    private static final String CONNECTION_TEMPLATE = "jdbc:sqlite:%s/cache.db";

    private static final String CREATE_TABLE_SQL = "CREATE TABLE TILECACHE (tileUrl VARCHAR(1024) PRIMARY KEY,tileImg BLOB,timeStamp BIGINT)";
    private static final String GET_TEMPLATE_SQL = "select TILEIMG from TILECACHE where TILEURL='%s'";
    private static final String PUT_TEMPLATE_SQL = "INSERT INTO TILECACHE VALUES('%s', ?, ?)";
    private static final String UPDATE_TEMPLATE_SQL = "UPDATE TILECACHE set TILEIMG=?,TIMESTAMP=? WHERE TILEURL='%s'";
    private static final String GET_TIMESTAMP_TEMPLATE_SQL = "select TIMESTAMP from TILECACHE where TILEURL='%s'";
    private static final String COUNT_TEMPLATE_SQL = "select count (*) from TILECACHE where TILEURL='%s'";


    public DBCache() {
        super(Defaults.DEFAULT_TILE_CACHE_DB, Defaults.DEFAULT_CACHE_TIMEOUT);
        createDatabaseIfNotExists();
    }

    public DBCache(Path dbBaseDir, Duration cacheTimeout) {
        super(dbBaseDir, cacheTimeout);
        createDatabaseIfNotExists();
    }

    @Override
    public BufferedImage get(String key) {
        return getImage(key);
    }

    @Override
    public void put(String key, BufferedImage value) {
        putImage(key, value);
    }

    @Override
    public boolean keyExpired(String key) {
            return keyExpired(getTimestampFromDB(key));
    }

    Connection getConnection() throws SQLException {
        return DriverManager.getConnection(String.format(CONNECTION_TEMPLATE, getBaseDir()));
    }

    private void createDatabaseIfNotExists() {
        try {
            if (!getBaseDir().toFile().exists()) {
                Files.createDirectory(getBaseDir());
            }
            try (Connection dbConnection = getConnection(); Statement smt = dbConnection.createStatement()) {
                // create cache table
                if (!cacheTableExists(dbConnection)) {
                    smt.execute(CREATE_TABLE_SQL);
                }
                log.info("Connected and initialized: {}", dbConnection.getMetaData().getURL());
            }
        } catch (SQLException | IOException e) {
            log.info("Could not create connection!");
        }
    }

    private boolean cacheTableExists(Connection dbConnection) throws SQLException {
        if (dbConnection != null) {
            DatabaseMetaData metaData = dbConnection.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "TILECACHE", null)) {
                return rs.next();
            }
        }
        return false;
    }

    private void putImage(String key, BufferedImage value) {
        try {
            byte[] imgbytes = ImageUtils.imageToBytes(value);
            String sqlCmd;

            // always write entry - TileProvider decides if it's a re-load or new tile
            if (!entryExists(key)) {
                sqlCmd = String.format(PUT_TEMPLATE_SQL, key);
            } else {
                sqlCmd = String.format(UPDATE_TEMPLATE_SQL, key);
            }

            try (Connection connection = getConnection(); PreparedStatement ps = connection.prepareStatement(sqlCmd)) {
                ps.setBytes(1, imgbytes);
                ps.setLong(2, System.currentTimeMillis());
                ps.execute();
            }
        } catch (IOException | SQLException e) {
            log.warn("Cache put failed! {}", e.getMessage());
        }
    }

    private BufferedImage getImage(String key) {
        String query = String.format(GET_TEMPLATE_SQL, key);
        try {
            try (Connection connection = getConnection(); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) { // only one result is always expected from the query so no need to loop
                    byte[] is = rs.getBytes(1);
                    return ImageUtils.imageFromBytes(is);
                }
            }
            return null;
        } catch (IOException | SQLException e) {
            return null;
        }
    }

    private long getTimestampFromDB(String key) {

        try {
            String query = String.format(GET_TIMESTAMP_TEMPLATE_SQL, key);
            try (Connection connection = getConnection(); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) { // only one result is always expected from the query so no need to loop
                    return rs.getLong(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            log.warn("Getting timestamp from db entry failed! {}", e.getMessage());
        }
        return 0;
    }

    boolean entryExists(String key) throws SQLException {

        String query = String.format(COUNT_TEMPLATE_SQL, key);
        try (Connection connection = getConnection(); Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            return (rs.next() && rs.getInt(1) == 1);
        }
    }
}
