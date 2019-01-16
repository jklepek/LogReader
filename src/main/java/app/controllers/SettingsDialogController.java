package app.controllers;

import app.utils.PreferencesController;
import javafx.beans.binding.BooleanBinding;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

import java.io.File;
import java.util.Map;

public class SettingsDialogController {

    private final PreferencesController preferences = PreferencesController.getInstance();
    @FXML
    private DialogPane dialogPane;
    @FXML
    private TextField initialDirField;
    @FXML
    private TextField autoRefreshIntervalField;
    @FXML
    private Button browseButton;
    @FXML
    private ImageView dirErrorIV = new ImageView();
    @FXML
    private ImageView intervalErrorIV = new ImageView();
    @FXML
    private Button deletePatternButton;
    @FXML
    private CheckBox watchDir = new CheckBox();
    @FXML
    private ComboBox<String> patternsComboBox = new ComboBox<>();
    @FXML
    private TextField patternField;
    private BooleanBinding validContent;
    private Map<String, String> patternMap;

    public void initialize() {
        Image openFolderImage = new Image(getClass().getResourceAsStream("/icons/openFolder.png"), 17, 17, true, true);
        Image errorImage = new Image(getClass().getResourceAsStream("/icons/error.png"), 17, 17, true, true);
        Image deleteImage = new Image(getClass().getResourceAsStream("/icons/delete.png"), 17, 17, true, true);
        intervalErrorIV.setImage(errorImage);
        intervalErrorIV.setVisible(false);
        dirErrorIV.setImage(errorImage);
        dirErrorIV.setVisible(false);
        Tooltip dirErrorTooltip = new Tooltip("Path is not valid.");
        dirErrorTooltip.setShowDelay(Duration.millis(150));
        Tooltip intervalErrorTooltip = new Tooltip("Value must be a number.");
        intervalErrorTooltip.setShowDelay(Duration.millis(150));
        browseButton.setGraphic(new ImageView(openFolderImage));
        deletePatternButton.setGraphic(new ImageView(deleteImage));
        initialDirField.setText(preferences.getInitialDir());
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
        autoRefreshIntervalField.setText(String.valueOf(preferences.getAutoRefreshInterval()));
        autoRefreshIntervalField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                intervalErrorIV.setVisible(true);
                Tooltip.install(intervalErrorIV, intervalErrorTooltip);
            } else {
                intervalErrorIV.setVisible(false);
                Tooltip.uninstall(intervalErrorIV, intervalErrorTooltip);
            }
        });
        watchDir.setSelected(preferences.getWatchForDirChanges());
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
                        || !file.exists());
            }
        };
        patternMap = preferences.getLogPatterns();
        patternsComboBox.getItems().addAll(patternMap.keySet());
        patternsComboBox.setEditable(true);
        String currentPattern = preferences.getLogPattern();
        if (!currentPattern.equals("")) {
            patternField.setText(currentPattern);
            patternMap.forEach((key, value) -> {
                if (value.equals(currentPattern)) {
                    patternsComboBox.getSelectionModel().select(key);
                }
            });
        }
    }

    public void savePreferences() {
        if (!initialDirField.getText().isEmpty() && !autoRefreshIntervalField.getText().isEmpty()
                && patternField.getText() != null && patternsComboBox.getValue() != null) {
            String dir = initialDirField.getText();
            preferences.setInitialDir(dir);
            preferences.setAutoRefreshInterval(Long.valueOf(autoRefreshIntervalField.getText()));
            preferences.setWatchForDirChanges(watchDir.isSelected());
            preferences.addLogPattern(patternsComboBox.getValue(), patternField.getText());
            preferences.setLogPattern(patternField.getText());
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
    private void setLogPatternField() {
        String patternName = patternsComboBox.getSelectionModel().getSelectedItem();
        if (patternName != null) {
            patternField.textProperty().setValue(patternMap.get(patternName));
        }
    }

    @FXML
    public void deletePattern() {
        String patternName = patternsComboBox.getSelectionModel().getSelectedItem();
        patternsComboBox.getItems().remove(patternName);
        preferences.removePattern(patternName);
        patternMap.remove(patternName);
        patternField.clear();
    }
}
