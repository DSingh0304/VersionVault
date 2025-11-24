# UML Class Diagrams - VersionVault

## Core File System Hierarchy (C++)

```
┌─────────────────────────────────┐
│       FileObject (Abstract)      │
├─────────────────────────────────┤
│ # filepath: string               │
│ # hash: string                   │
│ # fileSize: long                 │
│ # isModified: bool               │
├─────────────────────────────────┤
│ # computeHash(): string = 0      │
│ + isBinary(): bool = 0           │
│ + readContent(): vector<char> = 0│
│ + writeContent(data): void = 0   │
│ + getHash(): string              │
│ + operator==(other): bool        │
│ + operator!=(other): bool        │
└─────────────────────────────────┘
           △
           │
    ┌──────┴──────┐
    │             │
┌───▽──────┐  ┌──▽─────────┐
│TextFile  │  │BinaryFile  │
├──────────┤  ├────────────┤
│-lines:   │  │-data:      │
│ vector   │  │ vector     │
│-encoding │  │            │
├──────────┤  ├────────────┤
│+getLines │  │+getData    │
│+setLines │  │            │
└──────────┘  └────────────┘
```

## Repository Composition (Java)

```
┌──────────────────────────────────────┐
│           Repository                 │
├──────────────────────────────────────┤
│ - rootPath: String                   │
│ - vvPath: String                     │
│ - branchManager: BranchManager  ◆────┼─────┐
│ - commitHistory: CommitHistory  ◆────┼───┐ │
│ - stagingArea: StagingArea      ◆────┼─┐ │ │
│ - currentUser: User             ◇────┼┐│ │ │
├──────────────────────────────────────┤││ │ │
│ + initialize(): void                 │││ │ │
│ + isInitialized(): boolean           │││ │ │
│ + setUser(user): void                │││ │ │
└──────────────────────────────────────┘││ │ │
                                        ││ │ │
                    ┌───────────────────┘│ │ │
                    │    ┌───────────────┘ │ │
                    │    │    ┌────────────┘ │
                    │    │    │    ┌─────────┘
                    ▽    ▽    ▽    ▽
                ┌────┐┌────┐┌────┐┌────┐
                │User││Stag││Comm││Bran│
                │    ││Area││Hist││Mgr │
                └────┘└────┘└────┘└────┘

◆ = Composition (strong)
◇ = Aggregation (weak)
```

## Merge Strategy Pattern (Java)

```
┌──────────────────────────┐
│   <<interface>>          │
│   MergeStrategy          │
├──────────────────────────┤
│ + merge(): MergeResult   │
│ + hasConflicts(): bool   │
│ + getConflicts(): List   │
└──────────────────────────┘
           △
           │implements
           │
┌──────────┴────────────────┐
│  BaseMergeStrategy        │
│     (Abstract)            │
├───────────────────────────┤
│ # conflicts: List         │
├───────────────────────────┤
│ # performMerge() = 0      │
│ + merge(): MergeResult    │
└───────────────────────────┘
           △
           │extends
     ┌─────┴─────┬────────────┐
     │           │            │
┌────▽──────┐┌──▽──────┐┌───▽──────┐
│ThreeWay   ││Ours     ││Theirs    │
│Merge      ││Merge    ││Merge     │
├───────────┤├─────────┤├──────────┤
│+perform   ││+perform ││+perform  │
│ Merge     ││ Merge   ││ Merge    │
└───────────┘└─────────┘└──────────┘
```

## Command Pattern (Java)

```
┌──────────────────┐
│  <<interface>>   │
│    Command       │
├──────────────────┤
│ + execute()      │
└──────────────────┘
         △
         │implements
         │
    ┌────┴────┬────────┬─────────┬─────────┐
    │         │        │         │         │
┌───▽───┐┌───▽───┐┌──▽────┐┌───▽────┐┌──▽─────┐
│Add    ││Commit ││Branch ││Checkout││Merge   │
│Command││Command││Command││Command ││Command │
├───────┤├───────┤├───────┤├────────┤├────────┤
│+exec  ││+exec  ││+exec  ││+exec   ││+exec   │
└───────┘└───────┘└───────┘└────────┘└────────┘
```

## Singleton Pattern - ObjectStore (C++)

