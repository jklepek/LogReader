package app.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * LogEvent object is stored in ObservableList, to be displayed in UI table.
 */
public class LogEvent {

    private StringProperty timestamp;
    private StringProperty level;
    private StringProperty emitter;
    private StringProperty message;
    private StringProperty thread;
    private StringProperty mdc;
    private StringProperty stacktrace;

    private LogEvent() {
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

    public Object getPropertyByName(String propertyName) {
        Method method;
        try {
            method = this.getClass().getMethod(propertyName.toLowerCase() + "Property");
            return method.invoke(this);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static class Builder {

        private StringProperty timestamp = new SimpleStringProperty("");
        private StringProperty level = new SimpleStringProperty("");
        private StringProperty emitter = new SimpleStringProperty("");
        private StringProperty message = new SimpleStringProperty("");
        private StringProperty thread = new SimpleStringProperty("");
        private StringProperty mdc = new SimpleStringProperty("");
        private StringProperty stacktrace = new SimpleStringProperty("");

        public Builder() {
        }

        public Builder timestamp(String timestamp) {
            this.timestamp.set(timestamp);
            return this;
        }

        public Builder level(String level) {
            this.level.set(level);
            return this;
        }

        public Builder emitter(String emitter) {
            this.emitter.set(emitter);
            return this;
        }

        public Builder message(String message) {
            this.message.set(message);
            return this;
        }

        public Builder stacktrace(String stacktrace) {
            this.stacktrace.set(stacktrace);
            return this;
        }

        public Builder mdc(String mdc) {
            this.mdc.set(mdc);
            return this;
        }

        public Builder thread(String thread) {
            this.thread.set(thread);
            return this;
        }

        public LogEvent build() {
            LogEvent event = new LogEvent();
            event.timestamp = this.timestamp;
            event.level = this.level;
            event.emitter = this.emitter;
            event.message = this.message;
            event.stacktrace = this.stacktrace;
            event.mdc = this.mdc;
            event.thread = this.thread;
            return event;
        }
    }
}
