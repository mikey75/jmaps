package net.wirelabs.jmaps;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

public class MockHttpServer {

    private HttpServer httpServer;
    private static final File VALID_CAPABILITIES_FILE = new File("src/test/resources/wmts/capabilities.xml");
    private static final File INVALID_CAPABILITIES_FILE = new File("src/test/resources/wmts/invalid.xml");
    public static final File TEST_TILE_FILE = new File("src/test/resources/tiles/tile.png");

    @Getter
    int port;

    public MockHttpServer(int port) {
        this.port = port;
        try {
            //Create HttpServer which is listening on the given port
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
            //Create a new context for the given context and handler
            httpServer.createContext("/", new BasicHttpHandler());
            //Create a default executor
            httpServer.setExecutor(null);
            httpServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public MockHttpServer() throws IOException {
        this(getRandomFreeTcpPort());    // default port for tests
    }

    protected static int getRandomFreeTcpPort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    public static class BasicHttpHandler implements HttpHandler {



        public void handle(HttpExchange exchange) throws IOException {


            URI uri = exchange.getRequestURI();
            String uriString = uri.toString();
            OutputStream os;

            switch (uriString) {
                case "/valid1", "/valid1?service=WMTS&request=GetCapabilities", "/valid2?service=WMTS&request=GetCapabilities" -> {
                    exchange.sendResponseHeaders(HTTP_OK, VALID_CAPABILITIES_FILE.length());
                    os = exchange.getResponseBody();
                    os.write(Files.readAllBytes(VALID_CAPABILITIES_FILE.toPath()));
                    os.close();

                }
                case "/invalid" -> {
                    exchange.sendResponseHeaders(HTTP_OK, INVALID_CAPABILITIES_FILE.length());
                    os = exchange.getResponseBody();
                    os.write(Files.readAllBytes(INVALID_CAPABILITIES_FILE.toPath()));
                    os.close();

                }
                case "/tile.png" -> {
                    exchange.sendResponseHeaders(HTTP_OK, TEST_TILE_FILE.length());
                    // TODO set the Content-Type header to image/gif

                    os = exchange.getResponseBody();
                    Files.copy(TEST_TILE_FILE.toPath(), os);
                    os.close();

                }

                default -> {
                    exchange.sendResponseHeaders(HTTP_NOT_FOUND, 0);
                    os = exchange.getResponseBody();
                    os.write(' ');
                    os.close();
                }
            }

        }

    }
    public void stop() {
        httpServer.stop(2);
    }

}
