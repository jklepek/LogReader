package app.core;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PatternBuilderTest {

    private static PatternBuilder builder;

    @BeforeAll
    static void setup() {
        builder = new PatternBuilder("%d{yyyy-MM-dd' 'HH:mm:ss,SSS} [%t] %p %c %x - %m%n");
    }

    @Test
    void getKeywordsFromPatternTest() {
        List<String> keywords = builder.getKeywords();
        assertEquals(keywords, List.of("TIMESTAMP", "THREAD", "LEVEL", "EMITTER", "MDC", "MESSAGE"));
    }

    @Test
    void getTimestampRegexTest() {
        Pattern timestampPattern = builder.getTimestampPattern();
        assertEquals(timestampPattern.toString(), Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d,\\d\\d\\d").toString());
    }

}