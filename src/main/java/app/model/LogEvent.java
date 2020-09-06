package app.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * LogEvent object is stored in ObservableList, to be displayed in UI table.
 */
public class LogEvent {

    public static final Logger LOG = LoggerFactory.getLogger(LogEvent.class);

    private final Map<String, StringProperty> propertyMap;

    public LogEvent() {
        this.propertyMap = new HashMap<>();
    }

    public void setProperty(String propertyName, String propertyValue) {
        this.propertyMap.put(propertyName, new SimpleStringProperty(propertyValue));
    }

    public String getProperty(String propertyName) {
        try {
            return propertyMap.get(propertyName.toUpperCase()).getValue();
        } catch (Exception e) {
            LOG.warn("No such property: {}", propertyName);
        }
        return "";
    }

    public boolean hasProperty(String propertyName) {
        return propertyMap.containsKey(propertyName);
    }
}
