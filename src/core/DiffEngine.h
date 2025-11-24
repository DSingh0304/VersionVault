#ifndef DIFFENGINE_H
#define DIFFENGINE_H

#include "FileObject.h"
#include <vector>
#include <string>

enum class ChangeType {
    ADDED,
    REMOVED,
    MODIFIED,
    UNCHANGED
};

struct Change {
    ChangeType type;
    std::string path;
    std::string oldHash;
    std::string newHash;
    
    Change(ChangeType t, const std::string& p) 
        : type(t), path(p) {}
    
    Change(ChangeType t, const std::string& p, const std::string& oh, const std::string& nh)
        : type(t), path(p), oldHash(oh), newHash(nh) {}
};

class DiffAlgorithm {
public:
    virtual ~DiffAlgorithm() = default;
    virtual std::vector<std::string> computeDiff(
        const std::vector<std::string>& oldLines,
        const std::vector<std::string>& newLines
    ) = 0;
};

class MyersDiff : public DiffAlgorithm {
private:
    struct Snake {
        int x, y, length;
    };
    
    std::vector<Snake> findSnakes(
        const std::vector<std::string>& a,
        const std::vector<std::string>& b
    );
    
public:
    std::vector<std::string> computeDiff(
        const std::vector<std::string>& oldLines,
        const std::vector<std::string>& newLines
    ) override;
};

class SimpleDiff : public DiffAlgorithm {
public:
    std::vector<std::string> computeDiff(
        const std::vector<std::string>& oldLines,
        const std::vector<std::string>& newLines
    ) override;
};

class DiffEngine {
private:
    DiffAlgorithm* algorithm;
    bool showContext;
    int contextLines;
    
public:
    DiffEngine();
    ~DiffEngine();
    
    void setAlgorithm(DiffAlgorithm* algo);
    void setContextLines(int lines) { contextLines = lines; }
    
    Change compareFiles(FileObject* oldFile, FileObject* newFile);
    std::vector<std::string> generateUnifiedDiff(TextFile* oldFile, TextFile* newFile);
    
    double calculateSimilarity(const std::string& text1, const std::string& text2);
    bool areFilesSimilar(TextFile* file1, TextFile* file2, double threshold = 0.6);
};

#endif
