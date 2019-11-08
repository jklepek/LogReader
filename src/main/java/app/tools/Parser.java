package app.tools;

import app.model.EmitterTreeItem;
import app.model.LogEvent;
import app.tools.notifications.EventNotification;
import app.tools.notifications.NotificationService;
import app.tools.notifications.NotificationType;

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

public class Parser {

    private String timestampPattern;
    private Map<Integer, String> keywords;
    private String dateTimeRegex;
    private String delimiter;

    public Parser() {
        keywords = getKeywordsFromPattern();
        dateTimeRegex = getTimestampRegex(timestampPattern);
        delimiter = PreferencesRepository.getDelimiter();
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
    private Map<Integer, String> getKeywordsFromPattern() {
        Map<Integer, String> map = new TreeMap<>();
        String pattern = PreferencesRepository.getCurrentLogPattern();
        if (!pattern.equals("")) {
            String[] keywords = pattern.split("%");
            int count = 0;
            for (String word : keywords) {
                if (!word.isEmpty()) {
                    if (word.toUpperCase().startsWith("D")) {
                        timestampPattern = word.substring(word.indexOf("{") + 1, word.indexOf("}"));
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
     * @param line     one line from the log4j log file
     * @param keywords map with the keywords
     * @return new LogEvent
     */
    private LogEvent parse(String line, Map<Integer, String> keywords) {
        Matcher matcher = Pattern.compile(dateTimeRegex).matcher(line);
        LogEvent logEvent = new LogEvent();
        for (int i = 0; i < keywords.size(); i++) {
            String keyword = keywords.get(i);
            if (keyword.equalsIgnoreCase("TIMESTAMP")) {
                if (matcher.find()) {
                    String timestamp = line.substring(matcher.start(), matcher.end());
                    logEvent.setProperty(keyword, timestamp);
                    line = line.replace(timestamp, "").replaceFirst("^\\s++", "");
                }
            } else if (i == keywords.size() - 2) {
                String message = line.substring(0, line.indexOf(System.lineSeparator()));
                logEvent.setProperty(keyword, message);
                line = line.replace(message, "").replaceFirst("^\\s++", "");
                logEvent.setProperty("STACKTRACE", line);
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
                NotificationService.
                        addNotification(
                                new EventNotification("Error while reading file", e.getMessage(), NotificationType.ERROR));
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
            NotificationService.addNotification(new EventNotification("No events", "No events were parsed from file", NotificationType.WARNING));
        }
        String line;
        for (int i = 0; i < lineNumbers.size(); i++) {
            if (i + 1 >= lineNumbers.size()) {
                line = buffer.substring(lineNumbers.get(i));
            } else {
                line = buffer.substring(lineNumbers.get(i), lineNumbers.get(i + 1));
            }
            LogEvent event = parse(line, keywords);
            LogEventRepository.addEvent(absoluteFilePath, event);
            LogEventRepository.addEmitterTreeItem(absoluteFilePath, new EmitterTreeItem(event.getProperty("EMITTER")));
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
