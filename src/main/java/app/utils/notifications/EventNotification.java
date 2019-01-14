package app.utils.notifications;

public class EventNotification {

    private String title;
    private String text;
    private NotificationType type;

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
