/*
 * Created 2019. Open source.
 * @author jklepek
 */

package app.utils;

import app.model.LogEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {

    private static final String repoName = "test";
    private static Parser parser;

    @BeforeAll
    static void initTests() {
        PreferencesRepository.loadPreferences();
        PreferencesRepository.setCurrentLogPattern("%D{yyyy-MM-dd' 'HH:mm:ss,SSS} %LEVEL %THREAD %MESSAGE");
        LogEventRepository.newRepository(repoName);
        parser = new Parser();
    }

    @BeforeEach
    void setUp() {
        LogEventRepository.clearRepository(repoName);
    }

    @Test
    void parseBufferTest() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n");
        parser.parseBuffer(testString, repoName);
        LogEvent logEvent = LogEventRepository.getLogEventList(repoName).get(0);
        assertEquals("2018-12-10 12:07:43,330", logEvent.getTimestamp());
        assertEquals("ERROR", logEvent.getLevel());
        assertEquals("[NewConnectionWizard]", logEvent.getThread());
        assertEquals("java.lang.InterruptedException", logEvent.getMessage());
        assertEquals("", logEvent.getStacktrace());
    }

    @Test
    void parseBufferWithStackTraceTest() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43,330 ERROR [[NewConnectionWizard] - 1] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)\n");
        parser.parseBuffer(testString, repoName);
        LogEvent logEvent = LogEventRepository.getLogEventList(repoName).get(0);
        assertEquals("2018-12-10 12:07:43,330", logEvent.getTimestamp());
        assertEquals("ERROR", logEvent.getLevel());
        assertEquals("[[NewConnectionWizard] - 1]", logEvent.getThread());
        assertEquals("java.lang.InterruptedException", logEvent.getMessage());
        assertEquals("at java.util.concurrent.FutureTask.report(FutureTask.java:122)\n", logEvent.getStacktrace());
    }

    @Test
    void parseWrongFormat() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43 [ERROR] NewConnectionWizard ");
        parser.parseBuffer(testString, repoName);
        assertEquals(0, LogEventRepository.getLogEventList(repoName).size());
    }

}