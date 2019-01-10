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

public class DialogController {

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
    private ImageView dirErrorImageView = new ImageView();
    @FXML
    private ImageView intervalErrorImageView = new ImageView();
    private BooleanBinding validContent;

    public void initialize() {
        Image openFolderImage = new Image(getClass().getResourceAsStream("../resources/openFolder.png"), 17, 17, true, true);
        Image errorImage = new Image(getClass().getResourceAsStream("../resources/error.png"), 17, 17, true, true);
        intervalErrorImageView.setImage(errorImage);
        intervalErrorImageView.setVisible(false);
        dirErrorImageView.setImage(errorImage);
        dirErrorImageView.setVisible(false);
        Tooltip dirErrorTooltip = new Tooltip("Path is not valid.");
        dirErrorTooltip.setShowDelay(Duration.millis(150));
        Tooltip intervalErrorTooltip = new Tooltip("Value must be a number.");
        intervalErrorTooltip.setShowDelay(Duration.millis(150));
        browseButton.setGraphic(new ImageView(openFolderImage));
        initialDirField.setText(preferences.getInitialDir());
        initialDirField.textProperty().addListener((observable, oldValue, newValue) -> {
            File file = new File(initialDirField.getText());
            if (!file.exists() && !file.isDirectory()) {
                dirErrorImageView.setVisible(true);
                Tooltip.install(dirErrorImageView, dirErrorTooltip);
            } else {
                dirErrorImageView.setVisible(false);
                Tooltip.uninstall(dirErrorImageView, dirErrorTooltip);
            }
        });
        autoRefreshIntervalField.setText(String.valueOf(preferences.getAutoRefreshInterval()));
        autoRefreshIntervalField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                intervalErrorImageView.setVisible(true);
                Tooltip.install(intervalErrorImageView, intervalErrorTooltip);
            } else {
                intervalErrorImageView.setVisible(false);
                Tooltip.uninstall(intervalErrorImageView, intervalErrorTooltip);
            }
        });
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
    }

    public void savePreferences() {
        if (!initialDirField.getText().isEmpty() && !autoRefreshIntervalField.getText().isEmpty()) {
            String dir = initialDirField.getText();
            preferences.setInitialDir(dir);
            preferences.setAutoRefreshInterval(Long.valueOf(autoRefreshIntervalField.getText()));
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
}
