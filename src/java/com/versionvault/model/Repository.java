package com.versionvault.model;

import java.util.HashMap;
import java.util.Map;

public class Repository {
    private String path;
    private Map<String, Commit> commits;
    private String currentBranch;
    private Map<String, String> branches;

    public Repository(String path) {
        this.path = path;
        this.commits = new HashMap<>();
        this.branches = new HashMap<>();
        this.currentBranch = "main";
        branches.put("main", null);
    }

    public void addCommit(Commit commit) {
        commits.put(commit.getId(), commit);
        branches.put(currentBranch, commit.getId());
    }

    public Commit getLatestCommit() {
        String latestId = branches.get(currentBranch);
        return latestId != null ? commits.get(latestId) : null;
    }

    // Getters
    public String getPath() { return path; }
    public Map<String, Commit> getCommits() { return commits; }
    public String getCurrentBranch() { return currentBranch; }
}