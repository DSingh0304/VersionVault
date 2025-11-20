package com.versionvault.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileTracker {
    private Map<String, String> stagedFiles;

    public FileTracker() {
        this.stagedFiles = new HashMap<>();
    }

    public void addFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        String content = Files.readString(path);
        stagedFiles.put(filePath, content);
    }

    public Map<String, String> getStagedFiles() {
        return stagedFiles;
    }

    public void clearStaged() {
        stagedFiles.clear();
    }
}