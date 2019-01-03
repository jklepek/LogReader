package app.utils;

import app.model.LogEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public void parseBuffer(StringBuilder buffer) {
        String dateTimeRegex = "[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}";
        Pattern dateTimePattern = Pattern.compile(dateTimeRegex);
        Matcher matcher = dateTimePattern.matcher(buffer);
        List<Integer> lineNumbers = new ArrayList<>();
        while (matcher.find()) {
            lineNumbers.add(matcher.start());
        }
        for (int i = 0; i < lineNumbers.size(); i++) {
            if (i + 1 >= lineNumbers.size()) {
                LogEventRepository.addEvent(parse(buffer.substring(lineNumbers.get(i))));
            } else {
                LogEventRepository.addEvent(parse(buffer.substring(lineNumbers.get(i), lineNumbers.get(i + 1))));
            }
        }
    }

    public void getLogEventsFromFile(File file) {
        StringBuilder buffer = readFileToBuffer(file);
        parseBuffer(buffer);
    }
}
