package com.versionvault.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class StagingArea {
    private Repository repository;
    private Map<String, StagedFile> stagedFiles;
    private Set<String> removedFiles;
    
    public StagingArea(Repository repo) {
        this.repository = repo;
        this.stagedFiles = new HashMap<>();
        this.removedFiles = new HashSet<>();
        loadIndex();
    }
    
    public void addFile(String path) throws IOException {
        Path fullPath = Paths.get(repository.getRootPath(), path);
        
        if (!Files.exists(fullPath)) {
            throw new IOException("File does not exist: " + path);
        }
        
        StagedFile stagedFile = new StagedFile(path, StagingStatus.ADDED);
        stagedFiles.put(path, stagedFile);
        removedFiles.remove(path);
        
        saveIndex();
    }
    
    public void removeFile(String path) {
        stagedFiles.remove(path);
        removedFiles.add(path);
        saveIndex();
    }
    
    public void clear() {
        stagedFiles.clear();
        removedFiles.clear();
        saveIndex();
    }
    
    public Map<String, StagedFile> getStagedFiles() {
        return new HashMap<>(stagedFiles);
    }
    
    public Set<String> getRemovedFiles() {
        return new HashSet<>(removedFiles);
    }
    
    public boolean isEmpty() {
        return stagedFiles.isEmpty() && removedFiles.isEmpty();
    }
    
    public boolean isStaged(String path) {
        return stagedFiles.containsKey(path);
    }
    
    private void saveIndex() {
        try {
            Path indexFile = Paths.get(repository.getVVPath(), "index");
            
            try (PrintWriter writer = new PrintWriter(indexFile.toFile())) {
                for (Map.Entry<String, StagedFile> entry : stagedFiles.entrySet()) {
                    writer.println("ADD " + entry.getKey());
                }
                
                for (String removed : removedFiles) {
                    writer.println("REMOVE " + removed);
                }
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to save index", e);
        }
    }
    
    private void loadIndex() {
        Path indexFile = Paths.get(repository.getVVPath(), "index");
        if (!Files.exists(indexFile)) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(indexFile.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 2);
                if (parts.length != 2) continue;
                
                String action = parts[0];
                String path = parts[1];
                
                if ("ADD".equals(action)) {
                    stagedFiles.put(path, new StagedFile(path, StagingStatus.ADDED));
                } else if ("REMOVE".equals(action)) {
                    removedFiles.add(path);
                }
            }
        } catch (IOException e) {
        }
    }
}
