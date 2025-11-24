package com.versionvault.core;

public class StagedFile {
    private String path;
    private StagingStatus status;
    
    public StagedFile(String path, StagingStatus status) {
        this.path = path;
        this.status = status;
    }
    
    public String getPath() {
        return path;
    }
    
    public StagingStatus getStatus() {
        return status;
    }
}
