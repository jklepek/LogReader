package app.controllers;

import app.model.LogEvent;
import app.model.LogLevel;
import app.utils.LogEventRepository;
import app.utils.LogTailer;
import app.utils.Parser;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.controlsfx.control.CheckComboBox;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class TabController {

    private File file;
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

    public void initialize() {
        tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<? super LogEvent>) c -> {
            LogEvent logEvent = tableView.getSelectionModel().getSelectedItem();
            if (logEvent != null) {
                textArea.setText(logEvent.getStackTrace());
            }
        });
        List<String> keywords = Parser.getInstance().getKeywords();
        for (String keyword : keywords) {
            TableColumn<LogEvent, String> column = new TableColumn<>(keyword.toUpperCase());
            column.setCellValueFactory(new PropertyValueFactory<>(keyword));
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
                                default:
                                    setStyle("-fx-backgound-color: white;");
                                    break;
                            }
                        }
                    }
                }

        );
        for (Field level : LogLevel.class.getDeclaredFields()) {
            try {
                levelComboBox.getItems().add(level.get(LogLevel.class).toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        levelComboBox.getCheckModel().checkAll();
        levelComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<? super String>) observable -> filterLogEvents());
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

    private void filterLogEvents() {
        List<String> levels = levelComboBox.getCheckModel().getCheckedItems();
        FilteredList<LogEvent> filteredList = new FilteredList<>(LogEventRepository.getLogEventList(file.getName()));
        filteredList.setPredicate(logEvent -> levels.contains(logEvent.getLevel()));
        tableView.setItems(filteredList);
        tableView.refresh();
    }

    public void initData(File logFile) {
        this.file = logFile;
        logTailer = new LogTailer(file);
        tab.setOnCloseRequest(event -> logTailer.stopTailing());
    }
}
