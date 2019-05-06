package app.utils;

import app.model.EmitterTreeItem;
import app.model.LogEvent;
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

import static app.model.LogKeywords.valueOf;

public class Parser {

    private static String timestampPattern;
    private static Map<Integer, String> pattern;
    private static String dateTimeRegex;

    public Parser() {
        pattern = getKeywordsFromPattern();
        dateTimeRegex = getTimestampRegex(timestampPattern);
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
        if (!pattern.isEmpty()) {
            for (int i = 0; i < pattern.size(); i++) {
                keywords.add(pattern.get(i).toLowerCase());
            }
        }
        return keywords;
    }

    /**
     * Parsing line from logfile and creating a new LogEvent object
     *
     * @param line one line from the log4j log file
     * @param map  map with the keywords
     * @return new LogEvent
     */
    private LogEvent parse(String line, Map<Integer, String> map) {
        LogEvent.Builder eventBuilder = new LogEvent.Builder();
        Matcher matcher = Pattern.compile(dateTimeRegex).matcher(line);
        for (String value : map.values()) {
            switch (valueOf(value)) {
                case TIMESTAMP:
                    if (matcher.find()) {
                        String timestamp = line.substring(matcher.start(), matcher.end());
                        eventBuilder.timestamp(timestamp);
                        line = line.replace(timestamp, "").replaceFirst("^\\s++", "");
                    }
                    break;
                case LEVEL:
                    String level = line.substring(0, line.indexOf(" "));
                    eventBuilder.level(level);
                    line = line.replace(level, "").replaceFirst("^\\s++", "");
                    break;
                case EMITTER:
                    String emitter = line.substring(0, line.indexOf(" "));
                    eventBuilder.emitter(emitter);
                    line = line.replace(emitter, "").replaceFirst("^\\s++", "");
                    break;
                case MESSAGE:
                    String message = line.substring(0, line.indexOf(System.lineSeparator()));
                    eventBuilder.message(message);
                    String stacktrace = line.substring(line.indexOf(System.lineSeparator())).replaceFirst("^\\s++", "");
                    eventBuilder.stacktrace(stacktrace);
                    break;
                case THREAD:
                    String thread = line.substring(0, line.indexOf(" "));
                    eventBuilder.thread(thread);
                    line = line.replace(thread, "").replaceFirst("^\\s++", "");
                    break;
                case MDC:
                    String mdc = line.substring(0, line.indexOf(" "));
                    eventBuilder.mdc(mdc);
                    line = line.replace(mdc, "").replaceFirst("^\\s++", "");
                    break;
            }
        }
        return eventBuilder.build();
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
     * objects in repository, where the filename
     * defines the repository
     *
     * @param buffer   content of the log file
     * @param fileName name of the file
     */
    public void parseBuffer(StringBuilder buffer, String fileName) {
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
            LogEvent event = parse(line, pattern);
            LogEventRepository.addEvent(fileName, event);
            LogEventRepository.addEmitterTreeItem(fileName, new EmitterTreeItem(event.getEmitter()));
        }
    }

    /**
     * Public method to be called from controller
     *
     * @param file log file to be parsed
     */
    public void getLogEventsFromFile(File file) {
        String fileName = file.getName();
        StringBuilder buffer = readFileToBuffer(file);
        parseBuffer(buffer, fileName);
    }
}
