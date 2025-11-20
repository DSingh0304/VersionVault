package com.versionvault.model;

import java.time.LocalDateTime;
import java.util.Map;

public class Commit {
    private String id;
    private String message;
    private String author;
    private LocalDateTime timestamp;
    private Map<String, String> fileSnapshots;
    private String parentCommitId;
    private Map<String, String> customTags;

    public Commit(String id, String message, String author, Map<String, String> fileSnapshots, String parentCommitId, Map<String, String> customTags) {
        this.id = id;
        this.message = message;
        this.author = author;
        this.timestamp = LocalDateTime.now();
        this.fileSnapshots = fileSnapshots;
        this.parentCommitId = parentCommitId;
        this.customTags = customTags;
    }

    // Getters
    public String getId() { return id; }
    public String getMessage() { return message; }
    public String getAuthor() { return author; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, String> getFileSnapshots() { return fileSnapshots; }
    public String getParentCommitId() { return parentCommitId; }
    public Map<String, String> getCustomTags() { return customTags; }
}