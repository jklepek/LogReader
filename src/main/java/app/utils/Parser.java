package app.utils;

import app.model.LogEvent;
import app.model.LogKeywords;
import app.utils.notifications.EventNotification;
import app.utils.notifications.NotificationService;
import app.utils.notifications.NotificationType;

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

    private static final Parser instance = new Parser();
    private static String timestampPattern;

    private Parser() {
    }

    public static Parser getInstance() {
        return instance;
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
        Map<Integer, String> map = getKeywordsFromPattern();
        if (!map.isEmpty()) {
            for (int i = 0; i < map.size(); i++) {
                keywords.add(map.get(i).toLowerCase());
            }
        }
        return keywords;
    }

    /**
     * Parsing line from logfile and creating a new LogEvent object
     * @param line one line from the log4j log file
     * @param map map with the keywords
     * @param pattern timestamp regex pattern
     * @return new LogEvent
     */
    private LogEvent parse(String line, Map<Integer, String> map, String pattern) {
        String level = "";
        String emitter = "";
        String message = "";
        String timestamp = "";
        String stacktrace = "";
        String thread = "";
        String mdc = "";
        Matcher matcher = Pattern.compile(pattern).matcher(line);
        for (String value : map.values()) {
            switch (value) {
                case LogKeywords.TIMESTAMP:
                    if (matcher.find()) {
                        timestamp = line.substring(matcher.start(), matcher.end());
                        line = line.replace(timestamp, "").replaceFirst("^\\s++", "");
                    }
                    break;
                case LogKeywords.LEVEL:
                    level = line.substring(0, line.indexOf(" "));
                    line = line.replace(level, "").replaceFirst("^\\s++", "");
                    break;
                case LogKeywords.EMITTER:
                    emitter = line.substring(0, line.indexOf(" "));
                    line = line.replace(emitter, "").replaceFirst("^\\s++", "");
                    break;
                case LogKeywords.MESSAGE:
                    message = line.substring(0, line.indexOf(System.lineSeparator()));
                    stacktrace = line.substring(line.indexOf(System.lineSeparator())).replaceFirst("^\\s++", "");
                    break;
                case LogKeywords.THREAD:
                    thread = line.substring(0, line.indexOf(" "));
                    line = line.replace(thread, "").replaceFirst("^\\s++", "");
                    break;
                case LogKeywords.MDC:
                    mdc = line.substring(0, line.indexOf(" "));
                    line = line.replace(mdc, "").replaceFirst("^\\s++", "");
                    break;
            }
        }
        return new LogEvent(timestamp, level, emitter, message, thread, mdc, stacktrace);
    }

    /**
     * Reads log file into a StringBuilder
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
            }
        }
        return buffer;
    }

    /**
     * Parses the content of the log file and stores the LogEvent
     * objects in repository, where the filename
     * defines the repository
     * @param buffer content of the log file
     * @param fileName name of the file
     */
    public void parseBuffer(StringBuilder buffer, String fileName) {
        Map<Integer, String> keywords = getKeywordsFromPattern();
        String dateTimeRegex = getTimestampRegex(timestampPattern);
        Pattern dateTimePattern = Pattern.compile(dateTimeRegex);
        Matcher matcher = dateTimePattern.matcher(buffer);
        List<Integer> lineNumbers = new ArrayList<>();
        while (matcher.find()) {
            lineNumbers.add(matcher.start());
        }
        if (lineNumbers.size() == 0) {
            NotificationService.addNotification(new EventNotification("No events", "No events were parsed from file", NotificationType.WARNING));
        }
        for (int i = 0; i < lineNumbers.size(); i++) {
            if (i + 1 >= lineNumbers.size()) {
                LogEventRepository.addEvent(fileName, parse(buffer.substring(lineNumbers.get(i)), keywords, dateTimeRegex));
            } else {
                LogEventRepository.addEvent(fileName, parse(buffer.substring(lineNumbers.get(i), lineNumbers.get(i + 1)), keywords, dateTimeRegex));
            }
        }
    }

    /**
     * Public method to be called from controller
     * @param file log file to be parsed
     */
    public void getLogEventsFromFile(File file) {
        String fileName = file.getName();
        StringBuilder buffer = readFileToBuffer(file);
        parseBuffer(buffer, fileName);
    }
}
