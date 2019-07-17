/*
 * Created 2019. Open source.
 * @author jklepek
 */

package app.tools;

import app.tools.notifications.NotificationService;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

class DirectoryWatchServiceTest {

    private static final File file = new File("src/test/java/app/tools/log4j.log");
    private static final Path newFile = Paths.get("src/test/java/app/tools/log4j1.log");

    @BeforeEach
    void setUp() {
        PreferencesRepository.loadPreferences();
        PreferencesRepository.setAutoRefreshInterval(100);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (newFile.toFile().exists()) {
            Files.delete(newFile);
        }
    }

    @Test
    void watchServiceTest() throws IOException {
        DirectoryWatchServiceFactory.getDirectoryWatchService(file).ifPresent(DirectoryWatchService::startWatching);
        Awaitility.await().timeout(Duration.ONE_HUNDRED_MILLISECONDS);
        Files.createFile(newFile);
        Awaitility.await().atMost(4000, TimeUnit.MILLISECONDS).until(isNotificationAdded());
    }

    private Callable<Boolean> isNotificationAdded() {
        return () -> NotificationService.getNotifications().size() == 1;
    }
}