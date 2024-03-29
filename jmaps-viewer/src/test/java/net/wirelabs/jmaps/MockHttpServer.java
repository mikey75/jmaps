package net.wirelabs.jmaps;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created 11/10/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class MockHttpServer {

    protected final MockWebServer server = new MockWebServer();

    private static final File VALID_CAPABILITIES_FILE = new File("src/test/resources/wmts/capabilities.xml");
    private static final File INVALID_CAPABILITIES_FILE = new File("src/test/resources/wmts/invalid.xml");

    public static final File TEST_TILE_FILE = new File("src/test/resources/tiles/tile.png");

    public MockHttpServer(int port) {
        try {
            server.setDispatcher(dispatcher);
            server.start(port);
        } catch (IOException ex) {
            log.error("Mock http server could not be started", ex);
            throw new RuntimeException("Mock http server could not be started");
        }
    }

    public MockHttpServer()  {
        this(55555);    // default port for tests
    }

    final Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest request)  {

            MockResponse response;
            switch (request.getPath()) {
                case "/valid1":
                case "/valid1?service=WMTS&request=GetCapabilities":
                case "/valid2?service=WMTS&request=GetCapabilities":
                    response = serve(VALID_CAPABILITIES_FILE);
                    return response;
                case "/invalid":
                    response = serve(INVALID_CAPABILITIES_FILE);
                    return response;
                case "/tile.png":
                    response = serve(TEST_TILE_FILE);
                    return response;
                case "/nonexisting":
                    return new MockResponse().setResponseCode(404);

            }

            return new MockResponse().setResponseCode(404);
        }
    };

    public void stop() throws IOException {
        server.close();
    }

    private MockResponse serve(File fileToServe) {
        try {
            Buffer buffer = new Buffer();
            buffer.write(Files.readAllBytes(fileToServe.toPath()));
            return new MockResponse().setResponseCode(200).setBody(buffer);
        } catch (IOException e) {
            return new MockResponse().setResponseCode(404);
        }
    }

}
