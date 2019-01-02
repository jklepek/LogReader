package app;

import app.model.LogEvent;
import app.model.LogLevel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Controller {

    @FXML
    private BorderPane borderPane;

    @FXML
    private TableView<LogEvent> tableView;

    @FXML
    private TextArea textArea;

    @FXML
    private ComboBox levelComboBox;

    @FXML
    private ToggleButton toggleButton;

    @FXML
    private Button openFileButton;

    @FXML
    private Button settingsButton;


    private FileChooser fileChooser;
    private File currentLogFile;

    public void initialize() {
        Thread tailThread = new Thread(LogTailer.getInstance());
        tailThread.start();
        fileChooser = new FileChooser();
        configureFileChooser(fileChooser);
        textArea.setEditable(false);
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
        levelComboBox.getSelectionModel().selectedItemProperty().addListener(observable -> {
            if (currentLogFile != null) {
                filterLogEvents();
            }
        });
        toggleButton.setDisable(true);
    }

    @FXML
    public void openFile() {
        Window window = borderPane.getScene().getWindow();
        File logFile = fileChooser.showOpenDialog(window);
        if (logFile != currentLogFile) {
            LogEventRepository.clearRepository();
            tableView.refresh();
        }
        currentLogFile = logFile;
        Parser.getInstance().getLogEventsFromFile(logFile);
        tableView.setItems(LogEventRepository.getLogEventList());
        if (currentLogFile != null) {
            toggleButton.setDisable(false);
            LogTailer.getInstance().setLastPosition(currentLogFile);
        }
    }

    private void configureFileChooser(FileChooser chooser) {
        chooser.setTitle("Select log file");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Log", "*.log"));
        chooser.setInitialDirectory(new File("C:\\Program Files\\SmartBear\\SoapUI-5.4.0\\bin"));
    }

    @FXML
    public void tail() {
        if (toggleButton.isSelected() && currentLogFile != null) {
            LogTailer.getInstance().startTailing(currentLogFile);
        } else if (!toggleButton.isSelected() && currentLogFile != null) {
            LogTailer.getInstance().stopTailing();
        }
    }

    @FXML
    private void filterLogEvents() {
        String level = (String) levelComboBox.getSelectionModel().getSelectedItem();
        if (level.equals("ALL")) {
            tableView.setItems(LogEventRepository.getLogEventList());
            tableView.refresh();
        } else if (level.isEmpty()) {
            return;
        } else {
            FilteredList<LogEvent> filteredList = new FilteredList<>(LogEventRepository.getLogEventList());
            filteredList.setPredicate(event -> event.getLevel().equals(level));
            tableView.setItems(filteredList);
            tableView.refresh();
        }
    }

    @FXML
    public void openSettings() {
        Dialog<ButtonType> settingsDialog = new Dialog<>();
        settingsDialog.initOwner(borderPane.getScene().getWindow());
        settingsDialog.setTitle("Settings");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("settingsDialog.fxml"));
        try {
            settingsDialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        settingsDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        DialogController dialogController = fxmlLoader.getController();
        Optional<ButtonType> result = settingsDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dialogController.saveTimestampFormat();
        }
    }
}