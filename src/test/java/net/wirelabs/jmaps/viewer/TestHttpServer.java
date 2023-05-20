package net.wirelabs.jmaps.viewer;

import fi.iki.elonen.NanoHTTPD;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Created 5/27/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class TestHttpServer extends NanoHTTPD {

    private final File fileToServe;

    public TestHttpServer(int port,File fileToServe) throws IOException {
        super("localhost", port);
        start();
        this.fileToServe = fileToServe;
        log.info("Test httpserver at port {}", getListeningPort());
    }


    @Override
    public Response serve(IHTTPSession session) {

        try {
            String response = Files.readString(fileToServe.toPath(), StandardCharsets.UTF_8);
            return newFixedLengthResponse(response);
        } catch (IOException e) {
            throw new IllegalArgumentException("File could not be served", e);
        }
    }

    public static int getRandomFreeTcpPort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }
}
