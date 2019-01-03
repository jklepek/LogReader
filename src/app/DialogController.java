package app;

import app.utils.PreferencesController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class DialogController {

    private final PreferencesController preferences = PreferencesController.getInstance();
    @FXML
    private DialogPane dialogPane;
    @FXML
    private TextField preferredDirectoryField;
    @FXML
    private TextField autoRefreshIntervalField;
    @FXML
    private Button browseButton;
    @FXML
    private ImageView dirErrorImageView = new ImageView();
    @FXML
    private ImageView intervalErrorImageView = new ImageView();

    public void initialize() {
        Image openFolderImage = new Image(getClass().getResourceAsStream("resources/openFolder.png"), 17, 17, true, true);
        Image errorImage = new Image(getClass().getResourceAsStream("resources/error.png"), 17, 17, true, true);
        intervalErrorImageView.setImage(errorImage);
        intervalErrorImageView.setVisible(false);
        dirErrorImageView.setImage(errorImage);
        dirErrorImageView.setVisible(false);
        Tooltip dirErrorTooltip = new Tooltip("Path is not valid.");
        Tooltip intervalErrorTooltip = new Tooltip("Value must be a number.");
        browseButton.setGraphic(new ImageView(openFolderImage));
        preferredDirectoryField.setText(preferences.getPreferredDir());
        preferredDirectoryField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue) {
                File file = new File(preferredDirectoryField.getText());
                if (!file.exists() && !file.isDirectory()) {
                    dirErrorImageView.setVisible(true);
                    Tooltip.install(dirErrorImageView, dirErrorTooltip);
                } else {
                    dirErrorImageView.setVisible(false);
                    Tooltip.uninstall(dirErrorImageView, dirErrorTooltip);
                }
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
    }

    public void savePreferences() {
        if (!preferredDirectoryField.getText().isEmpty() && !autoRefreshIntervalField.getText().isEmpty()) {
            String dir = preferredDirectoryField.getText();
            preferences.setPreferredDir(dir);
            preferences.setAutoRefreshInterval(Long.valueOf(autoRefreshIntervalField.getText()));
        }
    }

    @FXML
    public void selectDir() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select log directory");
        File file = directoryChooser.showDialog(dialogPane.getScene().getWindow());
        if (file != null) {
            preferredDirectoryField.setText(file.getAbsolutePath());
        }
    }
}
