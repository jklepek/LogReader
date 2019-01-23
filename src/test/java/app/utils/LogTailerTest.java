/*
 * Created 2019. Open source.
 * @author jklepek
 */

package app.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;


class LogTailerTest {

    private static final Path filePath = Paths.get("src/test/java/app/utils/log4j.log");

    @BeforeAll
    static void initTests() {
        PreferenceRepository.setAutoRefreshInterval(100);
        LogEventRepository.newRepository(filePath.toFile().getName());
    }

    @BeforeEach
    void setUp() throws IOException {
        String content = "2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\n" +
                "2018-12-10 12:08:19,958 ERROR [RestoreUiStateRunnable] com.thoughtworks.xstream.io.StreamException:  : Premature end of file.\n" +
                "java.util.concurrent.ExecutionException: com.thoughtworks.xstream.io.StreamException:  : Premature end of file.\n" +
                "\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)\n" +
                "\tat java.util.concurrent.FutureTask.get(FutureTask.java:206)\n" +
                "\tat com.quest.toad.ui.state.restore.RestoreUiStateRunnable.waitForTasks(RestoreUiStateRunnable.java:179)\n" +
                "\tat com.quest.toad.ui.state.restore.RestoreUiStateRunnable.watchDirectory(RestoreUiStateRunnable.java:150)\n" +
                "\tat org.eclipse.jface.operation.ModalContext$ModalContextThread.watchDirectory(ModalContext.java:119)\n";
        Files.write(filePath, content.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @AfterEach
    void tearDown() {
        LogEventRepository.clearRepository(filePath.toFile().getName());
    }

    @Test
    void startTailTest() throws IOException, InterruptedException {
        File file = new File(filePath.toString());
        Parser.getInstance().getLogEventsFromFile(file);
        assertEquals(2, LogEventRepository.getLogEventList(file.getName()).size());
        LogTailer logTailer = new LogTailer(file);
        logTailer.startTailing();
        Files.write(filePath, "2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.get(FutureTask.java:206)\n".getBytes(), StandardOpenOption.APPEND);
        Thread.sleep(300);
        assertEquals(3, LogEventRepository.getLogEventList(file.getName()).size());
        logTailer.stopTailing();
    }

    @Test
    void stopTailTest() throws IOException, InterruptedException {
        File file = new File(filePath.toString());
        Parser.getInstance().getLogEventsFromFile(file);
        assertEquals(2, LogEventRepository.getLogEventList(file.getName()).size());
        LogTailer logTailer = new LogTailer(file);
        logTailer.startTailing();
        Files.write(filePath, "2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.get(FutureTask.java:206)\n".getBytes(), StandardOpenOption.APPEND);
        Thread.sleep(300);
        assertEquals(3, LogEventRepository.getLogEventList(file.getName()).size());
        logTailer.stopTailing();
        Thread.sleep(300);
        Files.write(filePath, "2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.get(FutureTask.java:206)\n".getBytes(), StandardOpenOption.APPEND);
        Thread.sleep(300);
        assertEquals(3, LogEventRepository.getLogEventList(file.getName()).size());
    }

    @Test
    void resetLogTailTest() throws IOException, InterruptedException {
        File file = new File(filePath.toString());
        Parser.getInstance().getLogEventsFromFile(file);
        assertEquals(2, LogEventRepository.getLogEventList(file.getName()).size());
        LogTailer logTailer = new LogTailer(file);
        logTailer.startTailing();
        Thread.sleep(300);
        Files.write(filePath, "2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.get(FutureTask.java:206)\n".getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        Thread.sleep(300);
        assertEquals(1, LogEventRepository.getLogEventList(file.getName()).size());
    }
}