```
┌─────────────────────────────────────┐
│        ObjectStore                   │
├─────────────────────────────────────┤
│ - instance: ObjectStore* (static)   │
│ - mtx: mutex (static)               │
│ - storePath: string                 │
│ - objectPool: StoragePool<...>     │
│ - hashToPath: map<string, string>  │
├─────────────────────────────────────┤
│ - ObjectStore(path)                 │
│ - ObjectStore(const &) = delete     │
│ - operator=(const &) = delete       │
├─────────────────────────────────────┤
│ + getInstance(path): ObjectStore*   │
│ + storeObject(obj): string          │
│ + retrieveObject(hash): unique_ptr  │
│ + hasObject(hash): bool             │
└─────────────────────────────────────┘

Note: Only ONE instance can exist (Singleton)
```

## Class Relationships Summary

### Inheritance Hierarchies

**C++ Inheritance:**
```
FileObject
├── TextFile
└── BinaryFile

DiffAlgorithm
├── MyersDiff
└── SimpleDiff
```

**Java Inheritance:**
```
BaseMergeStrategy
├── ThreeWayMerge
├── OursMerge
└── TheirsMerge

VaultOperation
├── CommitOperation
└── MergeOperation
```

### Composition Relationships

```
Repository ◆──→ BranchManager
Repository ◆──→ CommitHistory
Repository ◆──→ StagingArea
Commit ◆──→ CommitMetadata
ObjectStore ◆──→ StoragePool<T>
```

### Aggregation Relationships

```
Repository ◇──→ User
Branch ◇──→ Branch (tracking)
FileLock ◇──→ User
Commit ◇──→ User (author)
```

### Association Relationships

```
CommitHistory ── Commit
BranchManager ── Branch
StagingArea ── StagedFile
LockManager ── FileLock
```

## Complete Class Diagram Legend

```
┌─────────────┐
│  ClassName  │  Regular class
└─────────────┘

┌─────────────┐
│<<interface>>│  Interface
│  ClassName  │
└─────────────┘

┌─────────────┐
│ ClassName   │  Abstract class
│ (Abstract)  │
└─────────────┘

+ public
- private
# protected
~ package-private

= 0  pure virtual / abstract

△    Inheritance
◆    Composition (strong ownership)
◇    Aggregation (weak ownership)
──   Association
```

## Key OOP Relationships Demonstrated

### 1. IS-A Relationship (Inheritance)
- TextFile IS-A FileObject
- BinaryFile IS-A FileObject
- ThreeWayMerge IS-A BaseMergeStrategy

### 2. HAS-A Relationship (Composition)
- Repository HAS-A BranchManager (dies together)
- Repository HAS-A CommitHistory (dies together)
- Commit HAS-A CommitMetadata (dies together)

### 3. USES-A Relationship (Aggregation)
- Repository USES-A User (independent lifecycle)
- Branch USES-A Branch for tracking (independent)
- FileLock USES-A User (independent)

### 4. DEPENDS-ON (Association)
- CommitHistory depends on Commit
- LockManager depends on FileLock
- BranchManager depends on Branch

## Polymorphism Visualization

```
FileObject* ptr = FileFactory::createFileObject(path);

if (ptr->isBinary()) {
    // Runtime polymorphism
    // Calls BinaryFile::isBinary()
} else {
    // Calls TextFile::isBinary()
}

// Virtual function dispatch at runtime
ptr->computeHash();  // Calls overridden method
```

## Factory Pattern Flow

```
      Request
         │
         ▽
   ┌──────────┐
   │ Factory  │
   └──────────┘
         │
    ┌────┴────┐
    │ Detect  │
    │  Type   │
    └────┬────┘
         │
    ┌────▽─────┐
    │Is Binary?│
    └────┬─────┘
         │
    ┌────▽────┬────────┐
    │         │        │
    No       Yes      
    │         │        
    ▽         ▽        
┌────────┐┌─────────┐
│TextFile││Binary   │
│        ││File     │
└────────┘└─────────┘
```

## Use This For Your Exam

When asked to explain relationships:
1. Draw the relevant diagram on whiteboard
2. Explain inheritance vs composition vs aggregation
3. Show how polymorphism works at runtime
4. Demonstrate design patterns with diagrams

These diagrams make complex relationships visual and easier to understand during your presentation.
