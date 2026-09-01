package net.wirelabs.jmaps.map.cache.db;

import lombok.extern.slf4j.Slf4j;
import net.wirelabs.jmaps.map.Defaults;
import net.wirelabs.jmaps.map.cache.BaseCache;
import net.wirelabs.jmaps.map.cache.Cache;
import net.wirelabs.jmaps.map.utils.ImageUtils;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DBCache extends BaseCache implements Cache<String, BufferedImage>, Closeable {

    private static final String CONNECTION_TEMPLATE = "jdbc:sqlite:%s/cache.db";
    private static final int POOL_SIZE = 32;

    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS TILECACHE (tileUrl VARCHAR(1024) PRIMARY KEY, tileImg BLOB, timeStamp BIGINT)";
    private static final String GET_SQL = "SELECT tileImg FROM TILECACHE WHERE tileUrl = ?";
    private static final String PUT_SQL = "INSERT OR REPLACE INTO TILECACHE VALUES (?, ?, ?)";
    private static final String GET_TIMESTAMP_SQL = "SELECT timeStamp FROM TILECACHE WHERE tileUrl = ?";

    private final BlockingQueue<Connection> pool = new LinkedBlockingQueue<>();

    public DBCache() {
        this(Defaults.DEFAULT_TILE_CACHE_DB, Defaults.DEFAULT_CACHE_TIMEOUT);
    }

    public DBCache(Path dbBaseDir, Duration cacheTimeout) {
        super(dbBaseDir, cacheTimeout);
        initializePool(dbBaseDir);
    }

    private void initializePool(Path dbBaseDir) {
        try {
            if (!dbBaseDir.toFile().exists()) {
                Files.createDirectories(dbBaseDir);
            }
            for (int i = 0; i < POOL_SIZE; i++) {
                pool.add(createConnection(dbBaseDir));
            }
            log.info("Initialized {} pooled connections at {}", POOL_SIZE, dbBaseDir);
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Could not initialize cache database pool", e);
        }
    }

    private Connection createConnection(Path dbBaseDir) throws SQLException {
        Connection conn = DriverManager.getConnection(String.format(CONNECTION_TEMPLATE, dbBaseDir));
        try (Statement pragmas = conn.createStatement()) {
            pragmas.execute("PRAGMA journal_mode=WAL");
            pragmas.execute("PRAGMA busy_timeout=5000");
            pragmas.execute("PRAGMA synchronous=NORMAL");
            pragmas.execute(CREATE_TABLE_SQL);
        }
        return conn;
    }

    /** Borrow a connection, run work, always return it to the pool. */
    private <T> T withConnection(ConnectionWork<T> work) {
        Connection conn = null;
        try {
            conn = pool.poll(10, TimeUnit.SECONDS);
            if (conn == null) {
                throw new SQLException("Timed out waiting for a pooled connection");
            }
            return work.run(conn);
        } catch (SQLException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Cache operation failed", e);
            return null;
        } finally {
            if (conn != null) {
                pool.offer(conn); // return to pool for reuse
            }
        }
    }

    @FunctionalInterface
    private interface ConnectionWork<T> {
        T run(Connection conn) throws SQLException;
    }

    @Override
    public BufferedImage get(String key) {
        return withConnection(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(GET_SQL)) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        try {
                            return ImageUtils.imageFromBytes(rs.getBytes(1));
                        } catch (IOException e) {
                            log.debug("Failed to decode cached image for key {}", key, e);
                            return null;
                        }
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void put(String key, BufferedImage value) {
        withConnection(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(PUT_SQL)) {
                ps.setString(1, key);
                ps.setBytes(2, ImageUtils.imageToBytes(value));
                ps.setLong(3, System.currentTimeMillis());
                ps.execute();
            } catch (IOException e) {
                log.warn("Cache put failed for key {}", key, e);
            }
            return null;
        });
    }

    @Override
    public boolean keyExpired(String key) {
        return keyExpired(getTimestampFromDB(key));
    }

    private long getTimestampFromDB(String key) {
        Long result = withConnection(conn -> {
            try (PreparedStatement ps = conn.prepareStatement(GET_TIMESTAMP_SQL)) {
                ps.setString(1, key);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0L;
                }
            }
        });
        return result != null ? result : 0L;
    }

    @Override
    public void close() {
        for (Connection conn : pool) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Failed to close pooled connection", e);
            }
        }
    }

}