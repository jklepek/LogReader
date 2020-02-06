package app.core;

import app.model.LogEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for storing LogEvent objects into an ObservableList,
 * which is used for displaying them in UI
 */
public class LogEventRepository {

    /**
     * Map where opened files are keys and each file has corresponding ObservableList
     */
    private static final Map<String, ObservableList<LogEvent>> REPOSITORY = new HashMap<>();

    /**
     * @param absoluteFilePath is used as a key for storing in a map
     * @param event            LogEvent to be stored
     */
    public static void addEvent(String absoluteFilePath, LogEvent event) {
        REPOSITORY.get(absoluteFilePath).add(event);
    }

    /**
     * @param absoluteFilePath path of the opened log file
     * @return ObservableList<LogEvent>
     */
    public static ObservableList<LogEvent> getLogEventList(String absoluteFilePath) {
        return REPOSITORY.get(absoluteFilePath);
    }

    /**
     * Used for clearing the REPOSITORY, when the log file is reset
     *
     * @param fileName name of the opened log file
     */
    public static void clearRepository(String fileName) {
        REPOSITORY.get(fileName).clear();
    }

    /**
     * Returns whether file is already opened or not
     *
     * @param absoluteFilePath absolute path of the opened log file
     * @return true if new REPOSITORY was created
     */
    public static boolean isOpened(String absoluteFilePath) {
        return REPOSITORY.containsKey(absoluteFilePath);
    }

    /**
     * Creates new repository for the opened file
     *
     * @param absoluteFilePath path of the opened file
     */
    public static void createNewRepository(String absoluteFilePath) {
        ObservableList<LogEvent> logEventList = FXCollections.observableArrayList();
        REPOSITORY.put(absoluteFilePath, logEventList);
    }

    /**
     * Removes the entry from REPOSITORY map, if the file was closed
     *
     * @param absoluteFilePath name of the closed file
     */
    public static void removeRepository(String absoluteFilePath) {
        REPOSITORY.remove(absoluteFilePath);
    }
}