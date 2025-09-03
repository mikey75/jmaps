package net.wirelabs.jmaps.map.cache;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

//TODO: refine this client more
public class SimpleRedisClient implements Closeable {

    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;

    public SimpleRedisClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.input = socket.getInputStream();
        this.output = socket.getOutputStream();
    }

    // ---- Core RESP writer ----
    private void sendCommand(String... parts) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(parts.length).append("\r\n");
        for (String part : parts) {
            sb.append("$").append(part.getBytes().length).append("\r\n");
            sb.append(part).append("\r\n");
        }
        output.write(sb.toString().getBytes());
        output.flush();
    }

    private void sendCommandWithBinary(String cmd, String key, byte[] value) throws IOException {
        String commandHeader = "*3\r\n$%d\r\n%s\r\n$%d\r\n%s\r\n$%d\r\n";
        String header = String.format(commandHeader, cmd.length(), cmd, key.length(), key, value.length);
        output.write(header.getBytes());
        output.write(value);
        output.write("\r\n".getBytes());
        output.flush();
    }

    // ---- Core RESP reader ----
    private String readLine() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int b;
        while ((b = input.read()) != -1) {
            if (b == '\r') {
                int next = input.read();
                if (next == '\n') break;
                buffer.write(b);
                buffer.write(next);
            } else {
                buffer.write(b);
            }
        }
        return buffer.toString();
    }

    private byte[] readBulkString() throws IOException {
        String lenLine = readLine(); // e.g. "$123"
        if (lenLine.startsWith("$")) {
            int length = Integer.parseInt(lenLine.substring(1));
            if (length == -1) return null; // Redis nil

            byte[] data = input.readNBytes(length);
            input.readNBytes(2); // discard trailing \r\n
            return data;
        } else if (lenLine.startsWith("-")) {
            throw new IOException("Redis error: " + lenLine);
        }
        throw new IOException("Unexpected reply: " + lenLine);
    }

    // ---- Public API ----
    private void set(String key, byte[] value) throws IOException {
        sendCommandWithBinary("SET", key, value);
        String resp = readLine();
        if (!resp.equals("+OK")) {
            throw new IOException("SET failed: " + resp);
        }
    }

    private byte[] get(String key) throws IOException {
        sendCommand("GET", key);
        return readBulkString();
    }

    // ---- Helpers for Images ----
    public void setImage(String key, BufferedImage img, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, format, baos);
        set(key, baos.toByteArray());
    }

    public BufferedImage getImage(String key) throws IOException {
        byte[] data = get(key);
        if (data == null) return null;
        return ImageIO.read(new ByteArrayInputStream(data));
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}