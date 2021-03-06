/*
 * Created 2019. Open source.
 * @author jklepek
 */

package app.tools;

import app.core.LogEventRepository;
import app.core.Parser;
import app.model.LogEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static app.model.PatternKeywords.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {

    private static final String repoName = "test";
    private static Parser parser;

    @BeforeAll
    static void initTests() {
        LogEventRepository.createNewRepository(repoName);
        parser = new Parser("%d{yyyy-MM-dd' 'HH:mm:ss,SSS} %p %[t] %m%n");
    }

    @BeforeEach
    void setUp() {
        LogEventRepository.clearRepository(repoName);
    }

    @Test
    void parseBufferTest() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException" + System.lineSeparator());
        parser.parseBuffer(testString, repoName);
        LogEvent logEvent = LogEventRepository.getLogEventList(repoName).get(0);
        assertEquals("2018-12-10 12:07:43,330", logEvent.getProperty(TIMESTAMP.name()));
        assertEquals("ERROR", logEvent.getProperty(LEVEL.name()));
        assertEquals("[NewConnectionWizard]", logEvent.getProperty(THREAD.name()));
        assertEquals("java.lang.InterruptedException" + System.lineSeparator(), logEvent.getProperty(MESSAGE.name()));
    }

    @Test
    void parseBufferWithStackTraceTest() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43,330 ERROR [[NewConnectionWizard] - 1] java.lang.InterruptedException" + System.lineSeparator() + "\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)" + System.lineSeparator());
        parser.parseBuffer(testString, repoName);
        LogEvent logEvent = LogEventRepository.getLogEventList(repoName).get(0);
        assertEquals("2018-12-10 12:07:43,330", logEvent.getProperty(TIMESTAMP.name()));
        assertEquals("ERROR", logEvent.getProperty(LEVEL.name()));
        assertEquals("[[NewConnectionWizard] - 1]", logEvent.getProperty(THREAD.name()));
        assertEquals("java.lang.InterruptedException" + System.lineSeparator() + "\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)" + System.lineSeparator(), logEvent.getProperty(MESSAGE.name()));
    }

    @Test
    void parseWrongFormat() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43 [ERROR] NewConnectionWizard ");
        parser.parseBuffer(testString, repoName);
        assertEquals(0, LogEventRepository.getLogEventList(repoName).size());
    }

}