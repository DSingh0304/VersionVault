package com.versionvault.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Branch {
    private String name;
    private String currentCommit;
    private Branch trackingBranch;
    private boolean isRemote;
    
    public Branch(String name) {
        this.name = name;
        this.currentCommit = null;
        this.trackingBranch = null;
        this.isRemote = false;
    }
    
    public String getName() {
        return name;
    }
    
    public void setCurrentCommit(String commitHash) {
        this.currentCommit = commitHash;
    }
    
    public String getCurrentCommit() {
        return currentCommit;
    }
    
    public void setTrackingBranch(Branch branch) {
        this.trackingBranch = branch;
    }
    
    public Branch getTrackingBranch() {
        return trackingBranch;
    }
    
    public boolean isRemote() {
        return isRemote;
    }
    
    public void setRemote(boolean remote) {
        this.isRemote = remote;
    }
    
    @Override
    public String toString() {
        return name + (currentCommit != null ? " -> " + currentCommit.substring(0, 8) : "");
    }
}
