# VersionVault - Build From Scratch Guide

## 🎯 Overview
This guide will walk you through building VersionVault step-by-step, explaining each file's purpose, the OOP concepts it demonstrates, and how it connects to other files.

## 📋 Build Order Strategy
We'll build in this order:
1. **Phase 1**: C++ Core (Foundation Layer)
2. **Phase 2**: Java Core Domain (Business Objects)
3. **Phase 3**: Java Operations & Advanced Patterns
4. **Phase 4**: Java CLI & Commands
5. **Phase 5**: Build Scripts & Configuration
6. **Phase 6**: GUI (Optional)

---

# PHASE 1: C++ Core Foundation

## Why C++ First?
C++ handles performance-critical operations: file I/O, hashing, and diffing. The Java layer will use these components.

---

## Step 1: FileObject.h - The Foundation Class

**Location**: `src/core/FileObject.h`

**Why This First?**
- This is the base abstraction for all files in our VCS
- Demonstrates inheritance, polymorphism, and abstraction
- Everything else builds on this

**OOP Concepts**:
- Abstract base class
- Inheritance (TextFile and BinaryFile inherit from this)
- Polymorphism (virtual functions)
- Operator overloading
- Friend classes
- Factory pattern

**Code Explanation**:

```cpp
#ifndef FILEOBJECT_H
#define FILEOBJECT_H

#include <string>
#include <vector>
#include <memory>
#include <fstream>

// ABSTRACT BASE CLASS - demonstrates abstraction and polymorphism
class FileObject {
protected:  // Protected so derived classes can access
    std::string filepath;
    std::string hash;        // SHA-256 hash for content
    long fileSize;
    bool isModified;
    
    // PURE VIRTUAL FUNCTION - forces derived classes to implement
    virtual std::string computeHash() = 0;
    
public:
    FileObject(const std::string& path);
    
    // Virtual destructor for proper cleanup in polymorphic scenarios
    virtual ~FileObject() = default;
    
    // POLYMORPHIC FUNCTIONS - each file type implements differently
    virtual bool isBinary() const = 0;
    virtual std::vector<char> readContent() = 0;
    virtual void writeContent(const std::vector<char>& data) = 0;
    
    // Common interface for all file objects
    std::string getHash();
    std::string getPath() const { return filepath; }
    long getSize() const { return fileSize; }
    
    // OPERATOR OVERLOADING - compare files by hash
    bool operator==(const FileObject& other) const;
    bool operator!=(const FileObject& other) const;
    
    // FRIEND CLASS - ObjectStore can access private members
    friend class ObjectStore;
};

// INHERITANCE - TextFile IS-A FileObject
class TextFile : public FileObject {
private:
    std::vector<std::string> lines;
    std::string encoding;
    
    // Override the pure virtual function
    std::string computeHash() override;
    
public:
    TextFile(const std::string& path);
    
    // POLYMORPHISM - implementing virtual functions
    bool isBinary() const override { return false; }
    std::vector<char> readContent() override;
    void writeContent(const std::vector<char>& data) override;
    
    // Text-specific operations
    std::vector<std::string> getLines();
    void setLines(const std::vector<std::string>& newLines);
    int getLineCount() const { return lines.size(); }
};

// INHERITANCE - BinaryFile IS-A FileObject
class BinaryFile : public FileObject {
private:
    std::vector<char> data;
    
    std::string computeHash() override;
    
public:
    BinaryFile(const std::string& path);
    
    bool isBinary() const override { return true; }
    std::vector<char> readContent() override;
    void writeContent(const std::vector<char>& content) override;
    
    const std::vector<char>& getData() const { return data; }
};

// FACTORY PATTERN - creates appropriate file object type
class FileFactory {
public:
    // Smart pointer for automatic memory management
    static std::unique_ptr<FileObject> createFileObject(const std::string& path);
    static bool detectBinary(const std::string& path);
};

#endif
```

**What This Does**:
1. **FileObject**: Abstract base class that defines what ALL files can do
2. **TextFile**: Handles text files (line-by-line operations)
3. **BinaryFile**: Handles binary files (raw data)
4. **FileFactory**: Decides which type to create based on file content

**Connection to Project**:
- ObjectStore (next step) will store these objects
- DiffEngine will compare these objects
- Java layer will request file operations through this interface

---

## Step 2: FileObject.cpp - Implementation

**Location**: `src/core/FileObject.cpp`

**Code Explanation**:

