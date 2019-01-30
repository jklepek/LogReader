package app.utils;

import app.model.LogEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class LogEventRepository {

    private static final Map<String, ObservableList<LogEvent>> repository = new HashMap<>();

    public static void addEvent(String fileName, LogEvent event) {
        repository.get(fileName).add(event);
    }

    public static ObservableList<LogEvent> getLogEventList(String fileName) {
        return repository.get(fileName);
    }

    public static void clearRepository(String fileName) {
        repository.get(fileName).clear();
    }

    public static boolean newRepository(String fileName) {
        if (repository.containsKey(fileName)) {
            return false;
        }
        ObservableList<LogEvent> logEventList = FXCollections.observableArrayList();
        repository.put(fileName, logEventList);
        return true;
    }

    public static void removeRepository(String filename) {
        repository.remove(filename);
    }
}