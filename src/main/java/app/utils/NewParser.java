package app.utils;

import app.model.LogEvent;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewParser {

    private static final String parseFormat = "%yyyy-MM-dd' 'HH:mm:ss,SSS& %LEVEL& %EMITTER& %MESSAGE&";
    private static String format = "yyyy-MM-dd' 'HH:mm:ss,SSS";
    private static String string = "2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)\n";

    public static void main(String[] args) {

        System.out.println(getTimestampRegex(format));
        getKeywords(parseFormat);
        parse(string, getKeywords(parseFormat), getTimestampRegex(format));
    }

    private static String getTimestampRegex(String input) {
        input = input.replaceAll("'", "");
        return input.replaceAll("[yYmMdDhHsS]", "\\\\d");
    }

    private static Map<Integer, String> getKeywords(String input) {
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

    private static void parse(String line, Map<Integer, String> map, String pattern) {
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
                    line = line.replace(emitter, "").trim();
                default:
                    if (matcher.find()) {
                        timestamp = line.substring(matcher.start(), matcher.end());
                        line = line.replace(timestamp, "").trim();
                    }
                    break;
            }
        }
        LogEvent logEvent = new LogEvent(timestamp, level, emitter, message, stacktrace);
        System.out.println(logEvent.toString());
    }


}
