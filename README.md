# LogReader
An application for reading log4j log files built on javafx. Log records are color coded according to their severity and can be filtered accordingly.
Application supports multiple opened files.
Each file can be automatically refreshed, if there are new records in it.
When log file is reset, log entries in application are refreshed to mirror the actual state.
Parent directory of each opened file is being watched and when there is a new .log file, notification will pop up.
For convenience, stacktrace can be copied to clipboard with a push of a button.
Settings allow to set initial directory and auto-refresh interval, which is stored in user preferences.
For now the log4j format is hardcoded as: %d{yyyy-MM-dd' 'HH:mm:ss,SSS} %-5level [%logger{0}] %msg%n


TO DO:

  implement custom log formats