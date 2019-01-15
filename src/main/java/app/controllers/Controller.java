package app.controllers;

import app.model.LogEvent;
import app.utils.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Controller {

    private final PreferencesController preferences = PreferencesController.getInstance();
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button settingsButton;
    @FXML
    private TabPane tabPane;
    private FileChooser fileChooser;

    public void initialize() {
        fileChooser = new FileChooser();
        Image settingsImage = new Image(getClass().getResourceAsStream("/icons/settings.png"), 17, 17, true, true);
        settingsButton.setGraphic(new ImageView(settingsImage));
    }

    @FXML
    public void openFile() {
        configureFileChooser(fileChooser);
        Window window = borderPane.getScene().getWindow();
        File currentLogFile = fileChooser.showOpenDialog(window);
        if (currentLogFile != null) {
            createTab(currentLogFile);
        }
    }

    private void configureFileChooser(FileChooser chooser) {
        chooser.setTitle("Select log file");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Log", "*.log"));
        chooser.setInitialDirectory(new File(preferences.getInitialDir()));
    }


    @FXML
    public void openSettings() {
        Dialog<ButtonType> settingsDialog = new Dialog<>();
        settingsDialog.initOwner(borderPane.getScene().getWindow());
        settingsDialog.setTitle("Settings");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/settingsDialog.fxml"));
        try {
            settingsDialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        SettingsDialogController settingsDialogController = fxmlLoader.getController();
        settingsDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        settingsDialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(settingsDialogController.validContentProperty());
        Optional<ButtonType> result = settingsDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            settingsDialogController.savePreferences();
        }
    }

    private void createTab(File file) {
        boolean created = LogEventRepository.newRepository(file.getName());
        if (created) {
            Parser.getInstance().getLogEventsFromFile(file);
        }
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("/fxml/tableTab.fxml"));
        Tab tab = new Tab();
        try {
            tab = fxmlLoader.load();
        } catch (IOException e) {
            System.out.println("Couldn't load fxml file.");
            e.printStackTrace();
        }
        TabController tabController = fxmlLoader.getController();
        tabController.initData(file);
        tab.setText(file.getName());
        tabPane.getTabs().add(tab);
        setTabContent(tab, file);
    }

    private void setTabContent(Tab tab, File file) {
        TableView<LogEvent> tableView = (TableView<LogEvent>) tab.getContent().lookup("#tableView");
        tableView.setItems(LogEventRepository.getLogEventList(file.getName()));
        if (!PreferencesController.getInstance().getWatchForDirChanges()) {
            return;
        }
        Optional<DirectoryWatchService> dirListener = DirectoryWatchServiceFactory.getDirectoryWatchService(file);
        dirListener.ifPresent(DirectoryWatchService::startWatching);
        tab.setOnCloseRequest(event -> {
            dirListener.ifPresent(DirectoryWatchService::stopWatching);
            DirectoryWatchServiceFactory.removeWatchedDir(file);
        });
    }
}