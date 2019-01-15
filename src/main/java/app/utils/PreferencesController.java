package app.utils;

import app.utils.notifications.EventNotification;
import app.utils.notifications.NotificationService;
import app.utils.notifications.NotificationType;

import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PreferencesController {

    private static final PreferencesController INSTANCE = new PreferencesController();
    private final String autoRefreshInterval = "REFRESH_INTERVAL";
    private final String initialDir = "PREFERRED_DIR";
    private final String watchForDirChanges = "WATCH_FOR_DIR_CHANGES";
    private final String logPattern = "CURRENT_LOG_PATTERN";
    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getName());
    private final Preferences logPatterns = preferences.node("LogPatterns");

    public static PreferencesController getInstance() {
        return INSTANCE;
    }

    public boolean getWatchForDirChanges() {
        return preferences.getBoolean(watchForDirChanges, true);
    }

    public void setWatchForDirChanges(boolean watch) {
        preferences.putBoolean(watchForDirChanges, watch);
    }

    public long getAutoRefreshInterval() {
        return preferences.getLong(autoRefreshInterval, 1000);
    }

    public void setAutoRefreshInterval(long value) {
        preferences.putLong(autoRefreshInterval, value);
    }

    public String getInitialDir() {
        return preferences.get(initialDir, System.getProperty("user.dir"));
    }

    public void setInitialDir(String dir) {
        preferences.put(initialDir, dir);
    }

    public void addLogPattern(String name, String pattern) {
        if (!getPatterns().contains(name)) {
            logPatterns.put(name, pattern);
        }
    }

    public Optional<Map<String, String>> getLogPatterns() {
        Map<String, String> map = new HashMap<>();
        if (getPatterns().isEmpty()) {
            NotificationService.addNotification(new EventNotification("No pattern found", "There is no pattern defined", NotificationType.ERROR));
        }
        for (Object pattern : getPatterns()) {
            map.put(pattern.toString(), logPatterns.get(pattern.toString(), ""));
        }
        return Optional.of(map);
    }

    private List getPatterns() {
        try {
            return Arrays.asList(logPatterns.keys());
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    public void removePattern(String name) {
        logPatterns.remove(name);
    }

    public String getLogPattern() {
        return preferences.get(logPattern,"");
    }

    public void setLogPattern(String pattern) {
        preferences.put(logPattern, pattern);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }
}
