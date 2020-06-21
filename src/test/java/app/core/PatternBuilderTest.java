package app.core;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.ls.LSOutput;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatternBuilderTest {

    private static PatternBuilder builder;

    @BeforeAll
    static void setup() {
        builder = new PatternBuilder("%d{yyyy-MM-dd' 'HH:mm:ss,SSS} [%t] %p %c %x - %m%n");
    }

    @Test
    void getKeywordsFromPatternTest() {
        List<String> keywords = builder.getKeywords();
        keywords.forEach(System.out::println);
    }

    void getTimestampRegexTest() {

    }

}