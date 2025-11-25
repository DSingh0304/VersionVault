# PHASE 2: Java Core Domain

Now we'll build the Java layer that uses our C++ foundation. We start with core domain objects.

---

## Step 7: User.java - User Representation

**Location**: `src/java/com/versionvault/core/User.java`

**Why This First in Java?**
- Simple class with no dependencies
- Used by Commit, Repository, LockManager
- Demonstrates Java-specific features

**OOP Concepts**:
- Comparable interface
- Enums
- Encapsulation
- ToString override

**Code**:

```java
package com.versionvault.core;

import java.util.Objects;

// ENUM for user roles
public enum UserRole {
    ADMIN,      // Full permissions
    DEVELOPER,  // Can read/write/lock
    VIEWER      // Read-only
}

// COMPARABLE INTERFACE - allows sorting users
public class User implements Comparable<User> {
    private String name;
    private String email;
    private UserRole role;
    
    // CONSTRUCTOR OVERLOADING
    public User(String name, String email) {
        this(name, email, UserRole.DEVELOPER);  // Default role
    }
    
    public User(String name, String email, UserRole role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }
    
    // ENCAPSULATION - getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    // COMPARABLE IMPLEMENTATION - compare by email
    @Override
    public int compareTo(User other) {
        return this.email.compareTo(other.email);
    }
    
    // EQUALS AND HASHCODE - for collections
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(email, user.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
    
    // TOSTRING - for debugging and display
    @Override
    public String toString() {
        return name + " <" + email + "> [" + role + "]";
    }
    
    // Business logic
    public boolean hasPermission(UserRole requiredRole) {
        return this.role.ordinal() <= requiredRole.ordinal();
    }
    
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
```

**What This Does**:
1. **Comparable**: Users can be sorted by email
2. **Enum**: Type-safe role definition
3. **Equals/HashCode**: Proper behavior in collections
4. **Permission Logic**: Role-based access control

**Connection**:
- Used by `Commit` (author)
- Used by `Repository` (current user)
- Used by `LockManager` (permissions)

---

## Step 8: RepositoryException.java - Custom Exception

**Location**: `src/java/com/versionvault/core/RepositoryException.java`

**Why This?**
- Custom exceptions for better error handling
- Used throughout the codebase

**Code**:

```java
package com.versionvault.core;

// CUSTOM EXCEPTION - extends Exception
public class RepositoryException extends Exception {
    
    // CONSTRUCTOR OVERLOADING
    public RepositoryException(String message) {
        super(message);
    }
    
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RepositoryException(Throwable cause) {
        super(cause);
    }
}
```

**What This Does**:
- Custom exception for repository operations
- Wraps underlying IOExceptions with meaningful messages

---

## Step 9: StagedFile.java - Staging Area Entry

**Location**: `src/java/com/versionvault/core/StagedFile.java`

**Code**:

```java
package com.versionvault.core;

import java.nio.file.Path;
import java.time.LocalDateTime;

// Represents a file in the staging area
public class StagedFile {
    private Path filePath;
    private String hash;
    private long fileSize;
    private LocalDateTime stagedTime;
    
    public StagedFile(Path filePath, String hash, long fileSize) {
        this.filePath = filePath;
        this.hash = hash;
        this.fileSize = fileSize;
        this.stagedTime = LocalDateTime.now();
    }
    
    // Getters
    public Path getFilePath() {
        return filePath;
    }
    
    public String getHash() {
        return hash;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public LocalDateTime getStagedTime() {
        return stagedTime;
    }
    
    @Override
    public String toString() {
        return filePath.toString() + " (" + hash.substring(0, 8) + ")";
    }
}
```

**What This Does**:
- Represents a staged file with metadata
- Stores hash for tracking changes

---

## Step 10: StagingStatus.java - Status Enum

**Location**: `src/java/com/versionvault/core/StagingStatus.java`

**Code**:

```java
package com.versionvault.core;

// ENUM for file status
public enum StagingStatus {
    UNTRACKED,      // New file not in repo
    MODIFIED,       // Changed since last commit
    ADDED,          // Staged for commit
    DELETED,        // Marked for deletion
    UNCHANGED       // No changes
}
```

---

## Step 11: StagingArea.java - Staging Management

**Location**: `src/java/com/versionvault/core/StagingArea.java`

