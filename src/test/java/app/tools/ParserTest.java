/*
 * Created 2019. Open source.
 * @author jklepek
 */

package app.tools;

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
        PreferencesRepository.setCurrentLogPattern("%D{yyyy-MM-dd' 'HH:mm:ss,SSS} %LEVEL %THREAD %MESSAGE%N");
        LogEventRepository.createNewRepository(repoName);
        parser = new Parser();
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
        assertEquals("2018-12-10 12:07:43,330", logEvent.getProperty("TIMESTAMP"));
        assertEquals("ERROR", logEvent.getProperty("LEVEL"));
        assertEquals("[NewConnectionWizard]", logEvent.getProperty("THREAD"));
        assertEquals("java.lang.InterruptedException", logEvent.getProperty("MESSAGE"));
        assertEquals("", logEvent.getProperty("STACKTRACE"));
    }

    @Test
    void parseBufferWithStackTraceTest() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43,330 ERROR [[NewConnectionWizard] - 1] java.lang.InterruptedException" + System.lineSeparator() + "\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)" + System.lineSeparator());
        parser.parseBuffer(testString, repoName);
        LogEvent logEvent = LogEventRepository.getLogEventList(repoName).get(0);
        assertEquals("2018-12-10 12:07:43,330", logEvent.getProperty("TIMESTAMP"));
        assertEquals("ERROR", logEvent.getProperty("LEVEL"));
        assertEquals("[[NewConnectionWizard] - 1]", logEvent.getProperty("THREAD"));
        assertEquals("java.lang.InterruptedException", logEvent.getProperty("MESSAGE"));
        assertEquals("at java.util.concurrent.FutureTask.report(FutureTask.java:122)" + System.lineSeparator(), logEvent.getProperty("STACKTRACE"));
    }

    @Test
    void parseWrongFormat() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43 [ERROR] NewConnectionWizard ");
        parser.parseBuffer(testString, repoName);
        assertEquals(0, LogEventRepository.getLogEventList(repoName).size());
    }

}