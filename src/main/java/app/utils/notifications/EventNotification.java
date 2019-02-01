package app.utils.notifications;

/**
 * Notification object
 */
public class EventNotification {

    private final String title;
    private final String text;
    private final NotificationType type;

    public EventNotification(String title, String text, NotificationType type) {
        this.title = title;
        this.text = text;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public NotificationType getType() {
        return type;
    }
}
