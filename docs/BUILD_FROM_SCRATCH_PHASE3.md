# PHASE 3: Operations & Advanced Patterns

Now we implement the operation layer using design patterns like Template Method and Strategy.

---

## Step 16: Repository.java - The Central Hub

**Location**: `src/java/com/versionvault/core/Repository.java`

**Why This Now?**
- Now that we have all components (User, Commit, Branch, etc.), we can create the Repository
- This ties everything together
- Demonstrates COMPOSITION

**OOP Concepts**:
- Composition (HAS-A relationships)
- Constructor chaining
- Dependency injection

**Code**:

```java
package com.versionvault.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Repository {
    private String rootPath;
    private String vvPath;
    
    // COMPOSITION - Repository HAS-A these components (strong ownership)
    private BranchManager branchManager;
    private CommitHistory commitHistory;
    private StagingArea stagingArea;
    
    private User currentUser;
    
    public Repository(String path) throws RepositoryException {
        this.rootPath = path;
        this.vvPath = Paths.get(path, ".vv").toString();
        
        // COMPOSITION - creating owned objects
        this.commitHistory = new CommitHistory(this);
        this.branchManager = new BranchManager(this);
        this.stagingArea = new StagingArea(this);
    }
    
    // Initialize a new repository
    public void initialize() throws RepositoryException {
        try {
            // Create directory structure
            Files.createDirectories(Paths.get(vvPath));
            Files.createDirectories(Paths.get(vvPath, "objects"));
            Files.createDirectories(Paths.get(vvPath, "refs", "heads"));
            Files.createDirectories(Paths.get(vvPath, "refs", "tags"));
            
            // Create main branch
            branchManager.createBranch("main");
            branchManager.checkout("main");
            
            // Write initial config
            writeConfig();
            
        } catch (IOException e) {
            throw new RepositoryException("Failed to initialize repository: " + e.getMessage());
        }
    }
    
    // Check if repository is initialized
    public boolean isInitialized() {
        return Files.exists(Paths.get(vvPath));
    }
    
    // Set current user
    public void setUser(User user) {
        this.currentUser = user;
    }
    
    // Get current user
    public User getCurrentUser() {
        if (currentUser == null) {
            loadUserFromConfig();
        }
        return currentUser;
    }
    
    // Load user from config file
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
                // Config not readable
            }
        }
    }
    
    // Write configuration
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
    
    // GETTERS for composed objects
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
```

**What This Does**:
1. **Composition**: Repository OWNS BranchManager, CommitHistory, StagingArea
2. **Initialization**: Creates `.vv/` directory structure
3. **Configuration**: Manages user config
4. **Central Access Point**: All operations go through Repository

---

## Step 17: OperationResult.java - Operation Result

**Location**: `src/java/com/versionvault/operations/OperationResult.java`

**Code**:

```java
package com.versionvault.operations;

// Represents the result of an operation
public class OperationResult {
    private boolean success;
    private String message;
    private Object data;  // Optional result data
    
    public OperationResult(boolean success, String message) {
        this(success, message, null);
    }
    
    public OperationResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Object getData() {
        return data;
    }
    
    @Override
    public String toString() {
        return (success ? "SUCCESS" : "FAILURE") + ": " + message;
    }
}
```

---

## Step 18: OperationException.java - Operation Exception

**Location**: `src/java/com/versionvault/operations/OperationException.java`

**Code**:

```java
package com.versionvault.operations;

public class OperationException extends Exception {
    public OperationException(String message) {
        super(message);
    }
    
    public OperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

## Step 19: VaultOperation.java - Template Method Pattern

**Location**: `src/java/com/versionvault/operations/VaultOperation.java`

**Why This?**
- Demonstrates **Template Method** design pattern
- Defines operation workflow that all operations follow

**OOP Concepts**:
- Abstract class
- Template Method pattern
- Protected members

**Code**:

```java
package com.versionvault.operations;

import com.versionvault.core.Repository;

// TEMPLATE METHOD PATTERN - defines algorithm structure
public abstract class VaultOperation {
    protected Repository repository;
    protected String operationName;
    
    public VaultOperation(Repository repository, String operationName) {
        this.repository = repository;
        this.operationName = operationName;
    }
    
    // TEMPLATE METHOD - defines the algorithm structure
    public final OperationResult execute() {
        try {
            // Step 1: Validate
            validate();
            
            // Step 2: Execute operation (implemented by subclasses)
            Object result = performOperation();
            
            // Step 3: Post-processing
            onSuccess(result);
            
            return new OperationResult(true, operationName + " completed successfully", result);
            
        } catch (OperationException e) {
            onFailure(e);
            return new OperationResult(false, operationName + " failed: " + e.getMessage());
        }
    }
    
