package app.preferences;

import app.model.LogPattern;
import app.notifications.EventNotification;
import app.notifications.EventNotifier;
import app.notifications.NotificationListener;
import app.notifications.NotificationType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Controller that loads and stores user preferences
 * to system
 * These methods are called at the start of the application
 * and at the closing of the application
 */
public class PreferencesController implements EventNotifier {

    private PreferencesController() {
    }

    public static PreferencesController getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static final String autoRefreshInterval = "REFRESH_INTERVAL";
    private static final String initialDir = "PREFERRED_DIR";
    private static final String watchForDirChanges = "WATCH_FOR_DIR_CHANGES";
    private static final String currentLogPattern = "CURRENT_LOG_PATTERN";
    private final Preferences preferences = Preferences.userRoot().node(this.getClass().getName());
    private final Preferences logPatterns = preferences.node("LogPatterns");
    private final List<NotificationListener> listeners = new ArrayList<>();

    private static class LazyHolder {
        static final PreferencesController INSTANCE = new PreferencesController();
    }

    @Override
    public void addListener(NotificationListener listener) {
        listeners.add(listener);
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

    public List<LogPattern> getLogPatterns() {
        List<LogPattern> patterns = new ArrayList<>();
        List<String> patternNames = getPatterns();
        if (patternNames.isEmpty()) {
            listeners.forEach(listener ->
                    listener.fireNotification(
                            new EventNotification("No pattern found", "There is no pattern defined", NotificationType.ERROR)));
        }
        for (String name : patternNames) {
            patterns.add(new LogPattern(name, logPatterns.get(name, "")));
        }
        return patterns;
    }

    private List<String> getPatterns() {
        try {
            return Arrays.asList(logPatterns.keys());
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void removePattern(String name) {
        logPatterns.remove(name);
    }

    public String getCurrentLogPattern() {
        return preferences.get(currentLogPattern, "");
    }

    public void setCurrentLogPattern(String pattern) {
        preferences.put(currentLogPattern, pattern);
    }

}
