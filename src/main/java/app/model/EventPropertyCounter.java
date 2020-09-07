/*
 * Created 2019. Open source.
 */

package app.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author JKlepek
 *
 */
public class EventPropertyCounter {

    private final StringProperty name = new SimpleStringProperty("");
    private final LongProperty count = new SimpleLongProperty(1L);

    public EventPropertyCounter(String propertyValue) {
        this.name.set(propertyValue);
    }

    public EventPropertyCounter(String propertyValue, long count) {
        this.name.set(propertyValue);
        this.count.set(count);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public long getCount() {
        return count.get();
    }

    public LongProperty countProperty() {
        return count;
    }

    public void incrementCount() {
        this.count.set(count.get() + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EventPropertyCounter) {
            return  ((EventPropertyCounter) o).getName().equals(name.get());
        }
        return false;
    }
}
