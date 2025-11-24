package com.versionvault.lock;

import com.versionvault.core.*;
import java.time.LocalDateTime;
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class LockManager {
    private Repository repository;
    private Map<String, FileLock> locks;
    private Path locksFile;
    
    public LockManager(Repository repo) {
        this.repository = repo;
        this.locks = new HashMap<>();
        this.locksFile = Paths.get(repo.getVVPath(), "locks.dat");
        loadLocks();
    }
    
    public synchronized FileLock acquireLock(String filePath, User user, LockType type) 
            throws LockException {
        
        if (locks.containsKey(filePath)) {
            FileLock existing = locks.get(filePath);
            
            if (existing.getType() == LockType.SHARED && type == LockType.SHARED) {
                return existing;
            }
            
            if (!existing.isOwnedBy(user)) {
                throw new LockException("File is already locked by " + existing.getOwner());
            }
            
            return existing;
        }
        
        FileLock lock = new FileLock(filePath, user, type);
        locks.put(filePath, lock);
        saveLocks();
        
        return lock;
    }
    
    public synchronized void releaseLock(String filePath, User user, boolean force) 
            throws LockException {
        
        if (!locks.containsKey(filePath)) {
            throw new LockException("File is not locked: " + filePath);
        }
        
        FileLock lock = locks.get(filePath);
        
        if (!force && !lock.isOwnedBy(user)) {
            throw new LockException("Cannot release lock owned by " + lock.getOwner());
        }
        
        locks.remove(filePath);
        saveLocks();
    }
    
    public boolean isLocked(String filePath) {
        return locks.containsKey(filePath);
    }
    
    public FileLock getLock(String filePath) {
        return locks.get(filePath);
    }
    
    public synchronized List<FileLock> listLocks() {
        return new ArrayList<>(locks.values());
    }
    
    public boolean canModify(String filePath, User user) {
        if (!locks.containsKey(filePath)) {
            return true;
        }
        
        FileLock lock = locks.get(filePath);
        
        if (lock.getType() == LockType.SHARED) {
            return true;
        }
        
        return lock.isOwnedBy(user);
    }
    
    public synchronized void cleanupExpiredLocks(int hours) {
        List<String> toRemove = new ArrayList<>();
        
        for (Map.Entry<String, FileLock> entry : locks.entrySet()) {
            if (entry.getValue().isExpired(hours)) {
                toRemove.add(entry.getKey());
            }
        }
        
        for (String path : toRemove) {
            locks.remove(path);
        }
        
        if (!toRemove.isEmpty()) {
            saveLocks();
        }
    }
    
    private void saveLocks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(locksFile.toFile()))) {
        } catch (IOException e) {
        }
    }
    
    private void loadLocks() {
        if (!Files.exists(locksFile)) {
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(locksFile.toFile()))) {
        } catch (IOException e) {
        }
    }
}
