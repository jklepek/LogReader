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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final Parser instance = new Parser();

    private Parser() {
    }

    public static Parser getInstance() {
        return instance;
    }


    private String getTimestampRegex() {
        String pattern = getKeywordsFromPattern().get(0);
        pattern = pattern.replaceAll("'", "");
        return pattern.replaceAll("[yYmMdDhHsS]", "\\\\d");
    }

    private Map<Integer, String> getKeywordsFromPattern() {
        Map<Integer, String> map = new TreeMap<>();
        String pattern = PreferencesController.getInstance().getLogPattern();
        if (!pattern.equals("")) {
            List<String> keywords = Arrays.asList(pattern.split("%"));
            int count = 0;
            for (String word : keywords) {
                if (!word.isEmpty()) {
                    map.put(count, word.trim());
                    count++;
                }
            }
        }
        return map;
    }

    public List<String> getKeywords() {
        List<String> keywords = new ArrayList<>();
        Map<Integer, String> map = getKeywordsFromPattern();
        for (int i = 0; i < map.size(); i++) {
            if (i == 0) {
                keywords.add("timestamp");
            } else {
                keywords.add(map.get(i).toLowerCase());
            }
        }
        return keywords;
    }

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
                case LogKeywords.LEVEL:
                    level = line.substring(0, line.indexOf(" "));
                    line = line.replace(level, "").replaceFirst("^\\s++", "");
                    break;
                case LogKeywords.EMITTER:
                    emitter = line.substring(0, line.indexOf(" "));
                    line = line.replace(emitter, "").replaceFirst("^\\s++", "");
                    break;
                case LogKeywords.MESSAGE:
                    message = line.substring(0, line.indexOf("\r\n"));
                    stacktrace = line.substring(line.indexOf("\r\n") + 2);
                    break;
                case LogKeywords.THREAD:
                    thread = line.substring(0, line.indexOf(" "));
                    line = line.replace(thread, "").replaceFirst("^\\s++", "");
                    break;
                case LogKeywords.MDC:
                    mdc = line.substring(0, line.indexOf(" "));
                    line = line.replace(mdc, "").replaceFirst("^\\s++", "");
                    break;
                default:
                    if (matcher.find()) {
                        timestamp = line.substring(matcher.start(), matcher.end());
                        line = line.replace(timestamp, "").replaceFirst("^\\s++", "");
                    }
                    break;
            }
        }
        return new LogEvent(timestamp, level, emitter, message, thread, mdc, stacktrace);
    }

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

    public void parseBuffer(StringBuilder buffer, String fileName) {
        String dateTimeRegex = getTimestampRegex();
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
                LogEventRepository.addEvent(fileName, parse(buffer.substring(lineNumbers.get(i)), getKeywordsFromPattern(), dateTimeRegex));
            } else {
                LogEventRepository.addEvent(fileName, parse(buffer.substring(lineNumbers.get(i), lineNumbers.get(i + 1)), getKeywordsFromPattern(), dateTimeRegex));
            }
        }
    }

    public void getLogEventsFromFile(File file) {
        String fileName = file.getName();
        StringBuilder buffer = readFileToBuffer(file);
        parseBuffer(buffer, fileName);
    }
}
