package app.utils.notifications;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import org.controlsfx.control.Notifications;

public class NotificationService {

    private static final ObservableList<EventNotification> NOTIFICATIONS = FXCollections.observableArrayList();

    public static void startService(Stage stage) {
        NOTIFICATIONS.addListener((ListChangeListener<EventNotification>) c -> {
                    while (c.next()) {
                        if (c.wasAdded()) {
                            EventNotification notification = NOTIFICATIONS.get(NOTIFICATIONS.size() - 1);
                            if (!stage.isFocused() || !stage.isShowing()) {
                                Platform.runLater(() -> {
                                    stage.show();
                                    stage.requestFocus();
                                    createNotification(notification);
                                });
                            }
                        }
                    }
                }
        );
    }

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

    public static void addNotification(EventNotification notification) {
        NOTIFICATIONS.add(notification);
    }

    public static ObservableList<EventNotification> getNotifications() {
        return NOTIFICATIONS;
    }
}
