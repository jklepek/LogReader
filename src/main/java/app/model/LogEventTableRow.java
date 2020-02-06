/*
 * Created 2019. Open source.
 */

package app.model;

import javafx.scene.control.TableRow;

/**
 * @author JKlepek
 * @project LogReader
 */
public class LogEventTableRow extends TableRow<LogEvent> {

    @Override
    protected void updateItem(LogEvent item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null) {
            switch (LogLevel.valueOf(item.getProperty(PatternKeywords.LEVEL.name()))) {
                case ERROR:
                    setStyle("-fx-background-color: indianred;");
                    break;
                case INFO:
                    setStyle("-fx-background-color: cornflowerblue;");
                    break;
                case WARN:
                    setStyle("-fx-background-color: orange;");
                    break;
                case DEBUG:
                    setStyle("-fx-background-color: lightblue;");
                    break;
                case TRACE:
                    setStyle("-fx-background-color: ivory");
                    break;
                case FATAL:
                    setStyle("-fx-background-color: firebrick");
                    break;
                default:
                    setStyle("-fx-backgound-color: white;");
                    break;
            }
        } else {
            setStyle("-fx-backgound-color: white;");
        }
    }
}
