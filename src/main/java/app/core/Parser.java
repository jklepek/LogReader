package app.core;

import app.model.LogEvent;
import app.notifications.EventNotification;
import app.notifications.EventNotifier;
import app.notifications.NotificationListener;
import app.notifications.NotificationType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static app.model.PatternKeywords.*;

public class Parser implements EventNotifier {

    private String timestampStringPattern;
    private Map<Integer, String> keywords;
    private String dateTimeRegex;
    private String delimiter;
    private List<NotificationListener> listeners = new ArrayList<>();
    private Pattern timestampPattern;

    public Parser(String pattern, String delimiter) {
        keywords = getKeywordsFromPattern(pattern);
        dateTimeRegex = getTimestampRegex(timestampStringPattern);
        timestampPattern = Pattern.compile(dateTimeRegex);
        this.delimiter = delimiter;
    }

    @Override
    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    /**
     * @param pattern string form of a timestamp pattern e.g. yyyy-MM-dd' 'HH:mm:SSS,S
     * @return timestamp regex
     */
    private String getTimestampRegex(String pattern) {
        pattern = pattern.replaceAll("'", "");
        return pattern.replaceAll("[yYmMdDhHsS]", "\\\\d");
    }

    /**
     * Takes stored pattern and saves individual keywords in a map,
     * where the key is the order of the keyword
     *
     * @return map
     */
    private Map<Integer, String> getKeywordsFromPattern(String pattern) {
        Map<Integer, String> map = new TreeMap<>();
        if (!pattern.equals("")) {
            String[] keywords = pattern.split("%");
            int count = 0;
            for (String word : keywords) {
                if (!word.isEmpty()) {
                    if (word.toUpperCase().startsWith("D{")) {
                        timestampStringPattern = word.substring(word.indexOf("{") + 1, word.indexOf("}"));
                        word = "TIMESTAMP";
                    } else if (word.equalsIgnoreCase("N")) {
                        word = "STACKTRACE";
                    }
                    map.put(count, word.toUpperCase().trim());
                    count++;
                }
            }
        }
        return map;
    }


    /**
     * @return list of keywords used in the currently selected pattern
     */
    public List<String> getKeywords() {
        List<String> keywords = new ArrayList<>();
        if (!this.keywords.isEmpty()) {
            for (int i = 0; i < this.keywords.size(); i++) {
                keywords.add(this.keywords.get(i));
            }
        }
        return keywords;
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
        for (int i = 0; i < keywords.size(); i++) {
            String keyword = keywords.get(i);
            if (keyword.equals(TIMESTAMP.toString())) {
                if (matcher.find()) {
                    String timestamp = line.substring(matcher.start(), matcher.end());
                    logEvent.setProperty(keyword, timestamp);
                    line = line.replace(timestamp, "").replaceFirst("^\\s++", "");
                }
            } else if (keyword.equals(MESSAGE.toString())) {
                String message = line.substring(0, line.indexOf(System.lineSeparator()));
                logEvent.setProperty(keyword, message);
                line = line.replace(message, "").replaceFirst("^\\s++", "");
                logEvent.setProperty(STACKTRACE.name(), line);
                break;
            } else {
                String value;
                if (line.startsWith("[")) {
                    int rightBracketIndex = getEndingBracketIndex(line);
                    value = line.substring(0, rightBracketIndex + 1);
                } else {
                    value = line.substring(0, line.indexOf(delimiter));
                }
                line = line.replace(value, "").replaceFirst("^\\s++", "");
                logEvent.setProperty(keyword, value);
            }
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
                System.out.println(e.getMessage());
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
        Pattern dateTimePattern = Pattern.compile(dateTimeRegex);
        Matcher matcher = dateTimePattern.matcher(buffer);
        List<Integer> lineNumbers = new ArrayList<>();
        while (matcher.find()) {
            lineNumbers.add(matcher.start());
        }
        if (lineNumbers.size() == 0) {
            listeners.forEach(listener ->
                    listener.fireNotification(
                            new EventNotification("No events", "No events were parsed from file", NotificationType.WARNING)));
        }
        String line;
        for (int i = 0; i < lineNumbers.size(); i++) {
            if (i + 1 >= lineNumbers.size()) {
                line = buffer.substring(lineNumbers.get(i));
            } else {
                line = buffer.substring(lineNumbers.get(i), lineNumbers.get(i + 1));
            }
            LogEvent event = parse(line);
            LogEventRepository.addEvent(absoluteFilePath, event);
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