    // ABSTRACT METHODS - must be implemented by subclasses
    protected abstract void validate() throws OperationException;
    protected abstract Object performOperation() throws OperationException;
    
    // HOOK METHODS - can be overridden but have default implementation
    protected void onSuccess(Object result) {
        System.out.println("[" + operationName + "] Operation successful");
    }
    
    protected void onFailure(OperationException e) {
        System.err.println("[" + operationName + "] Operation failed: " + e.getMessage());
    }
    
    // Helper method
    protected void log(String message) {
        System.out.println("[" + operationName + "] " + message);
    }
}
```

**What This Does**:
1. **Template Method**: `execute()` defines workflow (validate → perform → onSuccess/onFailure)
2. **Abstract Methods**: Subclasses must implement `validate()` and `performOperation()`
3. **Hook Methods**: `onSuccess()` and `onFailure()` can be overridden
4. **Final Method**: `execute()` is final - workflow can't be changed

---

## Step 20: CommitOperation.java - Commit Implementation

**Location**: `src/java/com/versionvault/operations/CommitOperation.java`

**Code**:

```java
package com.versionvault.operations;

import com.versionvault.core.*;
import java.util.Map;

// Extends VaultOperation - inherits template method
public class CommitOperation extends VaultOperation {
    private String message;
    private User author;
    
    public CommitOperation(Repository repository, String message, User author) {
        super(repository, "COMMIT");
        this.message = message;
        this.author = author;
    }
    
    // OVERRIDE - implement validation
    @Override
    protected void validate() throws OperationException {
        if (message == null || message.trim().isEmpty()) {
            throw new OperationException("Commit message cannot be empty");
        }
        
        if (author == null) {
            throw new OperationException("Author must be set");
        }
        
        if (repository.getStagingArea().isEmpty()) {
            throw new OperationException("Nothing to commit (staging area is empty)");
        }
        
        log("Validation passed");
    }
    
    // OVERRIDE - implement the actual operation
    @Override
    protected Object performOperation() throws OperationException {
        try {
            // Get staged files
            Map<String, StagedFile> stagedFiles = repository.getStagingArea().getStagedFiles();
            
            // Convert to hash map
            Map<String, String> fileHashes = new java.util.HashMap<>();
            for (Map.Entry<String, StagedFile> entry : stagedFiles.entrySet()) {
                fileHashes.put(entry.getKey(), entry.getValue().getHash());
            }
            
            // Get parent commit (current HEAD)
            BranchManager branchManager = repository.getBranchManager();
            Branch currentBranch = branchManager.getBranch(branchManager.getCurrentBranch());
            String parentHash = (currentBranch != null) ? currentBranch.getHeadCommitHash() : null;
            
            // Create commit
            Commit commit = new Commit(message, author, fileHashes, parentHash);
            
            // Add to history
            repository.getCommitHistory().addCommit(commit);
            
            // Update branch HEAD
            branchManager.updateBranchHead(branchManager.getCurrentBranch(), commit.getHash());
            
            // Clear staging area
            repository.getStagingArea().clear();
            
            log("Created commit: " + commit.getHash().substring(0, 8));
            
            return commit;
            
        } catch (RepositoryException e) {
            throw new OperationException("Failed to create commit", e);
        }
    }
    
    // OVERRIDE - custom success handler
    @Override
    protected void onSuccess(Object result) {
        Commit commit = (Commit) result;
        System.out.println("✓ Commit created successfully: " + commit.getHash().substring(0, 8));
        System.out.println("  Author: " + commit.getAuthor());
        System.out.println("  Message: " + commit.getMessage());
    }
}
```

**What This Does**:
1. **Extends Template**: Uses the workflow from `VaultOperation`
2. **Validation**: Checks message, author, and staging area
3. **Operation**: Creates commit, updates history and branch
4. **Custom Success**: Pretty-prints commit info

---

## Step 21: MergeStrategy.java - Strategy Pattern

**Location**: `src/java/com/versionvault/merge/MergeStrategy.java`

**OOP Concepts**:
- Strategy pattern
- Interface with multiple implementations
- Enum for merge status

**Code**:

```java
package com.versionvault.merge;

import com.versionvault.core.*;
import java.util.*;

// ENUM for merge status
public enum MergeStatus {
    SUCCESS,
    CONFLICT,
    FAST_FORWARD,
    ALREADY_UP_TO_DATE
}

