package app.controllers;

import app.core.LogEventRepository;
import app.core.LogTailer;
import app.core.Parser;
import app.model.*;
import app.notifications.NotificationService;
import app.preferences.PreferencesController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import org.controlsfx.control.CheckComboBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static app.model.PatternKeywords.*;

public class TabController {

    private final EventTreeItem rootNode = new EventTreeItem(new EventPropertyCounter(""));
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
    private ComboBox<String> treeViewCBox;
    @FXML
    private TreeTableView<EventPropertyCounter> treeView;
    private final TreeTableColumn<EventPropertyCounter, String> treeNameColumn = new TreeTableColumn<>();
    private final TreeTableColumn<EventPropertyCounter, String> treeCountColumn = new TreeTableColumn<>("Count");
    private ObservableList<LogEvent> events;
    private FilteredList<LogEvent> filteredList;
    private ObservableList<EventTreeItem> treeItems = FXCollections.observableArrayList();
    private String pattern = PreferencesController.getInstance().getLogPattern();
    private String delimiter = PreferencesController.getInstance().getDelimiter();
    private final Parser parser = new Parser(pattern, delimiter);

    public void initialize() {
        tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super LogEvent>) c -> {
            LogEvent logEvent = tableView.getSelectionModel().getSelectedItem();
            if (logEvent != null) {
                textArea.setText(logEvent.getProperty(STACKTRACE.name()));
            }
        });
        List<String> keywords = getKeywords();
        for (String keyword : keywords) {
            if (!keyword.equalsIgnoreCase(STACKTRACE.name())) {
                TableColumn<LogEvent, String> column = new TableColumn<>(keyword);
                column.setCellValueFactory(new LogEventPropertyFactory(keyword));
                tableView.getColumns().add(column);
            }
        }
        tableView.setRowFactory(tableView -> new LogEventTableRow());
        levelComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<? super String>) observable -> filterEventBySeverity());
        filterCombo.getItems().addAll(keywords);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> filterEvents(newValue));
        List<String> filteredKeywords = keywords
                .stream()
                .filter(s -> !(s.equals(TIMESTAMP.name()) || s.equals(STACKTRACE.name()) || s.equals(MESSAGE.name())))
                .collect(Collectors.toList());
        treeViewCBox.getItems().addAll(filteredKeywords);
        textArea.setEditable(false);
        createTreeTableView();
        parser.addListener(NotificationService.getInstance());
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
        if (!event.getProperty(STACKTRACE.name()).isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(event.getProperty(STACKTRACE.name()));
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
        filteredList.setPredicate(logEvent -> levels.contains((logEvent.getProperty(LEVEL.name()))));
    }

    /**
     * @return list of keywords used in current log4j pattern
     */
    private List<String> getKeywords() {
        return new ArrayList<>(parser.getKeywords());
    }

    /**
     * @param text filter events by selected property containing typed text
     */
    private void filterEvents(String text) {
        String property = filterCombo.getSelectionModel().getSelectedItem();
        if (!property.isEmpty() && text != null) {
            filteredList.setPredicate(event -> {
                String value = event.getProperty(property);
                return value.toUpperCase().contains(text.toUpperCase());
            });
        }
    }

    @FXML
    public void populateTreeTable() {
        treeItems.clear();
        rootNode.getChildren().clear();
        String property = treeViewCBox.getSelectionModel().getSelectedItem().toUpperCase();
        getTreeItems(property);
        rootNode.getChildren().addAll(treeItems);
    }

    /**
     * This method takes all the events and maps them to a list of EventTreeItems
     *
     * @param property selected LegEvent property
     */
    private void getTreeItems(String property) {
        events.stream()
                .collect(Collectors.groupingBy(event -> event.getProperty(property), Collectors.counting()))
                .entrySet()
                .stream()
                .map(entry -> new EventPropertyCounter(entry.getKey(), entry.getValue()))
                .forEach(treeItem -> treeItems.add(new EventTreeItem(treeItem)));
    }

    /**
     * Iterates through the list of EventTreeItems, updates existing ones and add new ones
     *
     * @param propertyValue value of the selected LogEvent property
     */
    private void updateTreeItem(String propertyValue) {
        if (treeItems.contains(new EventTreeItem(new EventPropertyCounter(propertyValue)))) {
            treeItems.stream()
                    .filter(i -> i.getValue().getName().equals(propertyValue))
                    .findFirst()
                    .ifPresent(item -> item.getValue().incrementCount());
        }
        treeItems.add(new EventTreeItem(new EventPropertyCounter(propertyValue)));
    }

    /**
     * Method for initializing data from outside
     * the tab controller
     *
     * @param logFile opened log file
     */
    public void initData(File logFile) {
        logTailer = new LogTailer(logFile, parser);
        logTailer.addListener(NotificationService.getInstance());
        events = LogEventRepository.getLogEventList(logFile.getAbsolutePath());
        filteredList = new FilteredList<>(events);
        tableView.setItems(filteredList);
        levelComboBox.getItems().addAll(getSeverityLevels());
        levelComboBox.getCheckModel().checkAll();
        initEventsChangeListeners();
        this.tab.setOnCloseRequest((event) -> logTailer.stopTailing());
    }

    /**
     * @return list of levels used in the opened file
     */
    private List<String> getSeverityLevels() {
        return events
                .stream()
                .map(event -> event.getProperty(String.valueOf(LEVEL)))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Adds new severity levels to the checkComboBox if there are any new and checks them
     * Updates EventTreeItem list when there are new LogEvents
     */
    private void initEventsChangeListeners() {
        events.addListener((ListChangeListener<? super LogEvent>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(i -> {
                        Platform.runLater(() -> {
                            if (!levelComboBox.getItems().contains(i.getProperty(String.valueOf(LEVEL)))) {
                                levelComboBox.getItems().add(i.getProperty(String.valueOf(LEVEL)));
                                levelComboBox.getCheckModel().checkAll();
                            }
                        });
                        String property = treeViewCBox.getSelectionModel().getSelectedItem();
                        if (property != null) {
                            updateTreeItem(i.getProperty(property));
                        }
                    });
                }
            }
        });
    }

    /**
     * Initializes the emitters TreeTableView
     */
    private void createTreeTableView() {
        treeNameColumn.setGraphic(new HBox(treeViewCBox));
        treeNameColumn.setMinWidth(110);
        treeView.getColumns().setAll(treeCountColumn, treeNameColumn);
        treeNameColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        treeCountColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("count"));
        treeView.setRoot(rootNode);
        rootNode.setExpanded(true);
        treeView.setShowRoot(false);
        treeView.setOnMouseClicked(event -> {
            TreeItem<EventPropertyCounter> selectedItem = treeView.getSelectionModel().getSelectedItem();
            String selectedProperty = treeViewCBox.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2 && selectedItem != null) {
                filteredList.setPredicate(event1 -> selectedItem.getValue().getName().equalsIgnoreCase(event1.getProperty(selectedProperty)));
            }
        });
    }
}
