package app.controllers;

import app.core.LogEventRepository;
import app.core.LogTailer;
import app.core.Parser;
import app.model.EventPropertyCounter;
import app.model.EventTreeItem;
import app.model.LogEvent;
import app.model.ui.LogEventPropertyFactory;
import app.model.ui.LogEventTableRow;
import app.notifications.NotificationService;
import app.preferences.PreferencesController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.controlsfx.control.CheckComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static app.model.PatternKeywords.*;

public class TabController {

    public static final Logger LOG = LoggerFactory.getLogger(TabController.class);

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
    @FXML
    private Label rowsCount;
    @FXML
    private ProgressBar progressBar;
    private final TreeTableColumn<EventPropertyCounter, String> treeNameColumn = new TreeTableColumn<>();
    private final TreeTableColumn<EventPropertyCounter, String> treeCountColumn = new TreeTableColumn<>("Count");
    private ObservableList<LogEvent> events;
    private FilteredList<LogEvent> filteredList;
    private ObservableList<String> severityLevels = null;
    private final ObservableList<EventTreeItem> treeItems = FXCollections.observableArrayList();
    private final String pattern = PreferencesController.getInstance().getCurrentLogPattern();
    private final Parser parser = new Parser(pattern);

    public void initialize() {
        progressBar.setVisible(false);
        tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super LogEvent>) c -> {
            LogEvent logEvent = tableView.getSelectionModel().getSelectedItem();
            if (logEvent != null) {
                textArea.setText(logEvent.getProperty(MESSAGE.name()));
            }
        });
        List<String> keywords = getKeywords();
        for (String keyword : keywords) {
            TableColumn<LogEvent, String> column = new TableColumn<>(keyword);
            column.setCellValueFactory(new LogEventPropertyFactory(keyword));
            tableView.getColumns().add(column);
        }
        tableView.setRowFactory(tView -> new LogEventTableRow());
        severityLevels = levelComboBox.getItems();
        levelComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<? super String>) observable -> filterEventBySeverity());
        filterCombo.getItems().addAll(keywords);
        PauseTransition transition = new PauseTransition(Duration.millis(1000));
        filterField.textProperty().addListener((observable, oldValue, newValue) -> {
            progressBar.setVisible(true);
            transition.setOnFinished(event -> {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() {
                        filterEvents(newValue);
                        return null;
                    }
                };
                progressBar.progressProperty().bind(task.progressProperty());
                task.setOnSucceeded(event1 -> progressBar.setVisible(false));
                task.setOnFailed(event1 -> progressBar.setVisible(false));
                Platform.runLater(task);
            });
            transition.playFromStart();
        });
        List<String> filteredKeywords = keywords
                .stream()
                .filter(s -> !(s.equals(TIMESTAMP.name()) || s.equals(MESSAGE.name())))
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
        if (!event.getProperty(MESSAGE.name()).isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(event.getProperty(MESSAGE.name()));
            clipboard.setContent(content);
        }
    }

    @FXML
    public void autoRefresh() {
        if (autoRefreshButton.isSelected()) {
            logTailer.startTailing();
        } else if (!autoRefreshButton.isSelected()) {
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
        if (property != null && text != null) {
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
        EventTreeItem eventTreeItem = new EventTreeItem(new EventPropertyCounter(propertyValue));
        if (treeItems.contains(eventTreeItem)) {
            treeItems.stream()
                    .filter(i -> i.getValue().getName().equals(propertyValue))
                    .findFirst()
                    .ifPresent(item -> item.getValue().incrementCount());
        } else {
            treeItems.add(eventTreeItem);
        }
    }

    /**
     * Method for initializing data from outside
     * the tab controller
     *
     * @param logFile opened log file
     */
    public void initData(File logFile) {
        LOG.info("New tab for {}", logFile);
        logTailer = new LogTailer(logFile, parser);
        logTailer.addListener(NotificationService.getInstance());
        events = LogEventRepository.getLogEventList(logFile.getAbsolutePath());
        filteredList = new FilteredList<>(events);
        tableView.setItems(filteredList);
        severityLevels.setAll(getSeverityLevels());
        NumberFormat format = getFormat();
        tableView.getItems().addListener((InvalidationListener) c ->
                rowsCount.setText(format.format(tableView.getItems().size())));
        levelComboBox.getCheckModel().checkAll();
        initEventsChangeListeners();
        tab.setOnClosed(event -> logTailer.stopTailing());
    }

    private NumberFormat getFormat() {
        return NumberFormat.getInstance();
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

    private void updateLevelsComboBox(LogEvent event) {
        String level = event.getProperty(LEVEL.name());
        if (!severityLevels.contains(level)) {
            severityLevels.add(level);
            Platform.runLater(() -> levelComboBox.getCheckModel().checkAll());
        }
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
                        updateLevelsComboBox(i);
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