// Result of a merge operation
public class MergeResult {
    private MergeStatus status;
    private String message;
    private List<String> conflicts;
    private Commit mergeCommit;
    
    public MergeResult(MergeStatus status, String message) {
        this.status = status;
        this.message = message;
        this.conflicts = new ArrayList<>();
    }
    
    public MergeStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public List<String> getConflicts() { return conflicts; }
    public Commit getMergeCommit() { return mergeCommit; }
    
    public void addConflict(String file) {
        conflicts.add(file);
    }
    
    public void setMergeCommit(Commit commit) {
        this.mergeCommit = commit;
    }
    
    public boolean hasConflicts() {
        return !conflicts.isEmpty();
    }
}

// STRATEGY INTERFACE - defines merge algorithm interface
public interface MergeStrategy {
    MergeResult merge(Commit base, Commit ours, Commit theirs, User author) throws MergeException;
    String getStrategyName();
}

// EXCEPTION for merge errors
class MergeException extends Exception {
    public MergeException(String message) {
        super(message);
    }
    
    public MergeException(String message, Throwable cause) {
        super(message, cause);
    }
}

// STRATEGY IMPLEMENTATION 1: Three-Way Merge
class ThreeWayMergeStrategy implements MergeStrategy {
    
    @Override
    public String getStrategyName() {
        return "three-way";
    }
    
    @Override
    public MergeResult merge(Commit base, Commit ours, Commit theirs, User author) throws MergeException {
        MergeResult result = new MergeResult(MergeStatus.SUCCESS, "Three-way merge completed");
        
        // Get file sets
        Set<String> baseFiles = base != null ? base.getFiles().keySet() : new HashSet<>();
        Set<String> ourFiles = ours.getFiles().keySet();
        Set<String> theirFiles = theirs.getFiles().keySet();
        
        // Find all files
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(ourFiles);
        allFiles.addAll(theirFiles);
        
        Map<String, String> mergedFiles = new HashMap<>();
        
        for (String file : allFiles) {
            String baseHash = base != null ? base.getFiles().get(file) : null;
            String ourHash = ours.getFiles().get(file);
            String theirHash = theirs.getFiles().get(file);
            
            // No conflict scenarios
            if (Objects.equals(ourHash, theirHash)) {
                // Same in both - use either
                if (ourHash != null) {
                    mergedFiles.put(file, ourHash);
                }
            } else if (Objects.equals(baseHash, ourHash)) {
                // Only changed in theirs - use theirs
                if (theirHash != null) {
                    mergedFiles.put(file, theirHash);
                }
            } else if (Objects.equals(baseHash, theirHash)) {
                // Only changed in ours - use ours
                if (ourHash != null) {
                    mergedFiles.put(file, ourHash);
                }
            } else {
                // CONFLICT - both modified differently
                result.addConflict(file);
                result = new MergeResult(MergeStatus.CONFLICT, "Merge conflicts detected");
                // For now, use ours (can be improved)
                if (ourHash != null) {
                    mergedFiles.put(file, ourHash);
                }
            }
        }
        
        // Create merge commit if successful
        if (!result.hasConflicts()) {
            String message = "Merge branch '" + theirs.getHash().substring(0, 8) + "' into '" 
                           + ours.getHash().substring(0, 8) + "'";
            Commit mergeCommit = new Commit(message, author, mergedFiles, ours.getHash());
            result.setMergeCommit(mergeCommit);
        }
        
        return result;
    }
}

// STRATEGY IMPLEMENTATION 2: Fast-Forward Merge
class FastForwardMergeStrategy implements MergeStrategy {
    
    @Override
    public String getStrategyName() {
        return "fast-forward";
    }
    
    @Override
    public MergeResult merge(Commit base, Commit ours, Commit theirs, User author) throws MergeException {
        // Fast-forward: just move branch pointer to theirs
        MergeResult result = new MergeResult(MergeStatus.FAST_FORWARD, "Fast-forward merge");
        result.setMergeCommit(theirs);
        return result;
    }
}

// STRATEGY IMPLEMENTATION 3: Ours Strategy (take our changes)
class OursStrategy implements MergeStrategy {
    
    @Override
    public String getStrategyName() {
        return "ours";
    }
    
    @Override
    public MergeResult merge(Commit base, Commit ours, Commit theirs, User author) throws MergeException {
        MergeResult result = new MergeResult(MergeStatus.SUCCESS, "Merge using 'ours' strategy");
        
        String message = "Merge (ours): " + theirs.getHash().substring(0, 8);
        Commit mergeCommit = new Commit(message, author, ours.getFiles(), ours.getHash());
        result.setMergeCommit(mergeCommit);
        
        return result;
    }
}

