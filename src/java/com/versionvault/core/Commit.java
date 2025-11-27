package com.versionvault.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.util.*;

public class Commit implements Cloneable {
    private String hash;
    private String message;
    private User author;
    private LocalDateTime timestamp;
    private List<String> parents;
    private Map<String, String> fileHashes;
    private CommitMetadata metadata;

    private static int commitCounter = 0;
    private final int commitNumber;

    public Commit(String message, User author) {
        this.message = message;
        this.author = author;
        this.timestamp = LocalDateTime.now();
        this.parents = new ArrayList<>();
        this.fileHashes = new HashMap<>();
        this.metadata = new CommitMetadata();

        synchronized (Commit.class) {
            commitCounter++;
            this.commitNumber = commitCounter;
        }
    }

    public Commit(String hash, String message, User author, LocalDateTime timestamp) {
        this(message, author);
        this.hash = hash;
        this.timestamp = timestamp;
    }

    public void addParent(String parentHash) {
        this.parents.add(parentHash);
    }

    public void addFile(String path, String fileHash) {
        this.fileHashes.put(path, fileHash);
    }

    public String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            StringBuilder content = new StringBuilder();
            content.append(message);
            content.append(author.getSignature());
            content.append(timestamp.toString());

            for (String parent : parents) {
                content.append(parent);
            }

            TreeMap<String, String> sortedFiles = new TreeMap<>(fileHashes);
            for (Map.Entry<String, String> entry : sortedFiles.entrySet()) {
                content.append(entry.getKey());
                content.append(entry.getValue());
            }

            byte[] hashBytes = digest.digest(content.toString().getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }

            this.hash = hexString.toString();
            return hash;

        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate commit hash", e);
        }
    }

    public String getHash() {
        if (hash == null) {
            calculateHash();
        }
        return hash;
    }

    public String getMessage() {
        return message;
    }

    public User getAuthor() {
        return author;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<String> getParents() {
        return new ArrayList<>(parents);
    }

    public Map<String, String> getFileHashes() {
        return new HashMap<>(fileHashes);
    }

    public boolean hasParent() {
        return !parents.isEmpty();
    }

    public CommitMetadata getMetadata() {
        return metadata;
    }

    public int getCommitNumber() {
        return commitNumber;
    }

    public static int getTotalCommits() {
        return commitCounter;
    }

    @Override
    public Commit clone() {
        try {
            Commit cloned = (Commit) super.clone();
            cloned.parents = new ArrayList<>(this.parents);
            cloned.fileHashes = new HashMap<>(this.fileHashes);
            cloned.metadata = this.metadata.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }

    @Override
    public String toString() {
        return String.format("%s - %s", getHash().substring(0, 8), message);
    }
}

class CommitMetadata implements Cloneable {
    private Map<String, String> tags;
    private int linesAdded;
    private int linesRemoved;
    private int filesChanged;

    public CommitMetadata() {
        this.tags = new HashMap<>();
        this.linesAdded = 0;
        this.linesRemoved = 0;
        this.filesChanged = 0;
    }

    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    public String getTag(String key) {
        return tags.get(key);
    }

    public void setStats(int added, int removed, int changed) {
        this.linesAdded = added;
        this.linesRemoved = removed;
        this.filesChanged = changed;
    }

    public int getLinesAdded() {
        return linesAdded;
    }

    public int getLinesRemoved() {
        return linesRemoved;
    }

    public int getFilesChanged() {
        return filesChanged;
    }

    @Override
    public CommitMetadata clone() {
        try {
            CommitMetadata cloned = (CommitMetadata) super.clone();
            cloned.tags = new HashMap<>(this.tags);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
}
