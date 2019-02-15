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
            switch (item.getLevel()) {
                case LogLevel.ERROR:
                    setStyle("-fx-background-color: indianred;");
                    break;
                case LogLevel.INFO:
                    setStyle("-fx-background-color: cornflowerblue;");
                    break;
                case LogLevel.WARN:
                    setStyle("-fx-background-color: orange;");
                    break;
                case LogLevel.DEBUG:
                    setStyle("-fx-background-color: lightblue;");
                    break;
                case LogLevel.TRACE:
                    setStyle("-fx-background-color: ivory");
                    break;
                case LogLevel.FATAL:
                    setStyle("-fx-background-color: firebrick");
                    break;
                default:
                    setStyle("-fx-backgound-color: white;");
                    break;
            }
        }
    }
}
