package test;

import app.utils.LogEventRepository;
import app.utils.LogTailer;
import app.utils.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;


class LogTailerTest {

    private final String filePath = System.getProperty("user.dir") + "\\src\\test\\log4j.log";

    @BeforeEach
    void setUp() throws IOException {
        String content = "2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\n" +
                "2018-12-10 12:08:19,958 ERROR [RestoreUiStateRunnable] com.thoughtworks.xstream.io.StreamException:  : Premature end of file.\n" +
                "java.util.concurrent.ExecutionException: com.thoughtworks.xstream.io.StreamException:  : Premature end of file.\n" +
                "\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)\n" +
                "\tat java.util.concurrent.FutureTask.get(FutureTask.java:206)\n" +
                "\tat com.quest.toad.ui.state.restore.RestoreUiStateRunnable.waitForTasks(RestoreUiStateRunnable.java:179)\n" +
                "\tat com.quest.toad.ui.state.restore.RestoreUiStateRunnable.run(RestoreUiStateRunnable.java:150)\n" +
                "\tat org.eclipse.jface.operation.ModalContext$ModalContextThread.run(ModalContext.java:119)\n";
        Files.write(Paths.get(filePath), content.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        LogEventRepository.clearRepository();
    }

    @Test
    void startTailTest() throws IOException, InterruptedException {
        LogTailer.getInstance().setRefreshInterval(500);
        File file = new File(filePath);
        LogTailer.getInstance().setLastPosition(file);
        Parser.getInstance().getLogEventsFromFile(file);
        assertEquals(2, LogEventRepository.getLogEventList().size());
        LogTailer.getInstance().startTailing(file);
        Files.write(Paths.get(filePath), "2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.get(FutureTask.java:206)\n".getBytes(), StandardOpenOption.APPEND);
        Thread.sleep(1000);
        assertEquals(3, LogEventRepository.getLogEventList().size());
        LogTailer.getInstance().stopTailing();
    }

    @Test
    void stopTailTest() throws IOException, InterruptedException {
        LogTailer.getInstance().setRefreshInterval(100);
        File file = new File(filePath);
        LogTailer.getInstance().setLastPosition(file);
        Parser.getInstance().getLogEventsFromFile(file);
        assertEquals(2, LogEventRepository.getLogEventList().size());
        LogTailer.getInstance().startTailing(file);
        Files.write(Paths.get(filePath), "2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.get(FutureTask.java:206)\n".getBytes(), StandardOpenOption.APPEND);
        Thread.sleep(1000);
        assertEquals(3, LogEventRepository.getLogEventList().size());
        LogTailer.getInstance().stopTailing();
        Thread.sleep(1000);
        Files.write(Paths.get(filePath), "2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.get(FutureTask.java:206)\n".getBytes(), StandardOpenOption.APPEND);
        Thread.sleep(1000);
        assertEquals(3, LogEventRepository.getLogEventList().size());
    }

    @Test
    void resetLogTailTest() throws IOException, InterruptedException {
        LogTailer.getInstance().setRefreshInterval(500);
        File file = new File(filePath);
        LogTailer.getInstance().setLastPosition(file);
        Parser.getInstance().getLogEventsFromFile(file);
        assertEquals(2, LogEventRepository.getLogEventList().size());
        LogTailer.getInstance().startTailing(file);
        Thread.sleep(1000);
        Files.write(Paths.get(filePath), "2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.get(FutureTask.java:206)\n".getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        Thread.sleep(1000);
        assertEquals(1, LogEventRepository.getLogEventList().size());
    }
}