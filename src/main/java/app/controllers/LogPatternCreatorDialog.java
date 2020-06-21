package app.controllers;

import app.core.Parser;
import app.model.LogEvent;
import app.model.PatternKeywords;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LogPatternCreatorDialog {

    @FXML
    public DialogPane dialogPane;
    @FXML
    public TextArea sampleField;
    @FXML
    public Button openFileButton;
    @FXML
    public Label result;
    @FXML
    public TextField patternField;
    private FileChooser fileChooser;
    private static final String OPEN_FOLDER_ICON = "/icons/openFolder.png";
    private Parser parser;
    @FXML
    public Button tryPatternButton;
    @FXML
    public HBox buttonBox;
    private ObservableList<String> keywords;
    private List<String> newKeywords = new ArrayList<>();
    private int keywordComboCounter = 0;

    public void initialize() {
        fileChooser = new FileChooser();
        Image openFolderImage = new Image(getClass().getResourceAsStream(OPEN_FOLDER_ICON), 17, 17, true, true);
        openFileButton.setGraphic(new ImageView(openFolderImage));
    }

    public void openFile() {
        Window window = dialogPane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            StringBuilder firstLines = readFirstLines(file);
            sampleField.setText(firstLines.toString());
        }
    }


    private StringBuilder readFirstLines(File file) {
        StringBuilder buffer = new StringBuilder();
        int counter = 1;
        try (FileReader reader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            while (counter < 2) {
                buffer.append(bufferedReader.readLine()).append(System.lineSeparator());
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }


    public void tryPattern() {
        parser = new Parser(patternField.getText());
        LogEvent event = parser.parse(sampleField.getText());
        StringBuilder s = new StringBuilder();
        for (String keyword : parser.getKeywords()) {
            s.append(event.getProperty(keyword)).append("--");
        }
        result.setText(s.toString());
    }

}