package app.utils;

import java.util.prefs.Preferences;

public class PreferencesController {

    private static final PreferencesController instance = new PreferencesController();
    private final String autoRefreshInterval = "REFRESH_INTERVAL";
    private final String initialDir = "PREFERRED_DIR";
    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getName());

    public static PreferencesController getInstance() {
        return instance;
    }

    public long getAutoRefreshInterval() {
        return preferences.getLong(autoRefreshInterval, 1000);
    }

    public void setAutoRefreshInterval(long value) {
        preferences.put(autoRefreshInterval, String.valueOf(value));
    }

    public String getInitialDir() {
        return preferences.get(initialDir, System.getProperty("user.dir"));
    }

    public void setInitialDir(String dir) {
        preferences.put(initialDir, dir);
    }
}