**Why This?**
- Manages files before commit
- Like `git add` functionality

**OOP Concepts**:
- Encapsulation
- Collections (HashMap)
- Association with Repository

**Code**:

```java
package com.versionvault.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class StagingArea {
    private Repository repository;
    private Map<String, StagedFile> stagedFiles;  // path -> StagedFile
    private String stagingFilePath;
    
    // ASSOCIATION - StagingArea knows about Repository
    public StagingArea(Repository repository) {
        this.repository = repository;
        this.stagedFiles = new HashMap<>();
        this.stagingFilePath = repository.getVVPath() + "/index";
        loadStagingArea();
    }
    
    // Add file to staging
    public void addFile(String filepath) throws RepositoryException {
        try {
            Path path = Paths.get(repository.getRootPath(), filepath);
            
            if (!Files.exists(path)) {
                throw new RepositoryException("File not found: " + filepath);
            }
            
            // Compute hash (simplified - in real implementation, use C++ ObjectStore)
            String hash = computeHash(path);
            long size = Files.size(path);
            
            StagedFile staged = new StagedFile(path, hash, size);
            stagedFiles.put(filepath, staged);
            
            saveStagingArea();
            
        } catch (IOException e) {
            throw new RepositoryException("Failed to add file: " + e.getMessage(), e);
        }
    }
    
    // Remove file from staging
    public void removeFile(String filepath) throws RepositoryException {
        if (stagedFiles.remove(filepath) != null) {
            saveStagingArea();
        } else {
            throw new RepositoryException("File not in staging area: " + filepath);
        }
    }
    
    // Get all staged files
    public Map<String, StagedFile> getStagedFiles() {
        return new HashMap<>(stagedFiles);  // Return copy for immutability
    }
    
    // Check if staging area is empty
    public boolean isEmpty() {
        return stagedFiles.isEmpty();
    }
    
    // Clear staging area (after commit)
    public void clear() throws RepositoryException {
        stagedFiles.clear();
        saveStagingArea();
    }
    
    // Get status of a file
    public StagingStatus getFileStatus(String filepath) {
        if (stagedFiles.containsKey(filepath)) {
            return StagingStatus.ADDED;
        }
        
        Path path = Paths.get(repository.getRootPath(), filepath);
        if (!Files.exists(path)) {
            return StagingStatus.DELETED;
        }
        
        // TODO: Check against last commit to determine MODIFIED vs UNCHANGED
        return StagingStatus.UNTRACKED;
    }
    
    // Persist staging area to disk
    private void saveStagingArea() throws RepositoryException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(stagingFilePath))) {
            oos.writeObject(stagedFiles);
        } catch (IOException e) {
            throw new RepositoryException("Failed to save staging area", e);
        }
    }
    
    // Load staging area from disk
    @SuppressWarnings("unchecked")
    private void loadStagingArea() {
        Path indexPath = Paths.get(stagingFilePath);
        if (Files.exists(indexPath)) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(stagingFilePath))) {
                stagedFiles = (Map<String, StagedFile>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                stagedFiles = new HashMap<>();
            }
        }
    }
    
    // Simple hash computation (placeholder)
    private String computeHash(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        return Integer.toHexString(Arrays.hashCode(bytes));
    }
}
```

**What This Does**:
1. **HashMap**: Stores staged files by path
2. **Serialization**: Persists staging state to disk
3. **Association**: References Repository (weak relationship)
4. **Status Tracking**: Determines file state

**Connection**:
- Used by `Repository` (composition)
- Used by `AddCommand`
- Used by `CommitOperation`

---

## Step 12: Commit.java - Commit Object

**Location**: `src/java/com/versionvault/core/Commit.java`

**OOP Concepts**:
- Cloneable interface
- Static members
- Immutability pattern
- Association with User

**Code**:

