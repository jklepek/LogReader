/*
 * Created 2019. Open source.
 */

package app.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author JKlepek
 * @project LogReader
 *
 * Runtime repository so the user can change preferences
 * without the application immediately storing or loading them from system register
 */
public class PreferencesRepository {

    private static final PreferencesController preferences = PreferencesController.getInstance();
    private static long AUTO_REFRESH_INTERVAL;
    private static String INITIAL_DIRECTORY;
    private static boolean WATCH_DIR_FOR_CHANGES;
    private static String CURRENT_LOG_PATTERN;
    private static Map<String, String> ALL_LOG_PATTERNS;
    private static List<String> PATTERNS_TO_DELETE = new ArrayList<>();

    public static void loadPreferences() {
        AUTO_REFRESH_INTERVAL = preferences.getAutoRefreshInterval();
        INITIAL_DIRECTORY = preferences.getInitialDir();
        WATCH_DIR_FOR_CHANGES = preferences.getWatchForDirChanges();
        CURRENT_LOG_PATTERN = preferences.getLogPattern();
        ALL_LOG_PATTERNS = preferences.getLogPatterns();
    }

    public static long getAutoRefreshInterval() {
        return AUTO_REFRESH_INTERVAL;
    }

    public static void setAutoRefreshInterval(long autoRefreshInterval) {
        AUTO_REFRESH_INTERVAL = autoRefreshInterval;
    }

    public static String getInitialDirectory() {
        return INITIAL_DIRECTORY;
    }

    public static void setInitialDirectory(String initialDirectory) {
        INITIAL_DIRECTORY = initialDirectory;
    }

    public static boolean isWatchDirForChanges() {
        return WATCH_DIR_FOR_CHANGES;
    }

    public static void setWatchDirForChanges(boolean watchDirForChanges) {
        WATCH_DIR_FOR_CHANGES = watchDirForChanges;
    }

    public static String getCurrentLogPattern() {
        return CURRENT_LOG_PATTERN;
    }

    public static void setCurrentLogPattern(String currentLogPattern) {
        CURRENT_LOG_PATTERN = currentLogPattern;
    }

    public static Map<String, String> getAllLogPatterns() {
        return ALL_LOG_PATTERNS;
    }

    public static void addLogPattern(String name, String pattern) {
        ALL_LOG_PATTERNS.put(name, pattern);
    }

    public static void saveAllPreferences() {
        preferences.setAutoRefreshInterval(AUTO_REFRESH_INTERVAL);
        preferences.setInitialDir(INITIAL_DIRECTORY);
        preferences.setLogPattern(CURRENT_LOG_PATTERN);
        preferences.setWatchForDirChanges(WATCH_DIR_FOR_CHANGES);
        for (Map.Entry<String, String> pattern : ALL_LOG_PATTERNS.entrySet()) {
            preferences.addLogPattern(pattern.getKey(), pattern.getValue());
        }
        for (String pattern : PATTERNS_TO_DELETE){
            preferences.removePattern(pattern);
        }
    }
    public static void removePattern(String pattern) {
        PATTERNS_TO_DELETE.add(pattern);
    }
}
