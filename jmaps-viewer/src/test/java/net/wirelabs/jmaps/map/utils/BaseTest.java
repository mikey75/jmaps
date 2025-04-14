package net.wirelabs.jmaps.map.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseTest {

    // Base test for all tests with some common test features
    private final LogVerifier logVerifier = new LogVerifier();

    @BeforeEach
    void beforeEach() {
       logVerifier.clearLogs();
    }

    protected void verifyNeverLogged(String message) {
        logVerifier.verifyNeverLogged(message);
    }

    protected void verifyLogged(String message) {
        logVerifier.verifyLogged(message);
    }

    protected void verifyLoggedTimes(int times, String message) {
        logVerifier.verifyLoggedTimes(times, message);
    }

    // must be static, since it may be used in @BeforeAll (which must be static)
    protected static void waitUntilAsserted(Duration duration, ThrowingRunnable assertion) {
        Awaitility.await().atMost(duration).untilAsserted(assertion);
    }
}