// STRATEGY IMPLEMENTATION 4: Theirs Strategy (take their changes)
class TheirsStrategy implements MergeStrategy {
    
    @Override
    public String getStrategyName() {
        return "theirs";
    }
    
    @Override
    public MergeResult merge(Commit base, Commit ours, Commit theirs, User author) throws MergeException {
        MergeResult result = new MergeResult(MergeStatus.SUCCESS, "Merge using 'theirs' strategy");
        
        String message = "Merge (theirs): " + theirs.getHash().substring(0, 8);
        Commit mergeCommit = new Commit(message, author, theirs.getFiles(), ours.getHash());
        result.setMergeCommit(mergeCommit);
        
        return result;
    }
}
```

**What This Does**:
1. **Strategy Interface**: Defines `merge()` method
2. **Multiple Implementations**: 4 different merge strategies
3. **Three-Way Merge**: Compares base, ours, theirs to detect conflicts
4. **Fast-Forward**: Just moves branch pointer
5. **Ours/Theirs**: Takes one side's changes

---

## Step 22: MergeOperation.java - Merge Implementation

**Location**: `src/java/com/versionvault/operations/MergeOperation.java`

**Code**:

```java
package com.versionvault.operations;

import com.versionvault.core.*;
import com.versionvault.merge.*;

public class MergeOperation extends VaultOperation {
    private String targetBranch;
    private MergeStrategy strategy;
    private User author;
    
    public MergeOperation(Repository repository, String targetBranch, 
                         MergeStrategy strategy, User author) {
        super(repository, "MERGE");
        this.targetBranch = targetBranch;
        this.strategy = strategy;
        this.author = author;
    }
    
    @Override
    protected void validate() throws OperationException {
        if (targetBranch == null || targetBranch.trim().isEmpty()) {
            throw new OperationException("Target branch not specified");
        }
        
        Branch target = repository.getBranchManager().getBranch(targetBranch);
        if (target == null) {
            throw new OperationException("Branch not found: " + targetBranch);
        }
        
        if (!repository.getStagingArea().isEmpty()) {
            throw new OperationException("Please commit or stash changes before merging");
        }
        
        log("Validation passed");
    }
    
    @Override
    protected Object performOperation() throws OperationException {
        try {
            BranchManager branchManager = repository.getBranchManager();
            CommitHistory history = repository.getCommitHistory();
            
            // Get commits
            String currentBranchName = branchManager.getCurrentBranch();
            Branch currentBranch = branchManager.getBranch(currentBranchName);
            Branch targetBranch = branchManager.getBranch(this.targetBranch);
            
            Commit ours = history.getCommit(currentBranch.getHeadCommitHash());
            Commit theirs = history.getCommit(targetBranch.getHeadCommitHash());
            
            // Find common ancestor (simplified - just use ours parent)
            Commit base = history.getParentCommit(ours);
            
            // Perform merge using strategy
            MergeResult result = strategy.merge(base, ours, theirs, author);
            
            if (result.hasConflicts()) {
                log("Conflicts detected in files: " + result.getConflicts());
                throw new OperationException("Merge conflicts: " + result.getConflicts());
            }
            
            // Update branch
            Commit mergeCommit = result.getMergeCommit();
            if (mergeCommit != null) {
                history.addCommit(mergeCommit);
                branchManager.updateBranchHead(currentBranchName, mergeCommit.getHash());
            }
            
            return result;
            
        } catch (Exception e) {
            throw new OperationException("Merge failed", e);
        }
    }
    
    @Override
    protected void onSuccess(Object result) {
        MergeResult mergeResult = (MergeResult) result;
        System.out.println("✓ Merge successful using " + strategy.getStrategyName() + " strategy");
        System.out.println("  Status: " + mergeResult.getStatus());
    }
}
```

---

## Summary of Phase 3

You've now built:
✅ **Repository** - Central hub with composition
✅ **OperationResult & OperationException** - Operation result handling
✅ **VaultOperation** - Template Method pattern
✅ **CommitOperation** - Concrete operation
✅ **MergeStrategy** - Strategy pattern with 4 implementations
✅ **MergeOperation** - Merge with pluggable strategies

**OOP Concepts Added**:
- ✅ Template Method Pattern
- ✅ Strategy Pattern
- ✅ Composition (Repository HAS-A components)
- ✅ Abstract classes vs Interfaces
- ✅ Protected members
- ✅ Final methods
- ✅ Hook methods

**Next**: Phase 4 - File Locking & CLI!
