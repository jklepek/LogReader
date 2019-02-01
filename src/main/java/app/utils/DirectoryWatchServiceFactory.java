package app.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Factory for providing DirectoryWatchServices
 */
public class DirectoryWatchServiceFactory {

    private static final List<File> DIRECTORIES = new ArrayList<>();

    /**
     * If the parent directory of the log file already has watch service assigned,
     * empty Optional is returned
     * @param file opened log file
     * @return Optional of DirectoryWatchService
     */
    public static Optional<DirectoryWatchService> getDirectoryWatchService(File file) {
        File parentDir = file.getParentFile();
        if (!DIRECTORIES.contains(parentDir)) {
            DIRECTORIES.add(parentDir);
            return Optional.of(new DirectoryWatchService(parentDir));
        } else {
            System.out.println("Directory is already being watched");
        }
        return Optional.empty();
    }

    /**
     * Removes the parent directory from list of watched directories,
     * when the file is closed
     * @param file closed log file
     */
    public static void removeWatchedDir(File file) {
        File parentDir = file.getParentFile();
        DIRECTORIES.remove(parentDir);
    }
}
