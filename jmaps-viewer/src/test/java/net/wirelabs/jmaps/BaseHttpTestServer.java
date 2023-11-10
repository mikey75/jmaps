package net.wirelabs.jmaps;

import okhttp3.mockwebserver.MockWebServer;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created 11/10/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
public class BaseHttpTestServer {

    protected final MockWebServer server = new MockWebServer();

    protected static int getRandomFreeTcpPort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    public void stop() throws IOException {
        server.close();
    }

    public int getPort() {
        return server.getPort();
    }

    public void start() throws IOException {
        server.start(getRandomFreeTcpPort());
    }
}
