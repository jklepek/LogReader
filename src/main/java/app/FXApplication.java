package app;

import app.tools.PreferencesRepository;
import app.tools.notifications.NotificationService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FXApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/mainWindow.fxml"));
        primaryStage.setTitle("Log reader");
        primaryStage.setScene(new Scene(root, 1280, 800));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/reader.png")));
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            PreferencesRepository.saveAllPreferences();
            Platform.exit();
            System.exit(0);
        });
        PreferencesRepository.loadPreferences();
        NotificationService.startService();
    }
}
