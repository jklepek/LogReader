/*
 * Created 2019. Open source.
 * @author jklepek
 */

package app.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PreferencesControllerTest {

    final PreferencesController preferences = PreferencesController.getInstance();

    @Test
    void autoRefreshIntervalTest() {
        preferences.setAutoRefreshInterval(1000);
        assertEquals(1000, preferences.getAutoRefreshInterval());
        preferences.setAutoRefreshInterval(2000);
        assertEquals(2000, preferences.getAutoRefreshInterval());
    }

    @Test
    void preferredDirTest() {
        preferences.setInitialDir("C:\\Users\\jklepek\\Downloads\\Other");
        assertEquals("C:\\Users\\jklepek\\Downloads\\Other", preferences.getInitialDir());
        preferences.setInitialDir("C:\\Program Files\\SmartBear\\SoapUI-5.4.0\\bin");
        assertEquals("C:\\Program Files\\SmartBear\\SoapUI-5.4.0\\bin", preferences.getInitialDir());
    }
}