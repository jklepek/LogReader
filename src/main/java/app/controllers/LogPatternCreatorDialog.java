package app.controllers;

import app.core.Parser;
import app.model.LogEvent;
import app.model.LogPattern;
import app.model.ui.LogEventPropertyFactory;
import app.model.ui.LogEventTableRow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import java.util.ArrayList;
import java.util.List;

public class LogPatternCreatorDialog {

    @FXML
    public DialogPane dialogPane;
    @FXML
    public TextArea sampleField;
    @FXML
    public Button openFileButton;
    @FXML
    public TextField patternField;
    @FXML
    public TextField patternNameField;
    private FileChooser fileChooser;
    private List<String> keywords = new ArrayList<>();
    private static final String OPEN_FOLDER_ICON = "/icons/openFolder.png";
    @FXML
    public TableView<LogEvent> exampleTable;
    @FXML
    public HBox buttonBox;

    private final ObservableList<LogEvent> items = FXCollections.observableList(new ArrayList<>());

    public void initialize() {
        fileChooser = new FileChooser();
        Image openFolderImage = new Image(getClass().getResourceAsStream(OPEN_FOLDER_ICON), 17, 17, true, true);
        openFileButton.setGraphic(new ImageView(openFolderImage));
        initListener();
    }

    private void initListener() {
        patternField.textProperty().addListener((observable, oldValue, newValue) -> {
            items.clear();
            exampleTable.getColumns().clear();
            LogEvent logEvent = tryPattern();
            List<String> columnNames = new ArrayList<>();
            exampleTable.getColumns().forEach(col -> columnNames.add(col.getText()));
            keywords.forEach(keyword -> {
                if (!columnNames.contains(keyword)) {
                    TableColumn<LogEvent, String> column = new TableColumn<>(keyword);
                    column.setCellValueFactory(new LogEventPropertyFactory(keyword));
                    exampleTable.getColumns().add(column);
                }
            });
            exampleTable.setItems(items);
            exampleTable.setRowFactory(param -> new LogEventTableRow());
            items.add(logEvent);
        });
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


    public LogEvent tryPattern() {
        Parser parser = new Parser(patternField.getText());
        keywords = parser.getKeywords();
        return parser.parse(sampleField.getText());
    }

    public LogPattern saveNewPattern() {
        return new LogPattern(patternNameField.getText(), patternField.getText());
    }

}