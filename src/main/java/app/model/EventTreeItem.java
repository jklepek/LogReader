package app.model;

import javafx.scene.control.TreeItem;

public class EventTreeItem extends TreeItem<EventPropertyCounter> {

    private final EventPropertyCounter eventPropertyCounter;

    public EventTreeItem(EventPropertyCounter value) {
        super(value);
        this.eventPropertyCounter = value;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EventTreeItem) {
            return ((EventTreeItem) o).getValue().getName().equals(eventPropertyCounter.getName());
        }
        return false;
    }
}