```java
package com.versionvault.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

// CLONEABLE INTERFACE - allows deep copying
public class Commit implements Cloneable {
    // STATIC MEMBER - shared across all instances
    private static int commitCounter = 0;
    
    private final String hash;           // Unique identifier
    private final String message;
    private final User author;           // ASSOCIATION with User
    private final LocalDateTime timestamp;
    private final String parentHash;     // Previous commit (null for first)
    private final Map<String, String> files;  // filepath -> hash
    
    // Nested class for metadata
    public static class CommitMetadata {
        public final String shortHash;
        public final String author;
        public final String date;
        public final int filesChanged;
        
        public CommitMetadata(Commit commit) {
            this.shortHash = commit.hash.substring(0, 8);
            this.author = commit.author.getName();
            this.date = commit.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            this.filesChanged = commit.files.size();
        }
    }
    
    // CONSTRUCTOR
    public Commit(String message, User author, Map<String, String> files, String parentHash) {
        this.message = message;
        this.author = author;
        this.files = new HashMap<>(files);  // Defensive copy
        this.parentHash = parentHash;
        this.timestamp = LocalDateTime.now();
        this.hash = generateHash();
        
        // INCREMENT STATIC COUNTER
        commitCounter++;
    }
    
    // Generate unique hash for this commit
    private String generateHash() {
        String data = message + author.getEmail() + timestamp.toString() + commitCounter;
        return Integer.toHexString(data.hashCode()) + UUID.randomUUID().toString().substring(0, 8);
    }
    
    // GETTERS (immutable - no setters)
    public String getHash() {
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
    
    public String getParentHash() {
        return parentHash;
    }
    
    public Map<String, String> getFiles() {
        return new HashMap<>(files);  // Return defensive copy
    }
    
    // STATIC METHOD
    public static int getTotalCommits() {
        return commitCounter;
    }
    
    public static void resetCounter() {
        commitCounter = 0;
    }
    
    // CLONEABLE IMPLEMENTATION - deep copy
    @Override
    public Commit clone() {
        try {
            Commit cloned = (Commit) super.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported", e);
        }
    }
    
    // Get metadata
    public CommitMetadata getMetadata() {
        return new CommitMetadata(this);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Commit ").append(hash.substring(0, 8)).append("\n");
        sb.append("Author: ").append(author).append("\n");
        sb.append("Date: ").append(timestamp).append("\n\n");
        sb.append("    ").append(message).append("\n");
        return sb.toString();
    }
}
```

**What This Does**:
1. **Static Counter**: Tracks total commits
2. **Immutable**: Once created, cannot be modified
3. **Cloneable**: Can be deep copied
4. **Nested Class**: `CommitMetadata` for summary info
5. **Association**: References `User` as author

**Key Concepts**:
- Defensive copying in getters
- Final fields for immutability
- Static members shared across instances

---

## Step 13: Branch.java - Branch Object

**Location**: `src/java/com/versionvault/core/Branch.java`

**Code**:

```java
package com.versionvault.core;

// Represents a branch
public class Branch {
    private String name;
    private String headCommitHash;  // Points to latest commit
    private Branch parentBranch;    // AGGREGATION - weak reference
    
    public Branch(String name, String headCommitHash) {
        this.name = name;
        this.headCommitHash = headCommitHash;
        this.parentBranch = null;
    }
    
    public Branch(String name, String headCommitHash, Branch parent) {
        this.name = name;
        this.headCommitHash = headCommitHash;
        this.parentBranch = parent;  // AGGREGATION
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public String getHeadCommitHash() {
        return headCommitHash;
    }
    
    public void setHeadCommitHash(String hash) {
        this.headCommitHash = hash;
    }
    
    public Branch getParentBranch() {
        return parentBranch;
    }
    
    public void setParentBranch(Branch parent) {
        this.parentBranch = parent;
    }
    
    @Override
    public String toString() {
        String parent = (parentBranch != null) ? parentBranch.getName() : "none";
        return name + " -> " + (headCommitHash != null ? headCommitHash.substring(0, 8) : "empty") 
               + " (parent: " + parent + ")";
    }
}
```

**What This Does**:
- **Aggregation**: Branch can reference parent branch (weak ownership)
- Tracks HEAD commit
- Can exist independently of parent

---

## Step 14: BranchManager.java - Branch Management

**Location**: `src/java/com/versionvault/core/BranchManager.java`

**OOP Concepts**:
- Composition (owned by Repository)
- Collections management
- File I/O

**Code**:

