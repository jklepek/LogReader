package test;

import app.model.LogEvent;
import app.LogEventRepository;
import app.utils.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {

    @BeforeEach
    void setUp() {
        LogEventRepository.clearRepository();
    }

    @Test
    void parseBufferTest() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n");
        Parser.getInstance().parseBuffer(testString);
        LogEvent logEvent = LogEventRepository.getLogEventList().get(0);
        assertEquals("2018-12-10 12:07:43,330", logEvent.getTimestamp());
        assertEquals("ERROR", logEvent.getLevel());
        assertEquals("[NewConnectionWizard]", logEvent.getEmitter());
        assertEquals("java.lang.InterruptedException", logEvent.getMessage());
        assertEquals("", logEvent.getStackTrace());
    }

    @Test
    void parseBufferWithStackTraceTest() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43,330 ERROR [NewConnectionWizard] java.lang.InterruptedException\r\n\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)\n");
        Parser.getInstance().parseBuffer(testString);
        LogEvent logEvent = LogEventRepository.getLogEventList().get(0);
        assertEquals("2018-12-10 12:07:43,330", logEvent.getTimestamp());
        assertEquals("ERROR", logEvent.getLevel());
        assertEquals("[NewConnectionWizard]", logEvent.getEmitter());
        assertEquals("java.lang.InterruptedException", logEvent.getMessage());
        assertEquals("\tat java.util.concurrent.FutureTask.report(FutureTask.java:122)\n", logEvent.getStackTrace());
    }

    @Test
    void parseWrongFormat() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43 [ERROR] NewConnectionWizard ");
        Parser.getInstance().parseBuffer(testString);
        assertEquals(0, LogEventRepository.getLogEventList().size());
    }

    @Test
    void parseWrongFormatWithMatches() {
        StringBuilder testString = new StringBuilder("2018-12-10 12:07:43,330 [ERROR] NewConnectionWizard java.lang.InterruptedException\r\n");
        Parser.getInstance().parseBuffer(testString);
        LogEvent logEvent = LogEventRepository.getLogEventList().get(0);
        assertEquals(1, LogEventRepository.getLogEventList().size());
        assertEquals("", logEvent.getLevel());
        assertEquals("[ERROR]", logEvent.getEmitter());
    }

}