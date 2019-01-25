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

    @BeforeAll
    static void initTests() {
        PreferencesRepository.loadPreferences();
        PreferencesRepository.setCurrentLogPattern("%D{yyyy-MM-dd' 'HH:mm:ss,SSS} %LEVEL %EMITTER %MESSAGE");
        LogEventRepository.newRepository(repoName);
    }

    @BeforeEach
    void setUp() {
        LogEventRepository.clearRepository(repoName);
    }

    @Test
    void parseBufferTest() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n");
        Parser.getInstance().parseBuffer(testString, repoName);
        LogEvent logEvent = LogEventRepository.getLogEventList(repoName).get(0);
        assertEquals("2018-12-10 12:07:43,330", logEvent.getTimestamp());
        assertEquals("ERROR", logEvent.getLevel());
        assertEquals("[NewConnectionWizard]", logEvent.getEmitter());
        assertEquals("java.lang.InterruptedException", logEvent.getMessage());
        assertEquals("", logEvent.getStackTrace());
    }

    @Test
    void parseBufferWithStackTraceTest() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)\n");
        Parser.getInstance().parseBuffer(testString, repoName);
        LogEvent logEvent = LogEventRepository.getLogEventList(repoName).get(0);
        assertEquals("2018-12-10 12:07:43,330", logEvent.getTimestamp());
        assertEquals("ERROR", logEvent.getLevel());
        assertEquals("[NewConnectionWizard]", logEvent.getEmitter());
        assertEquals("java.lang.InterruptedException", logEvent.getMessage());
        assertEquals("at java.util.concurrent.FutureTask.report(FutureTask.java:122)\n", logEvent.getStackTrace());
    }

    @Test
    void parseWrongFormat() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43 [ERROR] NewConnectionWizard ");
        Parser.getInstance().parseBuffer(testString, repoName);
        assertEquals(0, LogEventRepository.getLogEventList(repoName).size());
    }

}