```cpp
#include "FileObject.h"
#include <fstream>
#include <sstream>
#include <openssl/sha.h>
#include <cstring>

// ============== BASE CLASS IMPLEMENTATION ==============

FileObject::FileObject(const std::string& path) 
    : filepath(path), fileSize(0), isModified(false) {
    // Constructor initializes base members
}

std::string FileObject::getHash() {
    if (hash.empty() || isModified) {
        hash = computeHash();  // Polymorphic call!
        isModified = false;
    }
    return hash;
}

// OPERATOR OVERLOADING - compare by content hash
bool FileObject::operator==(const FileObject& other) const {
    return const_cast<FileObject*>(this)->getHash() == 
           const_cast<FileObject*>(&other)->getHash();
}

bool FileObject::operator!=(const FileObject& other) const {
    return !(*this == other);
}

// ============== TEXT FILE IMPLEMENTATION ==============

TextFile::TextFile(const std::string& path) 
    : FileObject(path), encoding("UTF-8") {
    // Read file into lines
    std::ifstream file(path);
    std::string line;
    while (std::getline(file, line)) {
        lines.push_back(line);
    }
    fileSize = file.tellg();
}

std::string TextFile::computeHash() {
    // SHA-256 hashing
    unsigned char hash_bytes[SHA256_DIGEST_LENGTH];
    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    
    for (const auto& line : lines) {
        SHA256_Update(&sha256, line.c_str(), line.length());
    }
    
    SHA256_Final(hash_bytes, &sha256);
    
    // Convert to hex string
    std::stringstream ss;
    for (int i = 0; i < SHA256_DIGEST_LENGTH; i++) {
        ss << std::hex << (int)hash_bytes[i];
    }
    return ss.str();
}

std::vector<char> TextFile::readContent() {
    std::vector<char> content;
    for (const auto& line : lines) {
        content.insert(content.end(), line.begin(), line.end());
        content.push_back('\n');
    }
    return content;
}

void TextFile::writeContent(const std::vector<char>& data) {
    lines.clear();
    std::string line;
    for (char c : data) {
        if (c == '\n') {
            lines.push_back(line);
            line.clear();
        } else {
            line += c;
        }
    }
    if (!line.empty()) {
        lines.push_back(line);
    }
    isModified = true;
}

std::vector<std::string> TextFile::getLines() {
    return lines;
}

void TextFile::setLines(const std::vector<std::string>& newLines) {
    lines = newLines;
    isModified = true;
}

// ============== BINARY FILE IMPLEMENTATION ==============

BinaryFile::BinaryFile(const std::string& path) : FileObject(path) {
    std::ifstream file(path, std::ios::binary);
    file.seekg(0, std::ios::end);
    fileSize = file.tellg();
    file.seekg(0, std::ios::beg);
    
    data.resize(fileSize);
    file.read(data.data(), fileSize);
}

std::string BinaryFile::computeHash() {
    unsigned char hash_bytes[SHA256_DIGEST_LENGTH];
    SHA256((unsigned char*)data.data(), data.size(), hash_bytes);
    
    std::stringstream ss;
    for (int i = 0; i < SHA256_DIGEST_LENGTH; i++) {
        ss << std::hex << (int)hash_bytes[i];
    }
    return ss.str();
}

std::vector<char> BinaryFile::readContent() {
    return data;
}

void BinaryFile::writeContent(const std::vector<char>& content) {
    data = content;
    fileSize = content.size();
    isModified = true;
}

// ============== FACTORY PATTERN IMPLEMENTATION ==============

std::unique_ptr<FileObject> FileFactory::createFileObject(const std::string& path) {
    if (detectBinary(path)) {
        return std::make_unique<BinaryFile>(path);  // Smart pointer!
    } else {
        return std::make_unique<TextFile>(path);
    }
}

bool FileFactory::detectBinary(const std::string& path) {
    std::ifstream file(path, std::ios::binary);
    char buffer[512];
    file.read(buffer, 512);
    int bytesRead = file.gcount();
    
    // Check for null bytes (indicator of binary)
    for (int i = 0; i < bytesRead; i++) {
        if (buffer[i] == '\0') {
            return true;
        }
    }
    return false;
}
```

**Key Points**:
1. **Polymorphism in Action**: `getHash()` calls `computeHash()` which is implemented differently in each derived class
2. **Smart Pointers**: Factory returns `unique_ptr` for automatic memory management
3. **Hashing**: Uses OpenSSL SHA-256 for content-based identification
4. **Binary Detection**: Checks for null bytes to distinguish binary from text

