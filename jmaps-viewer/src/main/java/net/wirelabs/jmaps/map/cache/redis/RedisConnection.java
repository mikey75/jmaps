package net.wirelabs.jmaps.map.cache.redis;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class RedisConnection implements Closeable {
    private final Socket socket;
    private final BufferedInputStream input;
    private final BufferedOutputStream output;

    RedisConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.socket.setSoTimeout(5000);
        this.input = new BufferedInputStream(socket.getInputStream());
        this.output = new BufferedOutputStream(socket.getOutputStream());
    }

    private void sendCommand(String... parts) throws IOException {
        var sb = new StringBuilder();
        sb.append("*").append(parts.length).append("\r\n");
        for (var part : parts) {
            var bytes = part.getBytes(StandardCharsets.UTF_8);
            sb.append("$").append(bytes.length).append("\r\n");
            output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            output.write(bytes);
            output.write("\r\n".getBytes(StandardCharsets.UTF_8));
            sb.setLength(0);
        }
        output.flush();
    }

    private void sendCommandWithBinary(String cmd, String key, byte[] value, long ttlSeconds) throws IOException {
        var keyBytes = key.getBytes(StandardCharsets.UTF_8);
        var ttlBytes = Long.toString(ttlSeconds).getBytes(StandardCharsets.UTF_8);

        var header = String.format(
                "*5\r\n$%d\r\n%s\r\n$%d\r\n%s\r\n$%d\r\n",
                cmd.length(), cmd,
                keyBytes.length, key,
                value.length
        );
        output.write(header.getBytes(StandardCharsets.UTF_8));
        output.write(value);
        output.write("\r\n$2\r\nEX\r\n".getBytes(StandardCharsets.UTF_8));
        output.write(("$" + ttlBytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(ttlBytes);
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    private String readLine() throws IOException {
        var buffer = new ByteArrayOutputStream();
        int b;
        while ((b = input.read()) != -1) {
            if (b == '\r') {
                if (input.read() == '\n') break;
            } else {
                buffer.write(b);
            }
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private Object readResp() throws IOException {
        int prefix = input.read();
        if (prefix == -1) throw new EOFException("Redis connection closed");

        return switch (prefix) {
            case '+' -> readLine(); // simple string
            case '-' -> throw new RedisException(readLine());
            case '$' -> {
                var length = Integer.parseInt(readLine());
                if (length == -1) yield null; // nil bulk string
                var data = input.readNBytes(length);
                input.readNBytes(2); // discard \r\n
                yield data;
            }
            default -> throw new IOException("Unexpected RESP prefix: " + (char) prefix);
        };
    }

    public void put(String key, byte[] value, Duration ttl) throws IOException {
        sendCommandWithBinary("SET", key, value, ttl.getSeconds());
        var resp = readResp();
        if (!(resp instanceof String s) || !"OK".equals(s)) {
            throw new RedisException("PUT failed: " + resp);
        }
    }

    public byte[] get(String key) throws IOException {
        sendCommand("GET", key);
        var resp = readResp();
        return (resp instanceof byte[] data) ? data : null;
    }

    @Override
    public void close() throws IOException {
        try {
            sendCommand("QUIT");
            readResp(); // expect +OK
        } catch (Exception ignore) {
        } finally {
            socket.close();
        }
    }
}