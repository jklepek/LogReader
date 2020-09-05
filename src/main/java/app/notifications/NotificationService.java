package app.notifications;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.Notifications;

/**
 * Service watches for changes in ObservableList of EventNotifications
 * if a new event is added, notification is displayed
 */
public class NotificationService implements NotificationListener {

    private Stage stage;


    private NotificationService() {
        createHiddenStage();
    }

    public static NotificationService getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {

        static final NotificationService INSTANCE = new NotificationService();
    }

    @Override
    public void fireNotification(EventNotification notification) {
        Platform.runLater(() -> createNotification(notification));
    }

    /**
     * Creates notification based on it's type
     *
     * @param notification EventNotification
     */

    private void createNotification(EventNotification notification) {
        switch (notification.getType()) {
            case ERROR:
                buildNotification(notification).showError();
                break;
            case WARNING:
                buildNotification(notification).showWarning();
                break;
            case INFORMATION:
                buildNotification(notification).showInformation();
                break;
            case CONFIRMATION:
                buildNotification(notification).showConfirm();
                break;
        }
    }

    private Notifications buildNotification(EventNotification notification) {
        return Notifications.create()
                .owner(stage)
                .title(notification.getTitle())
                .text(notification.getText());
    }

    /**
     * Hidden stage is necessary to avoid null pointer exception
     * when the application is minimized or not focused.
     * This hidden stage allows notifications to be displayed at all times.
     */
    private void createHiddenStage() {
        Platform.runLater(() -> {
            stage = new Stage(StageStyle.TRANSPARENT);
            StackPane root = new StackPane();
            root.setStyle("-fx-background-color: TRANSPARENT");
            stage.setAlwaysOnTop(true);
            stage.initModality(Modality.NONE);
            stage.initStyle(StageStyle.UTILITY);
            stage.setOpacity(0);
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            stage.setX(bounds.getMaxX());
            stage.setY(bounds.getMaxY());
            stage.setScene(new Scene(root, 1d, 1d, Color.TRANSPARENT));
            stage.show();
        });
    }
}
