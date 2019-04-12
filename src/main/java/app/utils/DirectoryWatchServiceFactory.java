package app.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for providing DirectoryWatchServices
 */
public class DirectoryWatchServiceFactory {

    private static final Map<File, Integer> DIRECTORIES = new HashMap<>();

    /**
     * If the parent directory of the log file already has watch service assigned,
     * empty Optional is returned
     *
     * @param file opened log file
     * @return Optional of DirectoryWatchService
     */
    public static Optional<DirectoryWatchService> getDirectoryWatchService(File file) {
        File parentDir = file.getParentFile();
        if (!DIRECTORIES.keySet().contains(parentDir)) {
            DIRECTORIES.put(parentDir, 1);
            return Optional.of(new DirectoryWatchService(parentDir));
        } else {
            int count = DIRECTORIES.get(parentDir);
            DIRECTORIES.put(parentDir, count + 1);
        }
        return Optional.empty();
    }

    /**
     * Removes the parent directory from list of watched directories,
     * when the file is closed
     *
     * @param file closed log file
     */
    public static void removeWatchedDir(File file) {
        File parentDir = file.getParentFile();
        int count = DIRECTORIES.get(parentDir);
        if (count > 1) {
            DIRECTORIES.put(parentDir, count - 1);
        } else {
            DIRECTORIES.remove(parentDir);
        }
    }
}
