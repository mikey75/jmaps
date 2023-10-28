package net.wirelabs.jmaps;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Created 5/27/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class TestHttpServer {

    private final MockWebServer server = new MockWebServer();

    public TestHttpServer(File fileToServe) throws IOException {

        int port = getRandomFreeTcpPort();
        String responseBody = Files.readString(fileToServe.toPath(), StandardCharsets.UTF_8);

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(responseBody);

        server.enqueue(response);
        server.start(port);

        log.info("Test httpserver at port {}", server.getPort());
    }

    public void stop() throws IOException {
        server.close();
    }

    public int getPort() {
        return server.getPort();
    }

    private static int getRandomFreeTcpPort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }
}
