# VersionVault

A modern version control system built with Java and C++ that solves Git's complexity issues.

## Key Features That Improve Upon Git

1. **Simplified Merge Conflicts**: Better conflict resolution with clearer markers and smart three-way merge
2. **Built-in File Locking**: Exclusive locks for binary files to prevent merge nightmares
3. **Intuitive Interface**: Cleaner, more straightforward commands
4. **Hybrid Architecture**: C++ core for performance, Java for ease of use
5. **Smart Diff Engine**: Advanced similarity detection for file renames

## OOP Concepts Demonstrated

### Inheritance
- `FileObject` (abstract base) → `TextFile`, `BinaryFile`
- `BaseMergeStrategy` (abstract) → `ThreeWayMerge`, `OursMerge`, `TheirsMerge`
- `VaultOperation` (abstract) → `CommitOperation`, `MergeOperation`
- `DiffAlgorithm` (interface) → `MyersDiff`, `SimpleDiff`

### Polymorphism
- Virtual functions in C++ (`FileObject::isBinary()`, `computeHash()`)
- Interface implementation in Java (`MergeStrategy`, `Command`)
- Method overriding throughout the class hierarchy

### Encapsulation
- Private data members with public getters/setters
- Protected members for inheritance
- Package-private classes

### Abstraction
- Abstract base classes (`FileObject`, `BaseMergeStrategy`, `VaultOperation`)
- Interfaces (`MergeStrategy`, `Command`, `DiffAlgorithm`)
- Pure virtual functions in C++

### Composition
- `Repository` contains `BranchManager`, `CommitHistory`, `StagingArea`
- `Commit` contains `CommitMetadata`
- `ObjectStore` contains `StoragePool<T>`

### Aggregation
- `Branch` references `Branch` (tracking branch)
- `Commit` references parent commits by hash
- `FileLock` references `User`

### Static Members
- `Commit::commitCounter` - tracks total commits
- `ObjectStore::instance` - singleton pattern
- `Commit::getTotalCommits()` - static method

### Templates/Generics
- `StoragePool<T>` template class in C++
- Generic collections in Java (`List<Commit>`, `Map<String, Branch>`)

### Operator Overloading (C++)
- `FileObject::operator==()` and `operator!=()`
- Comparison operators for efficient object comparison

### Friend Functions (C++ only)
- `friend class ObjectStore` in `FileObject`

### Multiple Inheritance Features
- Interface implementation in Java
- Strategy pattern with multiple strategies

### Exception Handling
- Custom exceptions: `RepositoryException`, `LockException`, `OperoryException`
- Try-catch blocks for error handling
- Exception propagation

### Design Patterns
- **Singleton**: `ObjectStore`, `UserManager`
- **Factory**: `FileFactory`, `CommandFactory`
- **Strategy**: `MergeStrategy` implementations
- **Template Method**: `VaultOperation`
- **Command**: CLI command classes

### Nested/Inner Classes
- `CommitMetadata` nested in commit logic
- Various result classes

### Enum Types
- Java: `UserRole`, `Permission`, `LockType`, `MergeStatus`
- C++: `ChangeType` (enum class)

### Interfaces
- `MergeStrategy`, `Command`, `DiffAlgorithm`, `Comparable<User>`

###Thread Safety
- `synchronized` methods in `LockManager`
- `std::mutex` in `ObjectStore`
- Thread-safe singleton implementation

### Cloneable Interface
- `Commit` implements `Cloneable` with deep copy

## Architecture

```
VersionVault
├── C++ Core (Low-level operations)
│   ├── FileObject - File abstraction
│   ├── ObjectStore - Content-addressable storage
│   └── DiffEngine - Diff algorithms
│
└── Java Layer (High-level logic)
    ├── Core
    │   ├── Repository - Main repository
    │   ├── Commit - Commit management
    │   ├── Branch - Branch operations
    │   └── User - User management
    ├── Operations
    │   ├── CommitOperation
    │   └── MergeOperation
    ├── Merge
    │   └── Merge strategies
    ├── Lock
    │   └── File locking
    └── CLI
        └── Command-line interface
```

## Building

### Prerequisites
- CMake 3.15+
- G++ with C++17 support
- OpenSSL development libraries
- Java JDK 11+

### Build Steps

```bash
chmod +x build.sh create_executable.sh
./build.sh
./create_executable.sh
```

## Usage

### Initialize Repository
```bash
./vv init
```

### Add Files
```bash
./vv add myfile.txt
```

### Commit Changes
```bash
./vv commit "My first commit"
```

### Create Branch
```bash
./vv branch feature-x
```

### Switch Branch
```bash
./vv checkout feature-x
```

### Merge Branch
```bash
./vv merge feature-x
```

### Lock Files (for binary files)
```bash
./vv lock image.png
```

### View History
```bash
./vv log
```

### Check Status
```bash
./vv status
```

## Project Structure

```
Version Control/
├── src/
│   ├── core/               # C++ core implementation
│   │   ├── FileObject.h/cpp
│   │   ├── ObjectStore.h/cpp
│   │   └── DiffEngine.h/cpp
│   └── java/
│       └── com/versionvault/
│           ├── core/       # Java core classes
│           ├── operations/ # Operations
│           ├── merge/      # Merge strategies
│           ├── lock/       # File locking
│           └── cli/        # CLI interface
├── CMakeLists.txt
├── build.sh
└── create_executable.sh
```

## Advantages Over Git

1. **Simpler Learning Curve**: More intuitive commands
2. **Binary File Handling**: Native file locking prevents conflicts
3. **Clearer Merges**: Better conflict visualization
4. **Performance**: C++ core for heavy operations
5. **Extensibility**: Clean OOP design makes it easy to extend

## Testing

This system demonstrates all major OOP concepts required for academic evaluation while providing real functionality as a version control system.
