package app;

import app.utils.PreferencesController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class DialogController {

    @FXML
    private DialogPane dialogPane;

    @FXML
    private TextField preferredDirectoryField;

    @FXML
    private TextField autoRefreshIntervalField;

    @FXML
    private Button browseButton;

    private File file;
    private PreferencesController preferences = PreferencesController.getInstance();

    public void initialize() {
        Image openFolderImage = new Image(getClass().getResourceAsStream("resources/openFolder.png"), 17, 17, true, true);
        browseButton.setGraphic(new ImageView(openFolderImage));
        preferredDirectoryField.setText(preferences.getPreferedDir());
        autoRefreshIntervalField.setText(String.valueOf(preferences.getAutoRefreshInterval()));
    }

    public void savePreferences() {
        if (file != null && file.isDirectory()) {
            preferences.setPreferedDir(file.getAbsolutePath());
        }
        if (!autoRefreshIntervalField.getText().isEmpty()) {
            preferences.setAutoRefreshInterval(Long.valueOf(autoRefreshIntervalField.getText()));
        }
    }

    @FXML
    public void selectDir() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select log directory");
        file = directoryChooser.showDialog(dialogPane.getScene().getWindow());
        preferredDirectoryField.setText(file.getAbsolutePath());
    }
}
