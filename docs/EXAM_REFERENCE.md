# Quick Reference: Where to Find Each OOP Concept

## For Your Exam Defense

When your professor asks "Show me [concept]", use this guide:

## Core OOP Concepts

### 1. Inheritance
**File**: `src/core/FileObject.h` (lines 8-45)
- Base class: `FileObject`
- Derived: `TextFile`, `BinaryFile`

**File**: `src/java/com/versionvault/merge/MergeStrategy.java` (lines 15-30)
- Abstract: `BaseMergeStrategy`
- Concrete: `ThreeWayMerge`, `OursMerge`, `TheirsMerge`

### 2. Polymorphism
**File**: `src/core/FileObject.h` (lines 16-18)
```cpp
virtual bool isBinary() const = 0;
virtual vector<char> readContent() = 0;
```

### 3. Encapsulation
**File**: `src/java/com/versionvault/core/User.java` (lines 8-15)
- Private members with public getters

### 4. Abstraction
**File**: `src/core/FileObject.h` (line 8)
```cpp
class FileObject {
protected:
    virtual string computeHash() = 0;
```

### 5. Static Members
**File**: `src/java/com/versionvault/core/Commit.java` (lines 13-14, 106-108)
```java
private static int commitCounter = 0;
public static int getTotalCommits()
```

### 6. Templates
**File**: `src/core/ObjectStore.h` (lines 8-35)
```cpp
template<typename T>
class StoragePool
```

### 7. Operator Overloading
**File**: `src/core/FileObject.cpp` (lines 16-23)
```cpp
bool FileObject::operator==(const FileObject& other)
```

### 8. Friend Functions
**File**: `src/core/FileObject.h` (line 25)
```cpp
friend class ObjectStore;
```

### 9. Singleton Pattern
**File**: `src/core/ObjectStore.h` (lines 39-45)
```cpp
static ObjectStore* instance;
static mutex mtx;
ObjectStore(const ObjectStore&) = delete;
```

**Implementation**: `src/core/ObjectStore.cpp` (lines 9-22)

### 10. Factory Pattern
**File**: `src/core/FileObject.h` (lines 61-64)
```cpp
class FileFactory {
    static unique_ptr<FileObject> createFileObject(...)
}
```

### 11. Strategy Pattern
**File**: `src/java/com/versionvault/merge/MergeStrategy.java`
- Interface: `MergeStrategy` (lines 1-7)
- Implementations: `ThreeWayMerge`, `OursMerge`, `TheirsMerge`

### 12. Command Pattern
**File**: `src/java/com/versionvault/cli/VersionVaultCLI.java`
- Interface: `Command` (lines 77-79)
- Commands: `AddCommand`, `CommitCommand`, etc. (lines 92+)

### 13. Template Method Pattern
**File**: `src/java/com/versionvault/operations/VaultOperation.java` (lines 8-23)
```java
abstract class VaultOperation {
    public abstract void execute();
    public void validate() { }
}
```

### 14. Composition
**File**: `src/java/com/versionvault/core/Repository.java` (lines 9-14)
```java
class Repository {
    private BranchManager branchManager;
    private CommitHistory commitHistory;
    private StagingArea stagingArea;
}
```

### 15. Aggregation
**File**: `src/java/com/versionvault/core/BranchManager.java` (lines 6-9)
```java
class Branch {
    private Branch trackingBranch;  // can exist independently
}
```

### 16. Exception Handling
**File**: `src/java/com/versionvault/core/Repository.java` (lines 86-93)
```java
class RepositoryException extends Exception {
    public RepositoryException(String message) {
        super(message);
    }
}
```

### 17. Enum Types
**File**: `src/java/com/versionvault/core/User.java` (lines 61-78)
```java
enum UserRole { ADMIN, DEVELOPER, VIEWER }
enum Permission { READ, WRITE, ... }
```

**File**: `src/core/DiffEngine.h` (lines 6-11)
```cpp
enum class ChangeType { ADDED, REMOVED, MODIFIED, UNCHANGED }
```

### 18. Interface Implementation
**File**: `src/java/com/versionvault/merge/MergeStrategy.java` (lines 5-9)
```java
public interface MergeStrategy {
    MergeResult merge(...);
    boolean hasConflicts();
    List<ConflictRegion> getConflicts();
}
```

### 19. Abstract Class
**File**: `src/java/com/versionvault/merge/MergeStrategy.java` (lines 11-30)
```java
public abstract class BaseMergeStrategy implements MergeStrategy {
    protected abstract List<String> performMerge(...);
}
```

### 20. Method Overriding
**File**: `src/java/com/versionvault/core/User.java` (lines 49-66)
```java
@Override
public boolean equals(Object obj) { }

@Override
public String toString() { }
```

### 21. Constructor Overloading
**File**: `src/java/com/versionvault/core/User.java` (lines 9-18)
```java
public User(String name, String email) { }
public User(String name, String email, UserRole role) {
    this(name, email);
    this.role = role;
}
```

### 22. Thread Safety
**File**: `src/java/com/versionvault/lock/LockManager.java` (lines 38-40, 56-58)
```java
public synchronized FileLock acquireLock(...) { }
public synchronized void releaseLock(...) { }
```

**File**: `src/core/ObjectStore.cpp` (lines 12-18)
```cpp
lock_guard<mutex> lock(mtx);
```

### 23. Cloneable Interface
**File**: `src/java/com/versionvault/core/Commit.java` (lines 95-105)
```java
@Override
public Commit clone() {
    Commit cloned = (Commit) super.clone();
    // deep copy logic
}
```

### 24. Comparable Interface
**File**: `src/java/com/versionvault/core/User.java` (lines 67-70)
```java
@Override
public int compareTo(User other) {
    return this.email.compareTo(other.email);
}
```

### 25. Lambda Expressions
**File**: `src/java/com/versionvault/core/CommitHistory.java` (lines 76-85)
```java
commits.values().stream()
    .sorted((c1, c2) -> c2.getTimestamp().compareTo(c1.getTimestamp()))
    .filter(c -> c.getAuthor().equals(author))
    .collect(Collectors.toList());
```

### 26. Smart Pointers (C++)
**File**: `src/core/FileObject.h` (line 62)
```cpp
static unique_ptr<FileObject> createFileObject(...)
```

### 27. Virtual Functions
**File**: `src/core/FileObject.h` (lines 14-18)
```cpp
virtual ~FileObject() = default;
virtual bool isBinary() const = 0;
```

### 28. Nested Classes
**File**: `src/java/com/versionvault/core/Commit.java` (lines 110+)
```java
class CommitMetadata implements Cloneable {
    // nested within commit context
}
```

### 29. Access Modifiers
**File**: `src/core/FileObject.h`
- `protected:` members (lines 9-13)
- `public:` methods (lines 20-25)
- `private:` members in derived classes

### 30. Multiple File Types Handling
**File**: `src/core/FileObject.cpp` (lines 135-155)
- Demonstrates abstraction for text vs binary files

## Tips for Exam

1. **Know the line numbers** - Use the references above
2. **Explain the why** - Each pattern solves a specific problem
3. **Show relationships** - Draw UML diagrams if asked
4. **Demonstrate execution** - Be ready to run the code
5. **Explain advantages** - Know why each OOP concept is useful

## Key Selling Points

1. **Real-world application** - Actual version control system
2. **Practical problem solving** - Solves Git's binary file issue
3. **Hybrid approach** - Shows C++ and Java integration
4. **Production-quality** - Thread-safe, exception-safe code
5. **Complete coverage** - Every OOP concept demonstrated naturally
