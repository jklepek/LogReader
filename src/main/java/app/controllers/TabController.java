package app.controllers;

import app.model.LogEvent;
import app.model.LogLevel;
import app.utils.LogEventRepository;
import app.utils.LogTailer;
import app.utils.Parser;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.controlsfx.control.CheckComboBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TabController {

    private final TreeItem<String> rootNode = new TreeItem<>("Emitters:");
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
    private TreeView<String> treeView;
    private ObservableList<LogEvent> events;
    private FilteredList<LogEvent> filteredList;

    public void initialize() {
        tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super LogEvent>) c -> {
            LogEvent logEvent = tableView.getSelectionModel().getSelectedItem();
            if (logEvent != null) {
                textArea.setText(logEvent.getStacktrace());
            }
        });
        List<String> keywords = Parser.getInstance().getKeywords()
                .stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        for (String keyword : keywords) {
            TableColumn<LogEvent, String> column = new TableColumn<>(keyword);
            column.setCellValueFactory(new PropertyValueFactory<>(keyword.toLowerCase()));
            tableView.getColumns().add(column);
        }
        tableView.setRowFactory(tableView -> new TableRow<>() {
                    @Override
                    protected void updateItem(LogEvent item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            switch (item.getLevel()) {
                                case LogLevel.ERROR:
                                    setStyle("-fx-background-color: indianred;");
                                    break;
                                case LogLevel.INFO:
                                    setStyle("-fx-background-color: cornflowerblue;");
                                    break;
                                case LogLevel.WARN:
                                    setStyle("-fx-background-color: orange;");
                                    break;
                                case LogLevel.DEBUG:
                                    setStyle("-fx-background-color: lightblue;");
                                    break;
                                case LogLevel.TRACE:
                                    setStyle("-fx-background-color: ivory");
                                    break;
                                case LogLevel.FATAL:
                                    setStyle("-fx-background-color: firebrick");
                                    break;
                                default:
                                    setStyle("-fx-backgound-color: white;");
                                    break;
                            }
                        }
                    }
                }

        );
        levelComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<? super String>) observable -> filterEventBySeverity());
        filterCombo.getItems().addAll(keywords);
        filterField.textProperty().addListener((observable, oldValue, newValue) -> filterEvents(newValue));
    }

    @FXML
    public void copyStackTrace() {
        LogEvent event;
        if (!tableView.getItems().isEmpty()) {
            event = tableView.getSelectionModel().getSelectedItem();
        } else {
            return;
        }
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
            System.out.println("Started tailing.");
            logTailer.startTailing();
        } else if (!autoRefreshButton.isSelected()) {
            System.out.println("Stopped tailing.");
            logTailer.stopTailing();
        }
    }

    private void filterEventBySeverity() {
        List<String> levels = levelComboBox.getCheckModel().getCheckedItems();
        filteredList.setPredicate(logEvent -> levels.contains(logEvent.getLevel()));
        tableView.refresh();
    }

    private void filterEvents(String text) {
        String property = filterCombo.getSelectionModel().getSelectedItem().toLowerCase();
        if (!property.isEmpty() && text != null) {
            filteredList.setPredicate(event -> {
                String value = "";
                try {
                    value = ((StringProperty) event.getPropertyByName(property)).getValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return value.toUpperCase().contains(text.toUpperCase());
            });
        }
        tableView.refresh();
    }

    /**
     * Method for initializing data from outside
     * the tab controller
     *
     * @param logFile opened log file
     */
    public void initData(File logFile) {
        logTailer = new LogTailer(logFile);
        tab.setOnCloseRequest(event -> logTailer.stopTailing());
        events = LogEventRepository.getLogEventList(logFile.getName());
        filteredList = new FilteredList<>(events);
        tableView.setItems(filteredList);
        levelComboBox.getItems().addAll(getSeverityLevels());
        levelComboBox.getCheckModel().checkAll();
        rootNode.getChildren().addAll(getTreeItems());
        treeView.setRoot(rootNode);
        rootNode.setExpanded(true);
        initEventsChangeListeners();
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
     * Stores distinct values to a map where keys are distinct emitters
     * and value is the emitter's occurrence count
     */
    private Map<Object, Long> getDistinctEmitters() {
        return events
                .stream()
                .collect(Collectors.groupingBy(LogEvent::getEmitter, Collectors.counting()));
    }

    /**
     * ObservableList of tree items to populate tree view
     */
    private List<TreeItem<String>> getTreeItems() {
        return getDistinctEmitters()
                .entrySet()
                .stream()
                .map(e -> String.format("%s : [%s]", e.getKey(), e.getValue()))
                .map(TreeItem::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Refreshes tree view when new events are added
     */
    private void initEventsChangeListeners() {
        events.addListener((ListChangeListener<? super LogEvent>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    rootNode.getChildren().clear();
                    rootNode.getChildren().addAll(getTreeItems());
                    tableView.refresh();
                }
            }
        });
    }
}
