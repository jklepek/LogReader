/*
 * Created 2019. Open source.
 * @author jklepek
 */

package app.utils;

import app.utils.DirectoryWatchService;
import app.utils.DirectoryWatchServiceFactory;
import app.utils.PreferencesController;
import app.utils.notifications.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectoryWatchServiceTest {

    private static final File file = new File("src/test/java/app/utils/log4j.log");
    private static final Path newFile = Paths.get("src/test/java/app/utils/log4j1.log");

    @BeforeEach
    void setUp() {
        PreferencesController.getInstance().setAutoRefreshInterval(100);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (file.exists()) {
            Files.delete(newFile);
        }
    }

    @Test
    void watchServiceTest() throws IOException, InterruptedException {
        DirectoryWatchServiceFactory.getDirectoryWatchService(file).ifPresent(DirectoryWatchService::startWatching);
        Thread.sleep(300);
        Files.createFile(newFile);
        Thread.sleep(300);
        assertEquals(1, NotificationService.getNotifications().size());
    }
}