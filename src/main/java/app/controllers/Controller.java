package app.controllers;

import app.core.LogEventRepository;
import app.core.Parser;
import app.notifications.NotificationService;
import app.preferences.PreferencesController;
import app.tools.DirectoryWatchService;
import app.tools.DirectoryWatchServiceFactory;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class Controller {

    public static final Logger LOG = LoggerFactory.getLogger(Controller.class);

    @FXML
    private BorderPane borderPane;
    @FXML
    private Button settingsButton;
    @FXML
    private TabPane tabPane;
    @FXML
    private ProgressBar progressBar;

    private FileChooser fileChooser;

    public void initialize() {
        fileChooser = new FileChooser();
        Image settingsImage = new Image(getClass().getResourceAsStream("/icons/settings.png"), 17, 17, true, true);
        settingsButton.setGraphic(new ImageView(settingsImage));
        progressBar.setVisible(false);
    }

    @FXML
    public void openFile() {
        configureFileChooser(fileChooser);
        Window window = borderPane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file != null && !LogEventRepository.isOpened(file.getAbsolutePath())) {
            progressBar.setVisible(true);
            LogEventRepository.createNewRepository(file.getAbsolutePath());
            long start = System.currentTimeMillis();
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    new Parser(
                            PreferencesController.getInstance().getCurrentLogPattern()
                    ).getLogEventsFromFile(file);
                    return null;
                }
            };
            progressBar.progressProperty().bind(task.progressProperty());
            task.setOnSucceeded(e -> {
                progressBar.setVisible(false);
                createTab(file);
                long end = System.currentTimeMillis();
                LOG.info("Loaded {} in {} ms.", file.getAbsolutePath(), end - start);
                long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                LOG.debug("Memory consumption: {} MB", usedMemory / 1000000);
            });
            task.setOnFailed(e -> {
                progressBar.setVisible(false);
                LOG.error("Failed to load {}", file.getAbsolutePath());
            });
            LOG.info("Loading {} ...", file.getAbsolutePath());
            new Thread(task).start();
        }
    }

    private void configureFileChooser(FileChooser chooser) {
        chooser.setTitle("Select log file");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Log", "*.log", "*.txt"));
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
            LOG.error("Could not load fxml file", e);
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