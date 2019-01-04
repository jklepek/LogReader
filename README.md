# LogReader
An application for reading log4j logfiles built on javafx.

Opened logfile can be tailed, so new events will be added automatically.

Resetting logfile is also registered when auto-refresh is enabled.

Stacktrace can be copied to clipboard.

Different log levels are color coded and can be filtered.

Settings allow to set initial directory and auto-refresh interval, which is stored in user preferences.

For now the log4j format is hardcoded as: %d{yyyy-MM-dd' 'HH:mm:ss,SSS} %-5level [%logger{0}] %msg%n

TO DO:
  implement custom log formats
  
  multiple opened log files
