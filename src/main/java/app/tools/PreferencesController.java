package app.tools;

import app.tools.notifications.EventNotification;
import app.tools.notifications.NotificationService;
import app.tools.notifications.NotificationType;

import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Controller that loads and stores user preferences
 * to system
 * These methods are called at the start of the application
 * and at the closing of the application
 */
public class PreferencesController {

    private PreferencesController() {
    }

    public static PreferencesController getInstance() {
        return LazyHolder.INSTANCE;
    }

    private final String autoRefreshInterval = "REFRESH_INTERVAL";
    private final String initialDir = "PREFERRED_DIR";
    private final String watchForDirChanges = "WATCH_FOR_DIR_CHANGES";
    private final String logPattern = "CURRENT_LOG_PATTERN";
    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getName());
    private final Preferences logPatterns = preferences.node("LogPatterns");

    private static class LazyHolder {
        static final PreferencesController INSTANCE = new PreferencesController();
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
        logPatterns.put(name, pattern);
    }

    public Map<String, String> getLogPatterns() {
        Map<String, String> map = new HashMap<>();
        List<String> patterns = getPatterns();
        if (patterns.isEmpty()) {
            NotificationService.addNotification(new EventNotification("No pattern found", "There is no pattern defined", NotificationType.ERROR));
        }
        for (String pattern : patterns) {
            map.put(pattern, logPatterns.get(pattern, ""));
        }
        return map;
    }

    private List<String> getPatterns() {
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
        return preferences.get(logPattern, "");
    }

    public void setLogPattern(String pattern) {
        preferences.put(logPattern, pattern);
    }
}