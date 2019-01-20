package app.model;

import javafx.beans.property.SimpleStringProperty;

public class LogEvent {

    private final SimpleStringProperty timestamp = new SimpleStringProperty("");
    private final SimpleStringProperty level = new SimpleStringProperty("");
    private final SimpleStringProperty emitter = new SimpleStringProperty("");
    private final SimpleStringProperty message = new SimpleStringProperty("");
    private final SimpleStringProperty thread = new SimpleStringProperty("");
    private final SimpleStringProperty mdc = new SimpleStringProperty("");
    private final String stackTrace;

    public LogEvent(String timestamp, String level, String emitter, String message, String thread, String mdc, String stackTrace) {
        this.timestamp.set(timestamp);
        this.level.set(level);
        this.emitter.set(emitter);
        this.message.set(message);
        this.stackTrace = stackTrace;
        this.thread.set(thread);
        this.mdc.set(mdc);
    }

    public String getTimestamp() {
        return timestamp.get();
    }

    public String getLevel() {
        return level.get();
    }

    public String getEmitter() {
        return emitter.get();
    }

    public String getMessage() {
        return message.get();
    }

    public String getStackTrace() {
        return this.stackTrace;
    }

    public String getThread() {
        return thread.get();
    }

    public String getMdc() {
        return mdc.get();
    }
}
