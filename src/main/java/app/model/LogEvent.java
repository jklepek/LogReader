package app.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.lang.reflect.Method;

/**
 * LogEvent object is stored in ObservableList, to be displayed in UI table.
 */
public class LogEvent {

    private final StringProperty timestamp = new SimpleStringProperty("");
    private final StringProperty level = new SimpleStringProperty("");
    private final StringProperty emitter = new SimpleStringProperty("");
    private final StringProperty message = new SimpleStringProperty("");
    private final StringProperty thread = new SimpleStringProperty("");
    private final StringProperty mdc = new SimpleStringProperty("");
    private final StringProperty stacktrace = new SimpleStringProperty("");

    public LogEvent(String timestamp, String level, String emitter, String message, String thread, String mdc, String stacktrace) {
        this.timestamp.set(timestamp);
        this.level.set(level);
        this.emitter.set(emitter);
        this.message.set(message);
        this.stacktrace.set(stacktrace);
        this.thread.set(thread);
        this.mdc.set(mdc);
    }

    public StringProperty timestampProperty() {
        return timestamp;
    }

    public StringProperty levelProperty() {
        return level;
    }

    public StringProperty emitterProperty() {
        return emitter;
    }

    public StringProperty messageProperty() {
        return message;
    }

    public StringProperty threadProperty() {
        return thread;
    }

    public StringProperty mdcProperty() {
        return mdc;
    }

    public StringProperty stacktraceProperty() {
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
