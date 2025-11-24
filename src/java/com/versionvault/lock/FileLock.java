package com.versionvault.lock;

import com.versionvault.core.*;
import java.time.LocalDateTime;

public class FileLock {
    private String filePath;
    private User owner;
    private LocalDateTime timestamp;
    private String reason;
    private LockType type;
    
    public FileLock(String filePath, User owner, LockType type) {
        this.filePath = filePath;
        this.owner = owner;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.reason = "";
    }
    
    public String getFilePath() { return filePath; }
    public User getOwner() { return owner; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getReason() { return reason; }
    public LockType getType() { return type; }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public boolean isOwnedBy(User user) {
        return owner.equals(user);
    }
    
    public boolean isExpired(int hours) {
        return LocalDateTime.now().minusHours(hours).isAfter(timestamp);
    }
}