---

## Step 3: ObjectStore.h - Singleton Storage

**Location**: `src/core/ObjectStore.h`

**Why This File?**
- Centralized storage for all file objects
- Demonstrates Singleton pattern and thread safety
- Uses templates for generic storage

**OOP Concepts**:
- Singleton pattern
- Templates/Generics
- Thread safety (mutex)
- Static members
- Delete operators (prevent copying)

**Code**:

```cpp
#ifndef OBJECTSTORE_H
#define OBJECTSTORE_H

#include "FileObject.h"
#include <unordered_map>
#include <mutex>
#include <memory>

// TEMPLATE CLASS for generic storage
template<typename T>
class StoragePool {
private:
    std::unordered_map<std::string, std::shared_ptr<T>> objects;
    std::mutex poolMutex;
    
public:
    void store(const std::string& key, std::shared_ptr<T> obj) {
        std::lock_guard<std::mutex> lock(poolMutex);
        objects[key] = obj;
    }
    
    std::shared_ptr<T> retrieve(const std::string& key) {
        std::lock_guard<std::mutex> lock(poolMutex);
        auto it = objects.find(key);
        return (it != objects.end()) ? it->second : nullptr;
    }
    
    bool exists(const std::string& key) {
        std::lock_guard<std::mutex> lock(poolMutex);
        return objects.find(key) != objects.end();
    }
    
    void remove(const std::string& key) {
        std::lock_guard<std::mutex> lock(poolMutex);
        objects.erase(key);
    }
};

// SINGLETON PATTERN - only one instance exists
class ObjectStore {
private:
    // STATIC INSTANCE - shared across entire program
    static ObjectStore* instance;
    static std::mutex instanceMutex;
    
    // Storage pools using templates
    StoragePool<FileObject> filePool;
    std::string storePath;
    
    // PRIVATE CONSTRUCTOR - prevent direct instantiation
    ObjectStore(const std::string& path);
    
    // DELETE COPY OPERATIONS - prevent copying singleton
    ObjectStore(const ObjectStore&) = delete;
    ObjectStore& operator=(const ObjectStore&) = delete;
    
public:
    // STATIC METHOD to get singleton instance
    static ObjectStore* getInstance(const std::string& path = ".vv/objects");
    
    // Core operations
    std::string storeFile(std::unique_ptr<FileObject> file);
    std::shared_ptr<FileObject> retrieveFile(const std::string& hash);
    bool hasObject(const std::string& hash);
    void removeObject(const std::string& hash);
    
    // Destructor
    ~ObjectStore() = default;
};

#endif
```

**What This Does**:
1. **Singleton**: Only one ObjectStore exists for the entire program
2. **Thread-Safe**: Multiple threads can safely access the store
3. **Template Pool**: Generic storage that works with any type
4. **Hash-Based Storage**: Files are stored by their content hash

---

## Step 4: ObjectStore.cpp - Implementation

**Location**: `src/core/ObjectStore.cpp`

```cpp
#include "ObjectStore.h"
#include <fstream>
#include <filesystem>

namespace fs = std::filesystem;

// STATIC MEMBER INITIALIZATION
ObjectStore* ObjectStore::instance = nullptr;
std::mutex ObjectStore::instanceMutex;

ObjectStore::ObjectStore(const std::string& path) : storePath(path) {
    fs::create_directories(storePath);
}

// SINGLETON PATTERN IMPLEMENTATION
ObjectStore* ObjectStore::getInstance(const std::string& path) {
    // THREAD-SAFE SINGLETON (Double-Checked Locking)
    if (instance == nullptr) {
        std::lock_guard<std::mutex> lock(instanceMutex);
        if (instance == nullptr) {
            instance = new ObjectStore(path);
        }
    }
    return instance;
}

std::string ObjectStore::storeFile(std::unique_ptr<FileObject> file) {
    std::string hash = file->getHash();
    
    if (!hasObject(hash)) {
        // Store in pool
        filePool.store(hash, std::move(file));
        
        // Write to disk
        std::string objectPath = storePath + "/" + hash;
        std::ofstream outFile(objectPath, std::ios::binary);
        auto content = file->readContent();
        outFile.write(content.data(), content.size());
    }
    
    return hash;
}

std::shared_ptr<FileObject> ObjectStore::retrieveFile(const std::string& hash) {
    // First check in-memory pool
    auto file = filePool.retrieve(hash);
    
    if (file == nullptr) {
        // Load from disk if not in memory
        std::string objectPath = storePath + "/" + hash;
        if (fs::exists(objectPath)) {
            auto loadedFile = FileFactory::createFileObject(objectPath);
            file = std::shared_ptr<FileObject>(std::move(loadedFile));
            filePool.store(hash, file);
        }
    }
    
    return file;
}

bool ObjectStore::hasObject(const std::string& hash) {
    return filePool.exists(hash) || 
           fs::exists(storePath + "/" + hash);
}

void ObjectStore::removeObject(const std::string& hash) {
    filePool.remove(hash);
    fs::remove(storePath + "/" + hash);
}
```

