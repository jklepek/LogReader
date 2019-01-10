package app.controllers;

import app.model.LogEvent;
import app.model.LogLevel;
import app.utils.LogEventRepository;
import app.utils.LogTailer;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.File;

public class TabController {

    private File file;
    private LogTailer logTailer;
    @FXML
    private TableView<LogEvent> tableView;
    @FXML
    private TextArea textArea;
    @FXML
    private ToggleButton autoRefreshButton;
    @FXML
    private ComboBox<String> levelComboBox;

    public void initialize() {
        tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super LogEvent>) c -> {
            LogEvent logEvent = tableView.getSelectionModel().getSelectedItem();
            if (logEvent != null) {
                textArea.setText(logEvent.getStackTrace());
            }
        });
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
                                    setStyle("-fx-background-color: lightblue;");
                                    break;
                                case LogLevel.WARNING:
                                    setStyle("-fx-background-color: orange;");
                                    break;
                                case LogLevel.DEBUG:
                                    setStyle("-fx-background-color: lightgolderrodyellow;");
                                    break;
                                default:
                                    setStyle("-fx-backgound-color: white;");
                                    break;
                            }
                        }
                    }
                }

        );
        levelComboBox.setItems(FXCollections.observableArrayList("ALL", LogLevel.DEBUG, LogLevel.ERROR, LogLevel.INFO, LogLevel.WARNING));
        levelComboBox.getSelectionModel().selectedItemProperty().addListener(observable -> filterLogEvents());
    }

    @FXML
    public void copyStackTrace() {
        LogEvent event;
        if (!tableView.getItems().isEmpty()) {
            event = tableView.getSelectionModel().getSelectedItem();
        } else {
            return;
        }
        if (!event.getStackTrace().isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(event.getStackTrace());
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

    @FXML
    private void filterLogEvents() {
        String level = levelComboBox.getSelectionModel().getSelectedItem();
        if (level.equals("ALL")) {
            tableView.setItems(LogEventRepository.getLogEventList(file.getName()));
            tableView.refresh();
        } else {
            FilteredList<LogEvent> filteredList = new FilteredList<>(LogEventRepository.getLogEventList(file.getName()));
            filteredList.setPredicate(event -> event.getLevel().equals(level));
            tableView.setItems(filteredList);
            tableView.refresh();
        }
    }

    public void initData(File logFile){
        this.file = logFile;
        logTailer = new LogTailer(file);
    }
}
