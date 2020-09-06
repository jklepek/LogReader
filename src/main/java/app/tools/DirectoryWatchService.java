package app.tools;

import app.notifications.EventNotification;
import app.notifications.EventNotifier;
import app.notifications.NotificationListener;
import app.notifications.NotificationType;
import app.preferences.PreferencesController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Service that watches the parent directory of the opened log file
 * for new log files
 */
public class DirectoryWatchService implements Runnable, EventNotifier {

    public static final Logger LOG = LoggerFactory.getLogger(DirectoryWatchService.class);

    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final Path dirPath;
    private final long refreshInterval;
    private WatchService watchService;
    private Future taskHandle;
    private List<NotificationListener> listeners = new ArrayList<>();

    public DirectoryWatchService(File directory) {
        this.dirPath = directory.toPath();
        this.refreshInterval = PreferencesController.getInstance().getAutoRefreshInterval();
    }

    /**
     * Starts watching the directory
     */
    public void startWatching() {
        LOG.info("Started watching {} directory for new log files", dirPath.toString());
        taskHandle = service.submit(this);
    }

    /**
     * Stops watching the directory
     */
    public void stopWatching() {
        Optional.ofNullable(taskHandle).ifPresent(future -> future.cancel(true));
    }

    /**
     * Registers the directory to watch for new files
     */
    private void registerDir() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            dirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Periodically checks the folder for new files
     * and fires a notification when there is a new log file
     */
    private void watchDirectory() {
        registerDir();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(refreshInterval);
            } catch (InterruptedException e) {
                LOG.warn("File was closed. Stopped watching for new files.", e);
                Thread.currentThread().interrupt();
                break;
            }
            WatchKey key;
            key = watchService.poll();
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path newFileName = (Path) event.context();
                    String fileName = newFileName.toFile().getName();
                    if (fileName.endsWith(".log")) {
                        LOG.info("New logfile found: {}", newFileName);
                        listeners.forEach(listener ->
                                listener.fireNotification(
                                        new EventNotification("New log", "There is a new log file: " + fileName, NotificationType.INFORMATION)));
                    }
                }
                key.reset();
            }
        }
    }

    @Override
    public void run() {
        watchDirectory();
    }

    @Override
    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }
}