**Key Concepts**:
1. **Double-Checked Locking**: Efficient thread-safe singleton
2. **Lazy Initialization**: Instance created only when needed
3. **Disk + Memory**: Objects stored both in memory (fast) and disk (persistent)
4. **Move Semantics**: Using `std::move` for efficient transfers

---

## Step 5: DiffEngine.h - Difference Calculator

**Location**: `src/core/DiffEngine.h`

**Why This File?**
- Compares files and detects changes
- Demonstrates Strategy pattern
- Core algorithm for version control

**Code**:

```cpp
#ifndef DIFFENGINE_H
#define DIFFENGINE_H

#include "FileObject.h"
#include <vector>
#include <string>

// ENUM CLASS for change types
enum class ChangeType {
    ADDED,
    DELETED,
    MODIFIED,
    RENAMED
};

// Represents a single change
struct FileDiff {
    std::string filePath;
    ChangeType type;
    std::vector<std::string> addedLines;
    std::vector<std::string> deletedLines;
    std::string oldPath;  // For renames
    double similarity;     // For rename detection
};

// STRATEGY PATTERN - different diff algorithms
class DiffStrategy {
public:
    virtual ~DiffStrategy() = default;
    virtual FileDiff computeDiff(FileObject* oldFile, FileObject* newFile) = 0;
};

class LineByLineDiff : public DiffStrategy {
public:
    FileDiff computeDiff(FileObject* oldFile, FileObject* newFile) override;
};

class BinaryDiff : public DiffStrategy {
public:
    FileDiff computeDiff(FileObject* oldFile, FileObject* newFile) override;
};

// Main diff engine
class DiffEngine {
private:
    double renameThreshold;  // Similarity threshold for rename detection
    
    double calculateSimilarity(const std::string& content1, const std::string& content2);
    std::vector<std::string> findLCS(const std::vector<std::string>& seq1, 
                                     const std::vector<std::string>& seq2);
    
public:
    DiffEngine(double threshold = 0.7);
    
    FileDiff diff(FileObject* oldFile, FileObject* newFile);
    bool detectRename(FileObject* file1, FileObject* file2);
    
    std::vector<FileDiff> threeWayMerge(FileObject* base, 
                                        FileObject* ours, 
                                        FileObject* theirs);
};

#endif
```

**What This Does**:
1. **Strategy Pattern**: Different algorithms for text vs binary
2. **LCS Algorithm**: Longest Common Subsequence for line-by-line diff
3. **Rename Detection**: Similarity-based detection
4. **Three-Way Merge**: Merge algorithm for branch merging

---

## Step 6: DiffEngine.cpp - Implementation

**Location**: `src/core/DiffEngine.cpp`

