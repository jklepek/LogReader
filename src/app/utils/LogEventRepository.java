package app.utils;

import app.model.LogEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LogEventRepository {

    private static final ObservableList<LogEvent> logEventList = FXCollections.observableArrayList();

    public static void addEvent(LogEvent event) {
        logEventList.add(event);
    }

    public static ObservableList<LogEvent> getLogEventList() {
        return logEventList;
    }

    public static void clearRepository() {
        logEventList.clear();
    }
}