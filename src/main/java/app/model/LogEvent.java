package app.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * LogEvent object is stored in ObservableList, to be displayed in UI table.
 */
public class LogEvent {

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
            System.out.println("No such property: " + propertyName);
        }
        return "";
    }

    public boolean hasProperty(String propertyName) {
        return propertyMap.containsKey(propertyName);
    }
}
