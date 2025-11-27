package com.versionvault.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

public class CommitHistory {
    private Repository repository;
    private Map<String, Commit> commits;

    public CommitHistory(Repository repo) {
        this.repository = repo;
        this.commits = new HashMap<>();
        loadCommits();
    }

    public void addCommit(Commit commit) {
        String hash = commit.getHash();
        commits.put(hash, commit);
        saveCommit(commit);
    }

    public Commit getCommit(String hash) {
        if (hash.length() < 8) {
            return null;
        }

        if (commits.containsKey(hash)) {
            return commits.get(hash);
        }

        for (String key : commits.keySet()) {
            if (key.startsWith(hash)) {
                return commits.get(key);
            }
        }

        return null;
    }

    public List<Commit> getAncestors(String commitHash) {
        List<Commit> ancestors = new ArrayList<>();
        Commit current = getCommit(commitHash);

        while (current != null && current.hasParent()) {
            List<String> parents = current.getParents();
            if (parents.isEmpty())
                break;

            String parentHash = parents.get(0);
            Commit parent = getCommit(parentHash);

            if (parent != null) {
                ancestors.add(parent);
                current = parent;
            } else {
                break;
            }
        }

        return ancestors;
    }

    public Commit findCommonAncestor(String hash1, String hash2) {
        Set<String> ancestors1 = new HashSet<>();
        ancestors1.add(hash1);

        for (Commit c : getAncestors(hash1)) {
            ancestors1.add(c.getHash());
        }

        Commit current = getCommit(hash2);
        while (current != null) {
            if (ancestors1.contains(current.getHash())) {
                return current;
            }

            if (current.hasParent()) {
                String parentHash = current.getParents().get(0);
                current = getCommit(parentHash);
            } else {
                break;
            }
        }

        return null;
    }

    public List<Commit> getAllCommits() {
        return new ArrayList<>(commits.values());
    }

    public List<Commit> getCommitsSorted() {
        return commits.values().stream()
                .sorted((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp()))
                .collect(Collectors.toList());
    }

    public List<Commit> getCommitsByAuthor(User author) {
        return commits.values().stream()
                .filter(c -> c.getAuthor().equals(author))
                .collect(Collectors.toList());
    }

    private void saveCommit(Commit commit) {
        try {
            Path commitsPath = Paths.get(repository.getVVPath(), "commits");
            Files.createDirectories(commitsPath);

            Path commitFile = commitsPath.resolve(commit.getHash() + ".json");

            try (PrintWriter writer = new PrintWriter(commitFile.toFile())) {
                writer.println("{");
                writer.println("  \"hash\": \"" + commit.getHash() + "\",");
                writer.println("  \"message\": \"" + escapeJson(commit.getMessage()) + "\",");
                writer.println("  \"author\": {");
                writer.println("    \"name\": \"" + escapeJson(commit.getAuthor().getName()) + "\",");
                writer.println("    \"email\": \"" + escapeJson(commit.getAuthor().getEmail()) + "\"");
                writer.println("  },");
                writer.println("  \"timestamp\": \"" + commit.getTimestamp().toString() + "\",");

                writer.println("  \"parents\": [");
                List<String> parents = commit.getParents();
                for (int i = 0; i < parents.size(); i++) {
                    writer.print("    \"" + parents.get(i) + "\"");
                    if (i < parents.size() - 1)
                        writer.print(",");
                    writer.println();
                }
                writer.println("  ],");

                writer.println("  \"files\": {");
                Map<String, String> files = commit.getFileHashes();
                int i = 0;
                for (Map.Entry<String, String> entry : files.entrySet()) {
                    writer.print("    \"" + escapeJson(entry.getKey()) + "\": \"" + entry.getValue() + "\"");
                    if (i < files.size() - 1)
                        writer.print(",");
                    writer.println();
                    i++;
                }
                writer.println("  }");

                writer.println("}");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to save commit", e);
        }
    }

    private void loadCommits() {
        Path commitsPath = Paths.get(repository.getVVPath(), "commits");
        if (!Files.exists(commitsPath)) {
            return;
        }

        try {
            Files.walk(commitsPath)
                    .filter(p -> p.toString().endsWith(".json"))
                    .forEach(this::loadCommitFromFile);
        } catch (IOException e) {
        }
    }

    private void loadCommitFromFile(Path file) {
        try {
            List<String> lines = Files.readAllLines(file);
            String content = String.join("\n", lines);

            String hash = extractValue(content, "hash");
            String message = extractValue(content, "message");
            String authorName = extractNestedValue(content, "author", "name");
            String authorEmail = extractNestedValue(content, "author", "email");
            String timestampStr = extractValue(content, "timestamp");

            User author = new User(authorName, authorEmail);
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr);

            Commit commit = new Commit(hash, message, author, timestamp);

            // Parse parents
            List<String> parents = extractArray(content, "parents");
            for (String parent : parents) {
                commit.addParent(parent);
            }

            // Parse files - simple extraction for now
            Map<String, String> files = extractMap(content, "files");
            for (Map.Entry<String, String> entry : files.entrySet()) {
                commit.addFile(entry.getKey(), entry.getValue());
            }

            commits.put(hash, commit);

        } catch (Exception e) {
            System.err.println("Failed to load commit from " + file + ": " + e.getMessage());
        }
    }

    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\": \"";
        int start = json.indexOf(pattern);
        if (start == -1)
            return "";
        start += pattern.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private String extractNestedValue(String json, String parent, String key) {
        String parentPattern = "\"" + parent + "\": {";
        int parentStart = json.indexOf(parentPattern);
        if (parentStart == -1)
            return "";

        String subJson = json.substring(parentStart);
        int parentEnd = subJson.indexOf("}");
        if (parentEnd == -1)
            return "";
        subJson = subJson.substring(0, parentEnd);

        return extractValue(subJson, key);
    }

    private List<String> extractArray(String json, String key) {
        List<String> result = new ArrayList<>();
        String pattern = "\"" + key + "\": [";
        int start = json.indexOf(pattern);
        if (start == -1)
            return result;

        int end = json.indexOf("]", start);
        String arrayContent = json.substring(start + pattern.length(), end);

        String[] items = arrayContent.split(",");
        for (String item : items) {
            item = item.trim();
            if (item.startsWith("\"") && item.endsWith("\"")) {
                result.add(item.substring(1, item.length() - 1));
            }
        }
        return result;
    }

    private Map<String, String> extractMap(String json, String key) {
        Map<String, String> result = new HashMap<>();
        String pattern = "\"" + key + "\": {";
        int start = json.indexOf(pattern);
        if (start == -1)
            return result;

        int end = json.indexOf("}", start);
        String mapContent = json.substring(start + pattern.length(), end);

        String[] lines = mapContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty())
                continue;

            String[] parts = line.split(":");
            if (parts.length == 2) {
                String k = parts[0].trim().replace("\"", "");
                String v = parts[1].trim().replace("\"", "").replace(",", "");
                result.put(k, v);
            }
        }
        return result;
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
