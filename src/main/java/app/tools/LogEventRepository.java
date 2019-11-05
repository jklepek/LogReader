package app.tools;

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
     * @param absoluteFilePath is used as a key for storing in a map
     * @param event    LogEvent to be stored
     */
    public static void addEvent(String absoluteFilePath, LogEvent event) {
        REPOSITORY.get(absoluteFilePath).add(event);
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
        ObservableList<TreeItem<EmitterTreeItem>> emitterTreeItems = FXCollections.observableArrayList();
        TREE_ITEMS.put(absoluteFilePath, emitterTreeItems);
    }

    /**
     * Removes the entry from REPOSITORY map, if the file was closed
     *
     * @param absoluteFilePath name of the closed file
     */
    public static void removeRepository(String absoluteFilePath) {
        REPOSITORY.remove(absoluteFilePath);
        TREE_ITEMS.remove(absoluteFilePath);
    }


    public static void addEmitterTreeItem(String absoluteFilePath, EmitterTreeItem item) {
        if (TREE_ITEMS.get(absoluteFilePath).stream().anyMatch(item1 -> item1.getValue().getName().equalsIgnoreCase(item.getName()))) {
            TREE_ITEMS.get(absoluteFilePath)
                    .stream()
                    .filter(item1 -> item1.getValue().getName().equalsIgnoreCase(item.getName()))
                    .findFirst()
                    .ifPresent(i -> i.getValue().incrementCount());
        } else {
            TREE_ITEMS.get(absoluteFilePath).add(new TreeItem<>(item));
        }
    }

    public static ObservableList<TreeItem<EmitterTreeItem>> getTreeItems(String fileName) {
        return TREE_ITEMS.get(fileName);
    }

}