```java
package com.versionvault.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BranchManager {
    private Repository repository;  // COMPOSITION - BranchManager belongs to Repository
    private Map<String, Branch> branches;
    private String currentBranch;
    private String headsPath;
    
    public BranchManager(Repository repository) {
        this.repository = repository;
        this.branches = new HashMap<>();
        this.headsPath = repository.getVVPath() + "/refs/heads";
        this.currentBranch = "main";
    }
    
    // Create a new branch
    public void createBranch(String branchName) throws RepositoryException {
        if (branches.containsKey(branchName)) {
            throw new RepositoryException("Branch already exists: " + branchName);
        }
        
        // Get current HEAD commit
        String headCommit = getCurrentHeadCommit();
        
        Branch newBranch = new Branch(branchName, headCommit);
        
        // Set parent if creating from existing branch
        if (currentBranch != null && branches.containsKey(currentBranch)) {
            newBranch.setParentBranch(branches.get(currentBranch));
        }
        
        branches.put(branchName, newBranch);
        saveBranch(newBranch);
    }
    
    // Delete a branch
    public void deleteBranch(String branchName) throws RepositoryException {
        if (branchName.equals(currentBranch)) {
            throw new RepositoryException("Cannot delete current branch");
        }
        
        if (!branches.containsKey(branchName)) {
            throw new RepositoryException("Branch not found: " + branchName);
        }
        
        branches.remove(branchName);
        
        try {
            Files.deleteIfExists(Paths.get(headsPath, branchName));
        } catch (IOException e) {
            throw new RepositoryException("Failed to delete branch file", e);
        }
    }
    
    // Switch to a different branch (checkout)
    public void checkout(String branchName) throws RepositoryException {
        if (!branches.containsKey(branchName)) {
            throw new RepositoryException("Branch not found: " + branchName);
        }
        
        currentBranch = branchName;
        updateHEAD(branchName);
    }
    
    // Get current branch name
    public String getCurrentBranch() {
        return currentBranch;
    }
    
    // Get branch object
    public Branch getBranch(String name) {
        return branches.get(name);
    }
    
    // List all branches
    public List<String> listBranches() {
        return new ArrayList<>(branches.keySet());
    }
    
    // Update branch HEAD to new commit
    public void updateBranchHead(String branchName, String commitHash) throws RepositoryException {
        Branch branch = branches.get(branchName);
        if (branch == null) {
            throw new RepositoryException("Branch not found: " + branchName);
        }
        
        branch.setHeadCommitHash(commitHash);
        saveBranch(branch);
    }
    
    // Get HEAD commit of current branch
    private String getCurrentHeadCommit() {
        if (currentBranch != null && branches.containsKey(currentBranch)) {
            return branches.get(currentBranch).getHeadCommitHash();
        }
        return null;
    }
    
    // Save branch to disk
    private void saveBranch(Branch branch) throws RepositoryException {
        try {
            Path branchFile = Paths.get(headsPath, branch.getName());
            String commitHash = branch.getHeadCommitHash();
            if (commitHash != null) {
                Files.write(branchFile, commitHash.getBytes());
            } else {
                Files.write(branchFile, "".getBytes());
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to save branch", e);
        }
    }
    
    // Update HEAD file
    private void updateHEAD(String branchName) throws RepositoryException {
        try {
            Path headFile = Paths.get(repository.getVVPath(), "HEAD");
            String ref = "ref: refs/heads/" + branchName;
            Files.write(headFile, ref.getBytes());
        } catch (IOException e) {
            throw new RepositoryException("Failed to update HEAD", e);
        }
    }
    
    // Load all branches from disk
    public void loadBranches() throws RepositoryException {
        try {
            Path headsDir = Paths.get(headsPath);
            if (Files.exists(headsDir)) {
                Files.list(headsDir).forEach(branchFile -> {
                    try {
                        String branchName = branchFile.getFileName().toString();
                        String commitHash = new String(Files.readAllBytes(branchFile)).trim();
                        branches.put(branchName, new Branch(branchName, commitHash.isEmpty() ? null : commitHash));
                    } catch (IOException e) {
                        // Skip invalid branch files
                    }
                });
            }
            
            // Load current branch from HEAD
            Path headFile = Paths.get(repository.getVVPath(), "HEAD");
            if (Files.exists(headFile)) {
                String head = new String(Files.readAllBytes(headFile)).trim();
                if (head.startsWith("ref: refs/heads/")) {
                    currentBranch = head.substring("ref: refs/heads/".length());
                }
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to load branches", e);
        }
    }
}
```

