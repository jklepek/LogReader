package app.model;

import javafx.beans.property.SimpleStringProperty;

public class LogEvent {

    private SimpleStringProperty timestamp = new SimpleStringProperty("");
    private SimpleStringProperty level = new SimpleStringProperty("");
    private SimpleStringProperty emitter = new SimpleStringProperty("");
    private SimpleStringProperty message = new SimpleStringProperty("");
    private String stackTrace;

    public LogEvent(String timestamp, String level, String emitter, String message, String stackTrace) {
        this.timestamp.set(timestamp);
        this.level.set(level);
        this.emitter.set(emitter);
        this.message.set(message);
        this.stackTrace = stackTrace;
    }

    public String getTimestamp() {
        return timestamp.get();
    }

    public SimpleStringProperty timestampProperty() {
        return timestamp;
    }

    public String getLevel() {
        return level.get();
    }

    public SimpleStringProperty levelProperty() {
        return level;
    }

    public String getEmitter() {
        return emitter.get();
    }

    public SimpleStringProperty emitterProperty() {
        return emitter;
    }

    public String getMessage() {
        return message.get();
    }

    public SimpleStringProperty messageProperty() {
        return message;
    }

    public String getStackTrace(){
        return this.stackTrace;
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "timestamp=" + timestamp +
                ", level=" + level +
                ", emitter=" + emitter +
                ", message=" + message +
                ", stackTrace='" + stackTrace + '\'' +
                '}';
    }
}