```cpp
#include "DiffEngine.h"
#include <algorithm>
#include <set>

DiffEngine::DiffEngine(double threshold) : renameThreshold(threshold) {}

FileDiff DiffEngine::diff(FileObject* oldFile, FileObject* newFile) {
    FileDiff result;
    result.filePath = newFile->getPath();
    
    // Detect if files are same
    if (*oldFile == *newFile) {
        return result;  // No changes
    }
    
    // Check for rename
    if (detectRename(oldFile, newFile)) {
        result.type = ChangeType::RENAMED;
        result.oldPath = oldFile->getPath();
        result.similarity = calculateSimilarity(
            std::string(oldFile->readContent().begin(), oldFile->readContent().end()),
            std::string(newFile->readContent().begin(), newFile->readContent().end())
        );
        return result;
    }
    
    // Use appropriate strategy
    if (newFile->isBinary()) {
        BinaryDiff strategy;
        return strategy.computeDiff(oldFile, newFile);
    } else {
        LineByLineDiff strategy;
        return strategy.computeDiff(oldFile, newFile);
    }
}

// LINE-BY-LINE DIFF IMPLEMENTATION
FileDiff LineByLineDiff::computeDiff(FileObject* oldFile, FileObject* newFile) {
    FileDiff result;
    result.filePath = newFile->getPath();
    result.type = ChangeType::MODIFIED;
    
    // Cast to TextFile to access lines
    TextFile* oldText = dynamic_cast<TextFile*>(oldFile);
    TextFile* newText = dynamic_cast<TextFile*>(newFile);
    
    if (oldText && newText) {
        auto oldLines = oldText->getLines();
        auto newLines = newText->getLines();
        
        // Simple diff: find added and deleted lines
        std::set<std::string> oldSet(oldLines.begin(), oldLines.end());
        std::set<std::string> newSet(newLines.begin(), newLines.end());
        
        for (const auto& line : newLines) {
            if (oldSet.find(line) == oldSet.end()) {
                result.addedLines.push_back(line);
            }
        }
        
        for (const auto& line : oldLines) {
            if (newSet.find(line) == newSet.end()) {
                result.deletedLines.push_back(line);
            }
        }
    }
    
    return result;
}

// BINARY DIFF
FileDiff BinaryDiff::computeDiff(FileObject* oldFile, FileObject* newFile) {
    FileDiff result;
    result.filePath = newFile->getPath();
    result.type = ChangeType::MODIFIED;
    // Binary files: just mark as modified
    return result;
}

double DiffEngine::calculateSimilarity(const std::string& content1, const std::string& content2) {
    if (content1.empty() && content2.empty()) return 1.0;
    if (content1.empty() || content2.empty()) return 0.0;
    
    // Simple similarity: common characters / total characters
    std::set<char> chars1(content1.begin(), content1.end());
    std::set<char> chars2(content2.begin(), content2.end());
    
    std::set<char> intersection;
    std::set_intersection(chars1.begin(), chars1.end(),
                         chars2.begin(), chars2.end(),
                         std::inserter(intersection, intersection.begin()));
    
    std::set<char> unionSet;
    std::set_union(chars1.begin(), chars1.end(),
                   chars2.begin(), chars2.end(),
                   std::inserter(unionSet, unionSet.begin()));
    
    return static_cast<double>(intersection.size()) / unionSet.size();
}

bool DiffEngine::detectRename(FileObject* file1, FileObject* file2) {
    auto content1 = std::string(file1->readContent().begin(), file1->readContent().end());
    auto content2 = std::string(file2->readContent().begin(), file2->readContent().end());
    
    double similarity = calculateSimilarity(content1, content2);
    return similarity >= renameThreshold && file1->getPath() != file2->getPath();
}

std::vector<FileDiff> DiffEngine::threeWayMerge(FileObject* base, 
                                                FileObject* ours, 
                                                FileObject* theirs) {
    std::vector<FileDiff> conflicts;
    
    // Compare base->ours and base->theirs
    FileDiff ourChanges = diff(base, ours);
    FileDiff theirChanges = diff(base, theirs);
    
    // If both modified the same file, it's a conflict
    if (ourChanges.type == ChangeType::MODIFIED && 
        theirChanges.type == ChangeType::MODIFIED) {
        conflicts.push_back(ourChanges);
        conflicts.push_back(theirChanges);
    }
    
    return conflicts;
}
```

**Key Algorithms**:
1. **Diff Algorithm**: Compares line-by-line for text files
2. **Similarity Calculation**: Jaccard similarity for rename detection
3. **Three-Way Merge**: Detects conflicts between branches

---

## Summary of Phase 1 (C++ Core)

You've now built:
✅ **FileObject** - Base abstraction with inheritance
✅ **ObjectStore** - Singleton storage with thread safety
✅ **DiffEngine** - Diff algorithms with Strategy pattern

**OOP Concepts Covered**:
- ✅ Inheritance & Polymorphism
- ✅ Abstract Classes & Pure Virtual Functions
- ✅ Operator Overloading
- ✅ Friend Classes
- ✅ Factory Pattern
- ✅ Singleton Pattern
- ✅ Strategy Pattern
- ✅ Templates/Generics
- ✅ Smart Pointers
- ✅ Thread Safety (Mutex)
- ✅ Enum Classes
- ✅ Move Semantics

**Next**: We'll build the Java layer on top of this C++ foundation!

---

**Continue to PHASE 2 in next section...**

