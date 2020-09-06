package app.core;

import app.model.PatternKeywords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PatternBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(PatternBuilder.class);

    private final String pattern;
    private final List<String> keywords = new ArrayList<>();
    private String timestampStringPattern;
    private final Pattern regex = Pattern.compile("[a-zA-Z]");

    public PatternBuilder(String pattern) {
        this.pattern = pattern;
        getKeyWordsFromPattern();
    }

    /**
     * timestamp pattern e.g. yyyy-MM-dd' 'HH:mm:SSS,S
     *
     * @return timestamp regex
     */
    private String getTimestampRegex() {
        if (timestampStringPattern != null) {
            try {
                String timestamp = timestampStringPattern.replaceAll("'", "");
                return timestamp.replaceAll("[yYmMdDhHsS]", "\\\\d");
            } catch (NullPointerException e) {
                LOG.error("Incomplete timestamp", e);
            }
        }
        return "";
    }

    /**
     * @return timestamp in the form of <code>Pattern</code>
     */
    public Pattern getTimestampPattern() {
        return Pattern.compile(getTimestampRegex());
    }

    /**
     * Parse keywords from log4j pattern layout
     */
    private void getKeyWordsFromPattern() {
        if (!pattern.isEmpty()) {
            String[] parts = pattern.split("%");
            Stream.of(parts).forEach(part -> {
                if (part.startsWith("d")) {
                    try {
                        timestampStringPattern = part.substring(part.indexOf("{") + 1, part.indexOf("}"));
                    } catch (IndexOutOfBoundsException e) {
                        LOG.error("Could not parse pattern", e);
                    }
                    part = "d";
                }
                Matcher matcher = regex.matcher(part);
                if (matcher.find()) {
                    part = part.substring(matcher.start(), matcher.end());
                }
                Optional<String> keyword = PatternKeywords.getKeyword(part);
                keyword.ifPresent(keywords::add);
            });
        }
    }

    /**
     * @return list of keywords from log4j pattern
     */
    public List<String> getKeywords() {
        return keywords;
    }
}
