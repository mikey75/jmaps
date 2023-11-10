package net.wirelabs.jmaps;

import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okio.Buffer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created 5/27/23 by Michał Szwaczko (mikey@wirelabs.net)
 */
@Slf4j
public class TestHttpServer extends BaseHttpTestServer {

    public TestHttpServer(File xmlFile) throws IOException {

        Buffer buffer = new Buffer();
        buffer.write(Files.readAllBytes(xmlFile.toPath()));

        MockResponse response = new MockResponse().setResponseCode(200)
                //.addHeader("Content-Type", "application/xml; charset=utf-8")
                .setBody(buffer);

        server.enqueue(response);
        start();

    }




}
