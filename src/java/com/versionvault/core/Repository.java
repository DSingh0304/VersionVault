package com.versionvault.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Repository {
    private String rootPath;
    private String vvPath;
    private BranchManager branchManager;
    private CommitHistory commitHistory;
    private StagingArea stagingArea;
    private User currentUser;
    
    public Repository(String path) throws RepositoryException {
        this.rootPath = path;
        this.vvPath = Paths.get(path, ".vv").toString();
        this.commitHistory = new CommitHistory(this);
        this.branchManager = new BranchManager(this);
        this.stagingArea = new StagingArea(this);
    }
    
    public void initialize() throws RepositoryException {
        try {
            Files.createDirectories(Paths.get(vvPath));
            Files.createDirectories(Paths.get(vvPath, "objects"));
            Files.createDirectories(Paths.get(vvPath, "refs", "heads"));
            Files.createDirectories(Paths.get(vvPath, "refs", "tags"));
            
            branchManager.createBranch("main");
            branchManager.checkout("main");
            
            writeConfig();
            
        } catch (IOException e) {
            throw new RepositoryException("Failed to initialize repository: " + e.getMessage());
        }
    }
    
    public boolean isInitialized() {
        return Files.exists(Paths.get(vvPath));
    }
    
    public void setUser(User user) {
        this.currentUser = user;
    }
    
    public User getCurrentUser() {
        if (currentUser == null) {
            loadUserFromConfig();
        }
        return currentUser;
    }
    
    private void loadUserFromConfig() {
        Path configPath = Paths.get(vvPath, "config");
        if (Files.exists(configPath)) {
            try {
                Properties props = new Properties();
                props.load(new FileInputStream(configPath.toFile()));
                
                String name = props.getProperty("user.name");
                String email = props.getProperty("user.email");
                
                if (name != null && email != null) {
                    currentUser = new User(name, email);
                }
            } catch (IOException e) {
            }
        }
    }
    
    private void writeConfig() throws IOException {
        Properties props = new Properties();
        
        if (currentUser != null) {
            props.setProperty("user.name", currentUser.getName());
            props.setProperty("user.email", currentUser.getEmail());
        }
        
        props.setProperty("core.version", "1.0.0");
        props.setProperty("core.filemode", "true");
        
        Path configPath = Paths.get(vvPath, "config");
        props.store(new FileOutputStream(configPath.toFile()), "VersionVault Configuration");
    }
    
    public String getRootPath() {
        return rootPath;
    }
    
    public String getVVPath() {
        return vvPath;
    }
    
    public BranchManager getBranchManager() {
        return branchManager;
    }
    
    public CommitHistory getCommitHistory() {
        return commitHistory;
    }
    
    public StagingArea getStagingArea() {
        return stagingArea;
    }
}
