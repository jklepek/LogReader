package app.core;

import app.notifications.EventNotification;
import app.notifications.EventNotifier;
import app.notifications.NotificationListener;
import app.notifications.NotificationType;
import app.preferences.PreferencesController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class for reading new lines in the opened log file
 * and adding them to UI
 */
public class LogTailer implements Runnable, EventNotifier {

    public static final Logger LOG = LoggerFactory.getLogger(LogTailer.class);

    private final File logFile;
    private final long refreshInterval = PreferencesController.getInstance().getAutoRefreshInterval();
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private long lastPosition;
    private long startFileLength;
    private ScheduledFuture taskHandle;
    private final Parser parser;
    private final List<NotificationListener> listeners = new ArrayList<>();

    public LogTailer(File logFile, Parser parser) {
        this.startFileLength = logFile.length();
        this.lastPosition = startFileLength;
        this.logFile = logFile;
        this.parser = parser;
    }


    /**
     * Starts checking the file for new content
     */
    public void startTailing() {
        stopTailing();
        LOG.info("Started auto-refresh for log: {}", logFile);
        taskHandle = service.scheduleAtFixedRate(this, refreshInterval, refreshInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops checking the file for new content
     */
    public void stopTailing() {
        LOG.info("Stopped auto-refresh for log: {}", logFile);
        Optional.ofNullable(taskHandle).ifPresent(future -> future.cancel(true));
    }

    /**
     * Periodically checks if the file has new content
     * and if it does, it parses the new lines
     * and adds them to corresponding repository
     */
    private void tail() {
        StringBuilder buffer = new StringBuilder();
        long currentFileLength = logFile.length();
        if (currentFileLength >= startFileLength) {
            try (RandomAccessFile randomAccess = new RandomAccessFile(logFile, "r")) {
                randomAccess.seek(lastPosition);
                String currentLine;
                while ((currentLine = randomAccess.readLine()) != null) {
                    buffer.append(currentLine).append(System.lineSeparator());
                }
                lastPosition = randomAccess.getFilePointer();
                if (buffer.length() > 0) {
                    parser.parseBuffer(buffer, logFile.getAbsolutePath());
                }
                buffer.setLength(0);
            } catch (IOException e) {
                LOG.error("Could not read file {}", logFile, e);
            }
        } else {
            listeners.forEach(listener ->
                    listener.fireNotification(
                            new EventNotification("Log file reset", "Log has been reset", NotificationType.INFORMATION)));
            lastPosition = 0;
            startFileLength = currentFileLength;
            LogEventRepository.clearRepository(logFile.getAbsolutePath());
        }
    }

    @Override
    public void run() {
        tail();
    }

    @Override
    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }
}
