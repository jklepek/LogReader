package app.utils;

import app.model.LogEvent;
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

    private LogEvent parse(String line) {
        String timestamp = parseDate(line).map(String::toString).orElse("");
        String level = parseLevel(line).map(String::toString).orElse("");
        String emitter = parseEmitter(line).map(String::toString).orElse("");
        String message = parseMessage(line).map(String::toString).orElse("");
        String stackTrace = parseStackTrace(line).map(String::toString).orElse("");
        return new LogEvent(timestamp, level, emitter, message, stackTrace);
    }

    private Optional<String> parseDate(String line) {
        String dateTimeRegex = "^([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3})";
        Pattern dateTimePattern = Pattern.compile(dateTimeRegex);
        Matcher matcher = dateTimePattern.matcher(line);
        if (matcher.find()) {
            return Optional.of(line.substring(matcher.start(), matcher.end()).trim());
        }
        return Optional.empty();
    }

    private Optional<String> parseLevel(String line) {
        String levelRegex = "([A-Z]){2,} ";
        Pattern levelPattern = Pattern.compile(levelRegex);
        Matcher matcher = levelPattern.matcher(line);
        if (matcher.find()) {
            return Optional.of(line.substring(matcher.start(), matcher.end()).trim());
        }
        return Optional.empty();
    }

    private Optional<String> parseEmitter(String line) {
        String emitterRegex = "(\\[.*?\\])";
        Pattern threadPattern = Pattern.compile(emitterRegex);
        Matcher matcher = threadPattern.matcher(line);
        if (matcher.find()) {
            return Optional.of(line.substring(matcher.start(), matcher.end()).trim());
        }
        return Optional.empty();
    }

    private Optional<String> parseMessage(String line) {
        String messageRegex = "\\] .*";
        Pattern messagePattern = Pattern.compile(messageRegex);
        Matcher matcher = messagePattern.matcher(line);
        if (matcher.find()) {
            return Optional.of(line.substring(matcher.start() + 2, line.indexOf("\r\n")));
        }
        return Optional.empty();
    }

    private Optional<String> parseStackTrace(String line) {
        int index = line.indexOf("\r\n");
        return Optional.of(line.substring(index + 2));
    }

    private StringBuilder readFileToBuffer(File log) {
        StringBuilder buffer = new StringBuilder();
        if (log != null) {
            String currentLine;
            try (FileReader fileReader = new FileReader(log)) {
                try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                    while ((currentLine = bufferedReader.readLine()) != null) {
                        buffer.append(currentLine).append(System.lineSeparator());
                    }
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        return buffer;
    }

    public void parseBuffer(StringBuilder buffer, String fileName) {
        String dateTimeRegex = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}";
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
                LogEventRepository.addEvent(fileName, parse(buffer.substring(lineNumbers.get(i))));
            } else {
                LogEventRepository.addEvent(fileName, parse(buffer.substring(lineNumbers.get(i), lineNumbers.get(i + 1))));
            }
        }
    }

    public void getLogEventsFromFile(File file) {
        String fileName = file.getName();
        StringBuilder buffer = readFileToBuffer(file);
        parseBuffer(buffer, fileName);
    }


    private class toImplement {

        private String getTimestampRegex(String input) {
            input = input.replaceAll("'", "");
            return input.replaceAll("[yYmMdDhHsS]", "\\\\d");
        }

        private Map<Integer, String> getKeywords(String input) {
            Pattern p = Pattern.compile("%.*?&");
            Matcher matcher = p.matcher(input);
            Map<Integer, String> map = new TreeMap<>();
            int position = 0;
            while (matcher.find()) {
                map.put(position, input.substring(matcher.start() + 1, matcher.end() - 1));
                position++;
            }
            return map;
        }

        private void parse(String line, Map<Integer, String> map, String pattern) {
            String level = "";
            String emitter = "";
            String message = "";
            String timestamp = "";
            String stacktrace = "";
            String thread = "";
            Matcher matcher = Pattern.compile(pattern).matcher(line);
            for (String value : map.values()) {
                switch (value) {
                    case "LEVEL":
                        level = line.substring(0, line.indexOf(" "));
                        line = line.replace(level, "").trim();
                        break;
                    case "EMITTER":
                        emitter = line.substring(0, line.indexOf(" "));
                        line = line.replace(emitter, "").trim();
                        break;
                    case "MESSAGE":
                        message = line.substring(0, line.indexOf("\r\n"));
                        stacktrace = line.substring(line.indexOf("\r\n") + 2);
                        break;
                    case "THREAD":
                        thread = line.substring(0, line.indexOf(" "));
                        line = line.replace(thread, "").trim();
                    default:
                        if (matcher.find()) {
                            timestamp = line.substring(matcher.start(), matcher.end());
                            line = line.replace(timestamp, "").trim();
                        }
                        break;
                }
            }
            LogEvent logEvent = new LogEvent(timestamp, level, emitter, message, stacktrace);
        }
    }
}
