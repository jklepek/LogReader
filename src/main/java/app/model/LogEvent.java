package app.model;

import javafx.beans.property.SimpleStringProperty;

import java.lang.reflect.Method;

/**
 * LogEvent object is stored in ObservableList, to be displayed in UI table.
 */
public class LogEvent {

    private final SimpleStringProperty timestamp = new SimpleStringProperty("");
    private final SimpleStringProperty level = new SimpleStringProperty("");
    private final SimpleStringProperty emitter = new SimpleStringProperty("");
    private final SimpleStringProperty message = new SimpleStringProperty("");
    private final SimpleStringProperty thread = new SimpleStringProperty("");
    private final SimpleStringProperty mdc = new SimpleStringProperty("");
    private final SimpleStringProperty stacktrace = new SimpleStringProperty("");

    public LogEvent(String timestamp, String level, String emitter, String message, String thread, String mdc, String stacktrace) {
        this.timestamp.set(timestamp);
        this.level.set(level);
        this.emitter.set(emitter);
        this.message.set(message);
        this.stacktrace.set(stacktrace);
        this.thread.set(thread);
        this.mdc.set(mdc);
    }

    public SimpleStringProperty timestampProperty() {
        return timestamp;
    }

    public SimpleStringProperty levelProperty() {
        return level;
    }

    public SimpleStringProperty emitterProperty() {
        return emitter;
    }

    public SimpleStringProperty messageProperty() {
        return message;
    }

    public SimpleStringProperty threadProperty() {
        return thread;
    }

    public SimpleStringProperty mdcProperty() {
        return mdc;
    }

    public SimpleStringProperty stacktraceProperty() {
        return stacktrace;
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

    public String getStacktrace() {
        return stacktrace.get();
    }

    public String getThread() {
        return thread.get();
    }

    public String getMdc() {
        return mdc.get();
    }

    public Object getPropertyByName(String propertyName) throws Exception {
        Method method = this.getClass().getMethod(propertyName.toLowerCase() + "Property");
        return method.invoke(this);
    }
}
