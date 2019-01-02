package app;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.text.SimpleDateFormat;

public class DialogController {

    @FXML
    private TextField timestampFormat;

    public void saveTimestampFormat() {
        String timestamp = timestampFormat.getText();
        SimpleDateFormat sdf = new SimpleDateFormat(timestamp);
        String timestampRegex = sdf.toPattern();
        System.out.println(timestampRegex);
    }
}
