package app.utils;

import app.model.LogEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for storing LogEvent objects into a ObservableList,
 * which is used for displaying them in UI
 */
public class LogEventRepository {

    /**
     * Map where opened files are keys and each file has corresponding ObservableList
     */
    private static final Map<String, ObservableList<LogEvent>> repository = new HashMap<>();

    /**
     * @param fileName is used as a key for storing in a map
     * @param event LogEvent to be stored
     */
    public static void addEvent(String fileName, LogEvent event) {
        repository.get(fileName).add(event);
    }

    /**
     * @param fileName name of the opened log file
     * @return ObservableList<LogEvent>
     */
    public static ObservableList<LogEvent> getLogEventList(String fileName) {
        return repository.get(fileName);
    }

    /**
     * Used for clearing the repository, when the log file is reset
     * @param fileName name of the opened log file
     */
    public static void clearRepository(String fileName) {
        repository.get(fileName).clear();
    }

    /**
     * Creates new repository if there is not one for the opened file
     * @param fileName name of the opened log file
     * @return true if new repository was created
     */
    public static boolean newRepository(String fileName) {
        if (repository.containsKey(fileName)) {
            return false;
        }
        ObservableList<LogEvent> logEventList = FXCollections.observableArrayList();
        repository.put(fileName, logEventList);
        return true;
    }

    /**
     * Removes the entry from repository map, if the file was closed
     * @param filename name of the closed file
     */
    public static void removeRepository(String filename) {
        repository.remove(filename);
    }
}