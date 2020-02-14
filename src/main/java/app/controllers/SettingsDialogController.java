package app.controllers;

import app.preferences.PreferencesController;
import javafx.beans.binding.BooleanBinding;
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
import java.util.Map;
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
    public TextField delimiterField;
    @FXML
    public TextField displayDelimiterField;
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
    private final List<String> patternsToDelete = new ArrayList<>();

    public void initialize() {
        initImages();
        initTooltips();
        initBindings();
        initListeners();
        initFields();
    }

    public void savePreferences() {
        if (!initialDirField.getText().isEmpty() && !autoRefreshIntervalField.getText().isEmpty()
                && patternField.getText() != null && patternsComboBox.getValue() != null) {
            String dir = initialDirField.getText();
            PreferencesController.getInstance().setInitialDir(dir);
            PreferencesController.getInstance().setAutoRefreshInterval(Long.parseLong(autoRefreshIntervalField.getText()));
            PreferencesController.getInstance().setWatchForDirChanges(watchDir.isSelected());
            PreferencesController.getInstance().addLogPattern(patternsComboBox.getValue(), patternField.getText());
            PreferencesController.getInstance().setLogPattern(patternField.getText());
            for (String pattern : patternsToDelete) {
                PreferencesController.getInstance().removePattern(pattern);
            }
            PreferencesController.getInstance().setDelimiter(delimiterField.getText());
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
        String patternName = patternsComboBox.getSelectionModel().getSelectedItem();
        patternsToDelete.add(patternName);
        patternsComboBox.getItems().remove(patternName);
        patternMap.remove(patternName);
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
        initialDirField.setText(PreferencesController.getInstance().getInitialDir());
        autoRefreshIntervalField.setText(String.valueOf(PreferencesController.getInstance().getAutoRefreshInterval()));
        watchDir.setSelected(PreferencesController.getInstance().getWatchForDirChanges());
        patternMap = PreferencesController.getInstance().getLogPatterns();
        patternsComboBox.getItems().addAll(patternMap.keySet());
        patternsComboBox.setEditable(true);
        delimiterField.setText(PreferencesController.getInstance().getDelimiter());
        displayDelimiterField.setText("\"" + delimiterField.getText() + "\"");
        delimiterField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!oldValue.equalsIgnoreCase(newValue)) {
                displayDelimiterField.setText("\"" + newValue + "\"");
            }
        });
        String currentPattern = PreferencesController.getInstance().getLogPattern();
        if (!currentPattern.equals("")) {
            patternField.setText(currentPattern);
            patternMap.forEach((key, value) -> {
                if (value.equals(currentPattern)) {
                    patternsComboBox.getSelectionModel().select(key);
                }
            });
        }
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
        patternsComboBox.valueProperty().addListener((observable, oldValue, newValue) -> patternField.textProperty().setValue(patternMap.get(newValue)));
    }

    private void initBindings() {
        validContent = new BooleanBinding() {
            {
                bind(autoRefreshIntervalField.textProperty(),
                        initialDirField.textProperty(),
                        patternField.textProperty());
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
