package app.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DirectoryWatchServiceFactory {

    private static final List<File> DIRECTORIES = new ArrayList<>();

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

    public static void removeWatchedDir(File file) {
        File parentDir = file.getParentFile();
        DIRECTORIES.remove(parentDir);
    }
}
