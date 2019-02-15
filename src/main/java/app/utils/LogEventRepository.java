package app.utils;

import app.model.EmitterTreeItem;
import app.model.LogEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

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

    private static final Map<String, ObservableList<TreeItem<EmitterTreeItem>>> TREE_ITEMS = new HashMap<>();

    /**
     * @param fileName is used as a key for storing in a map
     * @param event    LogEvent to be stored
     */
    public static void addEvent(String fileName, LogEvent event) {
        REPOSITORY.get(fileName).add(event);
    }

    /**
     * @param fileName name of the opened log file
     * @return ObservableList<LogEvent>
     */
    public static ObservableList<LogEvent> getLogEventList(String fileName) {
        return REPOSITORY.get(fileName);
    }

    /**
     * Used for clearing the REPOSITORY, when the log file is reset
     *
     * @param fileName name of the opened log file
     */
    public static void clearRepository(String fileName) {
        REPOSITORY.get(fileName).clear();
        TREE_ITEMS.get(fileName).clear();
    }

    /**
     * Creates new REPOSITORY if there is not one for the opened file
     *
     * @param fileName name of the opened log file
     * @return true if new REPOSITORY was created
     */
    public static boolean newRepository(String fileName) {
        if (REPOSITORY.containsKey(fileName)) {
            return false;
        }
        ObservableList<LogEvent> logEventList = FXCollections.observableArrayList();
        REPOSITORY.put(fileName, logEventList);
        ObservableList<TreeItem<EmitterTreeItem>> emitterTreeItems = FXCollections.observableArrayList();
        TREE_ITEMS.put(fileName, emitterTreeItems);
        return true;
    }

    /**
     * Removes the entry from REPOSITORY map, if the file was closed
     *
     * @param fileName name of the closed file
     */
    public static void removeRepository(String fileName) {
        REPOSITORY.remove(fileName);
        TREE_ITEMS.remove(fileName);
    }


    public static void addEmitterTreeItem(String fileName, EmitterTreeItem item) {
        if (TREE_ITEMS.get(fileName).stream().anyMatch(item1 -> item1.getValue().getName().equalsIgnoreCase(item.getName()))) {
            TREE_ITEMS.get(fileName)
                    .stream()
                    .filter(item1 -> item1.getValue().getName().equalsIgnoreCase(item.getName()))
                    .findFirst()
                    .ifPresent(i -> i.getValue().incrementCount());
        } else {
            TREE_ITEMS.get(fileName).add(new TreeItem<>(item));
        }
    }

    public static ObservableList<TreeItem<EmitterTreeItem>> getTreeItems(String fileName) {
        return TREE_ITEMS.get(fileName);
    }

}