**What This Does**:
1. **Composition**: BranchManager can't exist without Repository
2. **Branch Creation**: Creates new branches from current HEAD
3. **Checkout**: Switches between branches
4. **Persistence**: Saves branch refs to disk

---

## Step 15: CommitHistory.java - Commit Timeline

**Location**: `src/java/com/versionvault/core/CommitHistory.java`

**OOP Concepts**:
- Streams API
- Lambda expressions
- Collections

**Code**:

```java
package com.versionvault.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class CommitHistory {
    private Repository repository;
    private List<Commit> commits;
    private String commitsPath;
    
    public CommitHistory(Repository repository) {
        this.repository = repository;
        this.commits = new ArrayList<>();
        this.commitsPath = repository.getVVPath() + "/commits";
    }
    
    // Add a commit to history
    public void addCommit(Commit commit) throws RepositoryException {
        commits.add(commit);
        saveCommit(commit);
    }
    
    // Get commit by hash
    public Commit getCommit(String hash) {
        // LAMBDA EXPRESSION with STREAMS API
        return commits.stream()
                .filter(commit -> commit.getHash().equals(hash))
                .findFirst()
                .orElse(null);
    }
    
    // Get all commits
    public List<Commit> getAllCommits() {
        return new ArrayList<>(commits);  // Defensive copy
    }
    
    // Get commits by author
    public List<Commit> getCommitsByAuthor(User author) {
        // STREAMS API with filter
        return commits.stream()
                .filter(commit -> commit.getAuthor().equals(author))
                .collect(Collectors.toList());
    }
    
    // Get recent commits (limit)
    public List<Commit> getRecentCommits(int limit) {
        // STREAMS API with limit
        return commits.stream()
                .sorted((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    // Search commits by message
    public List<Commit> searchCommits(String keyword) {
        // LAMBDA with string matching
        return commits.stream()
                .filter(commit -> commit.getMessage().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    // Get commit count
    public int getCommitCount() {
        return commits.size();
    }
    
    // Get parent commit
    public Commit getParentCommit(Commit commit) {
        String parentHash = commit.getParentHash();
        return (parentHash != null) ? getCommit(parentHash) : null;
    }
    
    // Save commit to disk
    private void saveCommit(Commit commit) throws RepositoryException {
        try {
            Path commitFile = Paths.get(commitsPath, commit.getHash());
            Files.createDirectories(Paths.get(commitsPath));
            
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(commitFile.toFile()))) {
                oos.writeObject(commit);
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to save commit", e);
        }
    }
    
    // Load all commits from disk
    public void loadCommits() throws RepositoryException {
        try {
            Path commitsDir = Paths.get(commitsPath);
            if (Files.exists(commitsDir)) {
                Files.list(commitsDir).forEach(commitFile -> {
                    try (ObjectInputStream ois = new ObjectInputStream(
                            new FileInputStream(commitFile.toFile()))) {
                        Commit commit = (Commit) ois.readObject();
                        commits.add(commit);
                    } catch (IOException | ClassNotFoundException e) {
                        // Skip corrupted commits
                    }
                });
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to load commits", e);
        }
    }
}
```

**What This Does**:
1. **Streams API**: Modern Java 8+ features for filtering/searching
2. **Lambda Expressions**: Concise filtering logic
3. **Serialization**: Persists commits to disk
4. **Search Functionality**: Find commits by various criteria

---

## Summary of Phase 2 (Java Core)

You've now built the Java core domain:
✅ **User** - Comparable, Enums
✅ **RepositoryException** - Custom exceptions
✅ **StagedFile** & **StagingStatus** - Staging support
✅ **StagingArea** - File staging with HashMap
✅ **Commit** - Cloneable, Static members, Immutability
✅ **Branch** - Aggregation pattern
✅ **BranchManager** - Branch operations
✅ **CommitHistory** - Streams API, Lambdas

**OOP Concepts Added**:
- ✅ Comparable Interface
- ✅ Enums
- ✅ Custom Exceptions
- ✅ Collections (HashMap, ArrayList)
- ✅ Streams API
- ✅ Lambda Expressions
- ✅ Cloneable Interface
- ✅ Static Members
- ✅ Composition vs Aggregation
- ✅ Immutability Pattern
- ✅ Serialization

**Next**: Phase 3 - Operations & Advanced Patterns!
