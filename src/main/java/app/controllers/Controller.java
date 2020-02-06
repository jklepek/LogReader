package app.controllers;

import app.core.LogEventRepository;
import app.core.Parser;
import app.notifications.NotificationService;
import app.preferences.PreferencesController;
import app.tools.DirectoryWatchService;
import app.tools.DirectoryWatchServiceFactory;
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
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            if (!LogEventRepository.isOpened(file.getAbsolutePath())) {
                LogEventRepository.createNewRepository(file.getAbsolutePath());
                new Parser(
                        PreferencesController.getInstance().getLogPattern(),
                        PreferencesController.getInstance().getDelimiter()
                ).getLogEventsFromFile(file);
            }
            createTab(file);
        }
    }

    private void configureFileChooser(FileChooser chooser) {
        chooser.setTitle("Select log file");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Log", "*.log"));
        File preferredFolder = new File(PreferencesController.getInstance().getInitialDir());
        if (!preferredFolder.exists() || !preferredFolder.isDirectory()) {
            chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        } else {
            chooser.setInitialDirectory(preferredFolder);
        }
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
        initDirectoryWatchService(tab, file);
        tabPane.getSelectionModel().select(tab);
    }

    private void initDirectoryWatchService(Tab tab, File file) {
        Optional<DirectoryWatchService> dirListener = DirectoryWatchServiceFactory.getDirectoryWatchService(file);
        if (PreferencesController.getInstance().getWatchForDirChanges()) {
            dirListener.ifPresent(directoryWatchService -> {
                directoryWatchService.addListener(NotificationService.getInstance());
                directoryWatchService.startWatching();
            });
        }
        tab.setOnCloseRequest(event -> {
            LogEventRepository.removeRepository(file.getAbsolutePath());
            dirListener.ifPresent(DirectoryWatchService::stopWatching);
            DirectoryWatchServiceFactory.removeWatchedDir(file);
        });
    }
}