package app.model.ui;

import app.model.LogEvent;
import app.model.PatternKeywords;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogEventPropertyFactory implements Callback<TableColumn.CellDataFeatures<LogEvent, String>, ObservableValue<String>> {

    public static final Logger LOG = LoggerFactory.getLogger(LogEventPropertyFactory.class);

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
            if (property.equals(PatternKeywords.MESSAGE.name())) {
                String message = value.substring(0, value.indexOf(System.lineSeparator()));
                return new ReadOnlyObjectWrapper<>(message);
            }
            return new ReadOnlyObjectWrapper<>(value);
        } catch (Exception e) {
            LOG.error("No such property: {}", property, e);
        }
        return null;
    }
}
