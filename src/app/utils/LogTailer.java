package app.utils;

import app.LogEventRepository;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LogTailer implements Runnable {

    private static final LogTailer instance = new LogTailer();
    private long lastPosition;
    private long startFileLength;
    private File logFile;
    private long refreshInterval = PreferencesController.getInstance().getAutoRefreshInterval();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private Future taskHandle;


    private LogTailer() {
    }

    public static LogTailer getInstance() {
        return instance;
    }

    public void startTailing(File file) {
        stopTailing();
        taskHandle = service.submit(this);
        if (file != logFile || file != null) {
            this.logFile = file;
            this.startFileLength = file.length();
        }
    }

    public void setLastPosition(File file) {
        this.lastPosition = file.length();
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public void stopTailing() {
        Optional.ofNullable(taskHandle).ifPresent(future -> future.cancel(true));
    }

    private void tail() {
        StringBuilder buffer = new StringBuilder();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(refreshInterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                break;
            }
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
                        Parser.getInstance().parseBuffer(buffer);
                        System.out.println("Found new entries in the log file:\n" + buffer.toString());
                    }
                    buffer.setLength(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Log file has been reset.");
                lastPosition = 0;
                startFileLength = currentFileLength;
                LogEventRepository.clearRepository();
            }
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            tail();
        }
    }
}
