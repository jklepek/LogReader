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
 * @project LogReader
 */
public class EmitterTreeItem {

    private final StringProperty name = new SimpleStringProperty("");
    private final LongProperty count = new SimpleLongProperty(1L);

    public EmitterTreeItem(String nameProperty) {
        this.name.set(nameProperty);
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

}
