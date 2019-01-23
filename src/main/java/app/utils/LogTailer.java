package app.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LogTailer implements Runnable {

    private long lastPosition;
    private long startFileLength;
    private final File logFile;
    private final long refreshInterval = PreferenceRepository.getAutoRefreshInterval();
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private Future taskHandle;

    public LogTailer(File logFile) {
        this.lastPosition = logFile.length();
        this.startFileLength = logFile.length();
        this.logFile = logFile;
    }

    public void startTailing() {
        stopTailing();
        taskHandle = service.submit(this);
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
                        Parser.getInstance().parseBuffer(buffer, logFile.getName());
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
                LogEventRepository.clearRepository(logFile.getName());
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
