package app.controllers;

import app.model.EmitterTreeItem;
import app.model.LogEvent;
import app.model.LogEventTableRow;
import app.utils.LogEventRepository;
import app.utils.LogTailer;
import app.utils.Parser;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.controlsfx.control.CheckComboBox;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class TabController {

    private final TreeItem<EmitterTreeItem> rootNode = new TreeItem<>();
    @FXML
    private Tab tab;
    private LogTailer logTailer;
    @FXML
    private TableView<LogEvent> tableView;
    @FXML
    private TextArea textArea;
    @FXML
    private ToggleButton autoRefreshButton;
    @FXML
    private CheckComboBox<String> levelComboBox;
    @FXML
    private ComboBox<String> filterCombo;
    @FXML
    private TextField filterField;
    @FXML
    private TreeTableView<EmitterTreeItem> treeView;
    private final TreeTableColumn<EmitterTreeItem, String> treeNameColumn = new TreeTableColumn<>("Emitter");
    private final TreeTableColumn<EmitterTreeItem, String> treeCountColumn = new TreeTableColumn<>("Count");
    private ObservableList<LogEvent> events;
    private FilteredList<LogEvent> filteredList;
    private ObservableList<TreeItem<EmitterTreeItem>> treeItems;
    private final Parser parser = new Parser();

    public void initialize() {
        tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super LogEvent>) c -> {
            LogEvent logEvent = tableView.getSelectionModel().getSelectedItem();
            if (logEvent != null) {
                textArea.setText(logEvent.getStacktrace());
            }
        });
        List<String> keywords = getKeywords();
        for (String keyword : keywords) {
            TableColumn<LogEvent, String> column = new TableColumn<>(keyword);
            column.setCellValueFactory(new PropertyValueFactory<>(keyword.toLowerCase()));
            tableView.getColumns().add(column);
        }
        tableView.setRowFactory(tableView -> new LogEventTableRow());
        levelComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<? super String>) observable -> filterEventBySeverity());
        filterCombo.getItems().addAll(keywords);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> filterEvents(newValue));
        textArea.setEditable(false);
        createTreeTableView();
    }

    @FXML
    public void resetFilters() {
        filteredList.setPredicate(event -> true);
        filterField.clear();
        levelComboBox.getCheckModel().checkAll();
    }

    @FXML
    public void copyStackTrace() {
        if (tableView.getItems().isEmpty()) {
            return;
        }
        LogEvent event = tableView.getSelectionModel().getSelectedItem();
        if (!event.getStacktrace().isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(event.getStacktrace());
            clipboard.setContent(content);
        }
    }

    @FXML
    public void autoRefresh() {
        if (autoRefreshButton.isSelected()) {
            System.out.println("Auto-refresh ON.");
            logTailer.startTailing();
        } else if (!autoRefreshButton.isSelected()) {
            System.out.println("Auto-refresh OFF.");
            logTailer.stopTailing();
        }
    }


    /**
     * Filters events by selected severity levels
     */
    private void filterEventBySeverity() {
        List<String> levels = levelComboBox.getCheckModel().getCheckedItems();
        filteredList.setPredicate(logEvent -> levels.contains((logEvent.getLevel())));
    }

    /**
     * @return list of keywords used in current log4j pattern
     */
    private List<String> getKeywords() {
        return parser.getKeywords()
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    /**
     * @param text filter events by selected property containing typed text
     */
    private void filterEvents(String text) {
        String property = filterCombo.getSelectionModel().getSelectedItem().toLowerCase();
        if (!property.isEmpty() && text != null) {
            filteredList.setPredicate(event -> {
                String value = ((StringProperty) event.getPropertyByName(property)).getValue();
                return value.toUpperCase().contains(text.toUpperCase());
            });
        }
    }

    /**
     * Method for initializing data from outside
     * the tab controller
     *
     * @param logFile opened log file
     */
    public void initData(File logFile) {
        logTailer = new LogTailer(logFile, parser);
        tab.setOnCloseRequest(event -> logTailer.stopTailing());
        events = LogEventRepository.getLogEventList(logFile.getName());
        filteredList = new FilteredList<>(events);
        tableView.setItems(filteredList);
        levelComboBox.getItems().addAll(getSeverityLevels());
        levelComboBox.getCheckModel().checkAll();
        treeItems = LogEventRepository.getTreeItems(logFile.getName());
        rootNode.getChildren().addAll(treeItems);
        initEventsChangeListeners();
        initTreeItemsChangeListener();
    }

    /**
     * @return list of levels used in opened file
     */
    private List<String> getSeverityLevels() {
        return events
                .stream()
                .map(LogEvent::getLevel)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Adds new severity levels to the checkComboBox if there are any new and checks them
     */
    private void initEventsChangeListeners() {
        events.addListener((ListChangeListener<? super LogEvent>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(i -> Platform.runLater(() -> {
                        if (!levelComboBox.getItems().contains(i.getLevel())) {
                            levelComboBox.getItems().add(i.getLevel());
                            levelComboBox.getCheckModel().checkAll();
                        }
                    }));
                }
            }
        });
    }

    /**
     * Initializes the emitters TreeTableView
     */
    private void createTreeTableView() {
        treeView.getColumns().setAll(treeCountColumn, treeNameColumn);
        treeNameColumn.setCellValueFactory(new TreeItemPropertyValueFactory("name"));
        treeCountColumn.setCellValueFactory(new TreeItemPropertyValueFactory("count"));
        treeView.setRoot(rootNode);
        rootNode.setExpanded(true);
        treeView.setShowRoot(false);
        treeView.setOnMouseClicked(event -> {
            TreeItem<EmitterTreeItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2 && selectedItem != null) {
                filteredList.setPredicate(event1 -> selectedItem.getValue().getName().equalsIgnoreCase(event1.getEmitter()));
            }
        });
    }

    /**
     * Initializes change listener on TreeItem observable list, to handle removing or adding items to TreeTable
     */
    private void initTreeItemsChangeListener() {
        treeItems.addListener((ListChangeListener<? super TreeItem<EmitterTreeItem>>) c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    rootNode.getChildren().removeAll(c.getRemoved());
                } else if (c.wasAdded()) {
                    c.getAddedSubList().forEach(o -> rootNode.getChildren().add(o));
                }
            }
        });
    }
}
