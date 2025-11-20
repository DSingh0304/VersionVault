package com.versionvault.service;

import com.versionvault.model.Commit;
import com.versionvault.model.FileTracker;
import com.versionvault.model.Repository;

import java.util.Map;
import java.util.UUID;

public class VersionVaultService {
    private Repository repository;
    private FileTracker fileTracker;

    public VersionVaultService(String repoPath) {
        this.repository = new Repository(repoPath);
        this.fileTracker = new FileTracker();
    }

    public void init() {
        System.out.println("Repository initialized at " + repository.getPath());
    }

    public void add(String filePath) {
        try {
            fileTracker.addFile(filePath);
            System.out.println("File added: " + filePath);
        } catch (Exception e) {
            System.out.println("Error adding file: " + e.getMessage());
        }
    }

    public void commit(String message, String author, Map<String, String> customTags) {
        String id = UUID.randomUUID().toString();
        Map<String, String> snapshots = fileTracker.getStagedFiles();
        String parentId = repository.getLatestCommit() != null ? repository.getLatestCommit().getId() : null;
        Commit commit = new Commit(id, message, author, snapshots, parentId, customTags);
        repository.addCommit(commit);
        fileTracker.clearStaged();
        System.out.println("Committed: " + id);
    }

    public void log() {
        Commit latest = repository.getLatestCommit();
        if (latest != null) {
            System.out.println("Commit: " + latest.getId());
            System.out.println("Message: " + latest.getMessage());
            System.out.println("Author: " + latest.getAuthor());
            System.out.println("Timestamp: " + latest.getTimestamp());
            if (!latest.getCustomTags().isEmpty()) {
                System.out.println("Tags: " + latest.getCustomTags());
            }
        } else {
            System.out.println("No commits yet.");
        }
    }

    public Repository getRepository() { return repository; }
}