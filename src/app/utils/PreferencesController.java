package app.utils;

import java.util.prefs.Preferences;

public class PreferencesController {

    private static PreferencesController instance = new PreferencesController();
    private String autoRefreshInterval = "REFRESH_INTERVAL";
    private String preferredDir = "PREFERRED_DIR";
    private Preferences preferences = Preferences.userRoot().node(this.getClass().getName());

    public static PreferencesController getInstance() {
        return instance;
    }

    public long getAutoRefreshInterval() {
        return preferences.getLong(autoRefreshInterval, 1000);
    }

    public void setAutoRefreshInterval(long value) {
        preferences.put(autoRefreshInterval, String.valueOf(value));
    }

    public String getPreferedDir() {
        return preferences.get(preferredDir, System.getProperty("user.dir"));
    }

    public void setPreferedDir(String dir) {
        preferences.put(preferredDir, dir);
    }
}
