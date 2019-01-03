package app;

import app.utils.LogTailer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("fxml/mainWindow.fxml"));
        primaryStage.setTitle("Log reader");
        primaryStage.setScene(new Scene(root, primaryStage.getMaxWidth(), primaryStage.getMaxHeight()));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            LogTailer.getInstance().stopTailing();
            Platform.exit();
            System.exit(0);
        });

    }
}
