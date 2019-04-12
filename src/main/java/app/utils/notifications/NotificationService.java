package app.utils.notifications;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
public class NotificationService {

    private static final ObservableList<EventNotification> NOTIFICATIONS = FXCollections.observableArrayList();
    private static Stage stage;

    /**
     * Starts the service
     */
    public static void startService() {
        NOTIFICATIONS.addListener((ListChangeListener<EventNotification>) c -> {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            EventNotification notification = NOTIFICATIONS.get(NOTIFICATIONS.size() - 1);
                            createHiddenStage();
                            Platform.runLater(() -> createNotification(notification));
                        }
                    }
                }
        );
    }

    /**
     * Creates notification based on it's type
     * @param notification EventNotification
     */
    private static void createNotification(EventNotification notification) {
        switch (notification.getType()) {
            case ERROR:
                Notifications
                        .create()
                        .title(notification.getTitle())
                        .text(notification.getText())
                        .showError();
                break;
            case WARNING:
                Notifications
                        .create()
                        .title(notification.getTitle())
                        .text(notification.getText())
                        .showWarning();
                break;
            case INFORMATION:
                Notifications
                        .create()
                        .title(notification.getTitle())
                        .text(notification.getText())
                        .showInformation();
                break;
            case CONFIRMATION:
                Notifications
                        .create()
                        .title(notification.getTitle())
                        .text(notification.getText())
                        .showConfirm();
                break;
        }
    }

    /**
     * Adds new notification to the ObservableList
     * @param notification EventNotification
     */
    public static void addNotification(EventNotification notification) {
        NOTIFICATIONS.add(notification);
    }

    public static ObservableList<EventNotification> getNotifications() {
        return NOTIFICATIONS;
    }

    /**
     * Hidden stage is necessary to avoid null pointer exception
     * when the application is minimized or not focused.
     * This hidden stage allows notifications to be displayed at all times.
     */
    private static void createHiddenStage() {
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
