package app.controllers;

import app.model.LogPattern;
import app.preferences.PreferencesController;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SettingsDialogController {

    private static final String OPEN_FOLDER_ICON = "/icons/openFolder.png";
    private static final String ERROR_ICON = "/icons/error.png";
    private static final String DELETE_ICON = "/icons/delete.png";
    private static final String PLUS_ICON = "/icons/plus.png";
    private Tooltip dirErrorTooltip;
    private Tooltip intervalErrorTooltip;
    @FXML
    public Button createPattern;
    @FXML
    private DialogPane dialogPane;
    @FXML
    private TextField initialDirField;
    @FXML
    private TextField autoRefreshIntervalField;
    @FXML
    private Button browseButton;
    @FXML
    private ImageView dirErrorIV;
    @FXML
    private ImageView intervalErrorIV;
    @FXML
    private Button deletePatternButton;
    @FXML
    private CheckBox watchDir;
    @FXML
    private ComboBox<LogPattern> patternsComboBox;
    private BooleanBinding validContent;
    private ObservableList<LogPattern> patterns;
    private final List<LogPattern> patternsToDelete = new ArrayList<>();

    public void initialize() {
        initImages();
        initTooltips();
        initBindings();
        initListeners();
        initFields();
    }

    public void savePreferences() {
        if (!initialDirField.getText().isEmpty() && !autoRefreshIntervalField.getText().isEmpty()
                && patternsComboBox.getValue() != null) {
            String dir = initialDirField.getText();
            PreferencesController instance = PreferencesController.getInstance();
            instance.setInitialDir(dir);
            instance.setAutoRefreshInterval(Long.parseLong(autoRefreshIntervalField.getText()));
            instance.setWatchForDirChanges(watchDir.isSelected());
            LogPattern selectedItem = patternsComboBox.getSelectionModel().getSelectedItem();
            instance.setCurrentLogPattern(selectedItem.getPattern());
            for (LogPattern pattern : patterns) {
                instance.addLogPattern(pattern.getName(), pattern.getPattern());
            }
            for (LogPattern patternToDelete : patternsToDelete) {
                instance.removePattern(patternToDelete.getName());
            }
        }
    }

    @FXML
    public void selectDir() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select log directory");
        File file = directoryChooser.showDialog(dialogPane.getScene().getWindow());
        if (file != null) {
            initialDirField.setText(file.getAbsolutePath());
        }
    }

    public BooleanBinding validContentProperty() {
        return validContent;
    }

    @FXML
    public void deletePattern() {
        LogPattern patternName = patternsComboBox.getSelectionModel().getSelectedItem();
        patternsToDelete.add(patternName);
        patterns.remove(patternName);
    }

    private void initImages() {
        Image openFolderImage = new Image(getClass().getResourceAsStream(OPEN_FOLDER_ICON), 17, 17, true, true);
        Image errorImage = new Image(getClass().getResourceAsStream(ERROR_ICON), 17, 17, true, true);
        Image deleteImage = new Image(getClass().getResourceAsStream(DELETE_ICON), 17, 17, true, true);
        Image plusImage = new Image(getClass().getResourceAsStream(PLUS_ICON), 17, 17, true, true);
        intervalErrorIV.setImage(errorImage);
        intervalErrorIV.setVisible(false);
        dirErrorIV.setImage(errorImage);
        dirErrorIV.setVisible(false);
        browseButton.setGraphic(new ImageView(openFolderImage));
        deletePatternButton.setGraphic(new ImageView(deleteImage));
        createPattern.setGraphic(new ImageView(plusImage));
    }

    private void initFields() {
        PreferencesController instance = PreferencesController.getInstance();
        initialDirField.setText(instance.getInitialDir());
        autoRefreshIntervalField.setText(String.valueOf(instance.getAutoRefreshInterval()));
        watchDir.setSelected(instance.getWatchForDirChanges());
        patterns = FXCollections.observableArrayList(instance.getLogPatterns());
        patternsComboBox.setItems(patterns);
        Optional<LogPattern> current = patterns.stream().filter(p -> p.getPattern().equals(instance.getCurrentLogPattern())).findFirst();
        current.ifPresent(logPattern -> patternsComboBox.getSelectionModel().select(logPattern));
    }

    private void initTooltips() {
        dirErrorTooltip = new Tooltip("Path is not valid.");
        dirErrorTooltip.setShowDelay(Duration.millis(150));
        intervalErrorTooltip = new Tooltip("Value must be a number.");
        intervalErrorTooltip.setShowDelay(Duration.millis(150));
    }

    public void openPatternCreatorDialog() {
        Dialog<ButtonType> patternCreator = new Dialog<>();
        patternCreator.initOwner(dialogPane.getScene().getWindow());
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/logPatternCreatorDialog.fxml"));
        try {
            patternCreator.getDialogPane().setContent(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        LogPatternCreatorDialog patternCreatorDialog = loader.getController();
        patternCreator.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = patternCreator.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            LogPattern pattern = patternCreatorDialog.saveNewPattern();
            patterns.add(pattern);
        }
    }

    private void initListeners() {
        initialDirField.textProperty().addListener((observable, oldValue, newValue) -> {
            File file = new File(initialDirField.getText());
            if (!file.exists() && !file.isDirectory()) {
                dirErrorIV.setVisible(true);
                Tooltip.install(dirErrorIV, dirErrorTooltip);
            } else {
                dirErrorIV.setVisible(false);
                Tooltip.uninstall(dirErrorIV, dirErrorTooltip);
            }
        });
        autoRefreshIntervalField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                intervalErrorIV.setVisible(true);
                Tooltip.install(intervalErrorIV, intervalErrorTooltip);
            } else {
                intervalErrorIV.setVisible(false);
                Tooltip.uninstall(intervalErrorIV, intervalErrorTooltip);
            }
        });
    }

    private void initBindings() {
        validContent = new BooleanBinding() {
            {
                bind(autoRefreshIntervalField.textProperty(),
                        initialDirField.textProperty());
            }

            @Override
            protected boolean computeValue() {
                File file = new File(initialDirField.getText());
                return (!autoRefreshIntervalField.getText().matches("\\d*")
                        || autoRefreshIntervalField.getText().isEmpty()
                        || !file.exists()
                );
            }
        };
    }
}
