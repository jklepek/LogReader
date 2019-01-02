package app;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class LogTailer implements Runnable {

    private static LogTailer instance = new LogTailer();
    private long lastPosition;
    private boolean tail = false;
    private long startFileLength;
    private File logFile;
    private long waitTime = 3000;
    private boolean exit = false;


    private LogTailer() {
    }

    public static LogTailer getInstance() {
        return instance;
    }

    public void startTailing(File file) {
        System.out.println("Started tailing.");
        this.tail = true;
        if (file != logFile || file != null) {
            this.logFile = file;
            this.startFileLength = file.length();
        }
    }

    public void exit(){
        this.exit = true;
    }

    public void setLastPosition(File file) {
        this.lastPosition = file.length();
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    public void stopTailing() {
        System.out.println("Stopped tailing.");
        this.tail = false;
    }

    private void tail() {
        StringBuffer buffer = new StringBuffer();
        try {
            while (tail) {
                Thread.sleep(waitTime);
                long currentFileLength = logFile.length();
                if (currentFileLength >= startFileLength) {
                    try (RandomAccessFile randomAccess = new RandomAccessFile(logFile, "r")) {
                        randomAccess.seek(lastPosition);
                        String currentLine;
                        while ((currentLine = randomAccess.readLine()) != null) {
                            buffer.append(currentLine + System.lineSeparator());
                        }
                        lastPosition = randomAccess.getFilePointer();
                        if (buffer.length() > 0) {
                            Parser.getInstance().parseBuffer(buffer);
                            System.out.println("Found new entries in the log file:\n" + buffer.toString());
                        }
                        buffer.setLength(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                        stopTailing();
                    }
                } else if (currentFileLength < startFileLength) {
                    System.out.println("Log file has been reset.");
                    lastPosition = 0;
                    startFileLength = currentFileLength;
                    LogEventRepository.clearRepository();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            stopTailing();
        }
    }

    @Override
    public void run() {
        while (!exit) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (tail) {
                tail();
            }
        }
    }
}
