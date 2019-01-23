package app.utils;

import app.utils.notifications.EventNotification;
import app.utils.notifications.NotificationService;
import app.utils.notifications.NotificationType;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DirectoryWatchService implements Runnable {

    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final Path dirPath;
    private final long refreshInterval;
    private WatchService watchService;
    private Future taskHandle;

    public DirectoryWatchService(File directory) {
        this.dirPath = directory.toPath();
        this.refreshInterval = PreferenceRepository.getAutoRefreshInterval();
    }

    public void startWatching() {
        System.out.println("Watching " + dirPath.toString());
        taskHandle = service.submit(this);
    }

    public void stopWatching() {
        Optional.ofNullable(taskHandle).ifPresent(future -> future.cancel(true));
    }

    private void registerDir() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            dirPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void watchDirectory() {
        registerDir();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(refreshInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
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
                        System.out.println("There is a new log file: " + newFileName);
                        NotificationService
                                .addNotification(
                                        new EventNotification("New log",
                                                "There is a new log file: " + fileName,
                                                NotificationType.INFORMATION));
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

}
