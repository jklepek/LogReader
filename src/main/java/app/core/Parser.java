package app.core;

import app.model.LogEvent;
import app.notifications.EventNotification;
import app.notifications.EventNotifier;
import app.notifications.NotificationListener;
import app.notifications.NotificationType;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static app.model.PatternKeywords.MESSAGE;
import static app.model.PatternKeywords.TIMESTAMP;

public class Parser implements EventNotifier {

    public static final Logger LOG = LoggerFactory.getLogger(Parser.class);

    private final List<String> keywords;
    private final List<NotificationListener> listeners = new ArrayList<>();
    private final Pattern timestampPattern;

    public Parser(String pattern) {
        PatternBuilder patternBuilder = new PatternBuilder(pattern);
        this.timestampPattern = patternBuilder.getTimestampPattern();
        this.keywords = patternBuilder.getKeywords();
    }

    @Override
    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    /**
     * @return list of keywords used in the currently selected pattern
     */
    public List<String> getKeywords() {
        return this.keywords;
    }

    /**
     * Parsing line from logfile and creating a new LogEvent object
     *
     * @param line one line from the log4j log file
     * @return new LogEvent
     */
    public LogEvent parse(String line) {
        Matcher matcher = timestampPattern.matcher(line);
        LogEvent logEvent = new LogEvent();
        try {
            for (String keyword : keywords) {
                if (keyword.equals(TIMESTAMP.name())) {
                    if (matcher.find()) {
                        String timestamp = line.substring(matcher.start(), matcher.end());
                        logEvent.setProperty(keyword, timestamp);
                        line = line.replace(timestamp, "").replaceFirst("^\\s++", "");
                    }
                } else if (keyword.equals(MESSAGE.name())) {
                    logEvent.setProperty(keyword, line);
                    break;
                } else {
                    String value;
                    if (line.startsWith("[")) {
                        int rightBracketIndex = getEndingBracketIndex(line);
                        value = line.substring(0, rightBracketIndex + 1);
                    } else {
                        value = line.substring(0, line.indexOf(" "));
                    }
                    line = line.replace(value, "").replaceFirst("^\\s++", "");
                    logEvent.setProperty(keyword, value);
                }
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            listeners.forEach(listener -> listener.fireNotification(
                    new EventNotification("Parsing error", "An error occurred while parsing.", NotificationType.ERROR)));
        }
        return logEvent;
    }

    private int getEndingBracketIndex(String line) {
        int leftBracketCount = 0;
        int rightBracketCount = 0;
        Matcher matcher = Pattern.compile("[\\[\\]]").matcher(line);
        while (matcher.find()) {
            int i = matcher.start();
            if (line.charAt(i) == '[') {
                leftBracketCount++;
            } else if (line.charAt(i) == ']') {
                rightBracketCount++;
            }
            if (leftBracketCount == rightBracketCount) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Reads log file into a StringBuilder
     *
     * @param log logfile to be parsed
     * @return StringBuilder with whole file content in it
     */
    private StringBuilder readFileToBuffer(File log) {
        StringBuilder buffer = new StringBuilder();
        if (log != null) {
            String currentLine;
            try (FileReader fileReader = new FileReader(log);
                 BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                while ((currentLine = bufferedReader.readLine()) != null) {
                    buffer.append(currentLine).append(System.lineSeparator());
                }
            } catch (IOException e) {
                LOG.error("Error while reading file {}", log, e);
                listeners.forEach(listener ->
                        listener.fireNotification(
                                new EventNotification("Error while reading file", e.getMessage(), NotificationType.ERROR)));
            }
        }
        return buffer;
    }

    /**
     * Parses the content of the log file and stores the LogEvent
     * objects in repository, where the filepath
     * defines the repository
     *
     * @param buffer           content of the log file
     * @param absoluteFilePath path of the file
     */
    public void parseBuffer(StringBuilder buffer, String absoluteFilePath) {
        Matcher matcher = timestampPattern.matcher(buffer);
        ObservableList<LogEvent> logEventList = LogEventRepository.getLogEventList(absoluteFilePath);
        boolean parsed = false;
        boolean first = true;
        int start;
        int end = 0;
        while (!parsed) {
            if (matcher.find()) {
                start = matcher.start();
                // here the matcher is already one match ahead, so we have to parse from the previous match
                if (!first) {
                    LogEvent previousEvent = parse(buffer.substring(end, start));
                    logEventList.add(previousEvent);
                }
                if (matcher.find()) {
                    first = false;
                    end = matcher.start();
                } else {
                    end = buffer.length();
                    parsed = true;
                }
                LogEvent logEvent = parse(buffer.substring(start, end));
                logEventList.add(logEvent);
            } else {
                parsed = true;
            }
        }
    }

    /**
     * Public method to be called from controller
     *
     * @param file log file to be parsed
     */
    public void getLogEventsFromFile(File file) {
        String absoluteFilePath = file.getAbsolutePath();
        StringBuilder buffer = readFileToBuffer(file);
        parseBuffer(buffer, absoluteFilePath);
    }
}
