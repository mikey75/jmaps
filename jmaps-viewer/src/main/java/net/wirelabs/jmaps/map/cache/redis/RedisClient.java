package net.wirelabs.jmaps.map.cache.redis;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Slf4j
public final class RedisClient implements Closeable {

    private final Duration ttl;
    private final BlockingQueue<RedisConnection> pool;

    public RedisClient(String host, int port, Duration ttl, int poolSize) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("TTL must be a positive duration");
        }
        if (poolSize <= 0) {
            throw new IllegalArgumentException("Pool size must be greater than 0");
        }

        this.ttl = ttl;
        this.pool = new ArrayBlockingQueue<>(poolSize);

        try {
            for (int i = 0; i < poolSize; i++) {
                pool.add(new RedisConnection(host, port));
            }
        } catch (IOException ex) {
            // fail fast — half-initialized client is worse than no client
            throw new IllegalStateException("Failed to initialize Redis connections", ex);
        }
    }

    private <T> T withConnection(IOFunction<RedisConnection, T> fn) throws IOException {
        RedisConnection conn = null;
        try {
            conn = pool.take();
            return fn.apply(conn);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for Redis connection", e);
        } catch (IOException e) {
            // remove bad connection from pool
            if (conn != null) {
                safeClose(conn);
                conn = null;
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    pool.put(conn); // blocks until space available, safer than offer()
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    safeClose(conn);
                }
            }
        }
    }

    // ---- Public API ----

    public void putImage(String key, BufferedImage img, String format) throws IOException {
        try (var baos = new ByteArrayOutputStream()) {
            boolean ok = ImageIO.write(img, format, baos);
            if (!ok) {
                throw new IOException("Unsupported image format: " + format);
            }
            final byte[] bytes = baos.toByteArray();
            withConnection(conn -> {
                conn.put(key, bytes, ttl);
                return null;
            });
        }
    }

    public BufferedImage getImage(String key) throws IOException {
        byte[] data = withConnection(conn -> conn.get(key));
        return (data == null) ? null : ImageIO.read(new ByteArrayInputStream(data));
    }

    @Override
    public void close() throws IOException {
        IOException ex = null;
        for (RedisConnection conn : pool) {
            try {
                conn.close();
            } catch (IOException e) {
                if (ex == null) ex = e;
                else ex.addSuppressed(e);
            }
        }
        if (ex != null) throw ex;
    }

    // ---- Helpers ----
    private void safeClose(RedisConnection conn) {
        try {
            conn.close();
        } catch (IOException e) {
            log.warn("Error closing Redis connection: {}", e.getMessage());
        }
    }
}