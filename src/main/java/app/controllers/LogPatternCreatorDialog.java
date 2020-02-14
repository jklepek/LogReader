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
    public TextField timestampPatternField;
    @FXML
    public ComboBox<String> delimiterComboBox;
    @FXML
    public Label result;
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
        List<String> list = Arrays.stream(PatternKeywords.values())
                .map(Enum::name)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
        keywords = FXCollections.observableArrayList(list);
        delimiterComboBox.getItems().addAll("/", " ", "-", ";");
        buttonBox.getChildren().addListener((ListChangeListener<? super Node>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    ComboBox<String> button = (ComboBox<String>) buttonBox.getChildren().get(keywordComboCounter);
                    button.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != null && !newValue.isEmpty() && Integer.parseInt(button.getId()) == (keywordComboCounter)) {
                            newKeywords.add(newValue);
                            keywordComboCounter++;
                            addKeywordButton(keywordComboCounter);
                        }
                    });
                }
            }
        });
        addKeywordButton(0);
    }

    public void openFile() {
        Window window = dialogPane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            StringBuilder firstLines = readFirstLines(file);
            sampleField.setText(firstLines.toString());
        }
    }

    public void addKeywordButton(int position) {
        ComboBox<String> keywordBox = new ComboBox<>();
        keywordBox.setItems(keywords);
        List<Node> buttons = buttonBox.getChildren();
        keywordBox.setId(String.valueOf(position));
        buttons.add(keywordBox);
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

    private String buildPatternFromKeywords() {
        StringBuilder pattern = new StringBuilder();
        buttonBox.getChildren()
                .stream()
                .sorted(Comparator.comparingInt(value -> Integer.parseInt(value.getId())))
                .map(node -> ((ComboBox<String>) node).getSelectionModel().getSelectedItem())
                .filter(Objects::nonNull)
                .map(s -> {
                    String timestampPattern;
                    if (s.equals(PatternKeywords.TIMESTAMP.name())) {
                        timestampPattern = s.replace(PatternKeywords.TIMESTAMP.name(), String.format("D{%s}", timestampPatternField.getText()));
                        return timestampPattern;
                    }
                    return s;
                })
                .forEach(s -> pattern.append("%").append(s).append(" "));
        return pattern.toString();
    }

    public void tryPattern() {
        String pattern = buildPatternFromKeywords();
        parser = new Parser(pattern, delimiterComboBox.getSelectionModel().getSelectedItem());
        LogEvent event = parser.parse(sampleField.getText());
        StringBuilder s = new StringBuilder();
        for (String keyword : parser.getKeywords()) {
            s.append(event.getProperty(keyword)).append("--");
        }
        result.setText(s.toString());
    }

}