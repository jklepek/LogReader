package app.model.ui;

import app.model.LogEvent;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class LogEventPropertyFactory implements Callback<TableColumn.CellDataFeatures<LogEvent, String>, ObservableValue<String>> {

    private final String property;

    public LogEventPropertyFactory(String property) {
        this.property = property;
    }

    @Override
    public ObservableValue<String> call(TableColumn.CellDataFeatures<LogEvent, String> param) {
        return getLogEventProperty(param.getValue());
    }

    private ObservableValue<String> getLogEventProperty(LogEvent event) {
        if (event == null || this.property == null || this.property.isEmpty()) {
            return null;
        }
        try {
            String value = event.getProperty(property);
            return new ReadOnlyObjectWrapper<>(value);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("No such property: " + property);
        }
        return null;
    }
}
