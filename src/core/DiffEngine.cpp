#include "DiffEngine.h"
#include <algorithm>
#include <cmath>

DiffEngine::DiffEngine() 
    : algorithm(new SimpleDiff()), showContext(true), contextLines(3) {
}

DiffEngine::~DiffEngine() {
    delete algorithm;
}

void DiffEngine::setAlgorithm(DiffAlgorithm* algo) {
    if (algorithm) {
        delete algorithm;
    }
    algorithm = algo;
}

Change DiffEngine::compareFiles(FileObject* oldFile, FileObject* newFile) {
    if (oldFile == nullptr && newFile == nullptr) {
        return Change(ChangeType::UNCHANGED, "");
    }
    
    if (oldFile == nullptr) {
        return Change(ChangeType::ADDED, newFile->getPath(), "", newFile->getHash());
    }
    
    if (newFile == nullptr) {
        return Change(ChangeType::REMOVED, oldFile->getPath(), oldFile->getHash(), "");
    }
    
    std::string oldHash = oldFile->getHash();
    std::string newHash = newFile->getHash();
    
    if (oldHash == newHash) {
        return Change(ChangeType::UNCHANGED, oldFile->getPath(), oldHash, newHash);
    }
    
    return Change(ChangeType::MODIFIED, oldFile->getPath(), oldHash, newHash);
}

std::vector<std::string> DiffEngine::generateUnifiedDiff(TextFile* oldFile, TextFile* newFile) {
    if (!oldFile || !newFile) {
        return {};
    }
    
    auto oldLines = oldFile->getLines();
    auto newLines = newFile->getLines();
    
    return algorithm->computeDiff(oldLines, newLines);
}

double DiffEngine::calculateSimilarity(const std::string& text1, const std::string& text2) {
    int m = text1.length();
    int n = text2.length();
    
    if (m == 0 && n == 0) return 1.0;
    if (m == 0 || n == 0) return 0.0;
    
    std::vector<std::vector<int>> dp(m + 1, std::vector<int>(n + 1, 0));
    
    for (int i = 0; i <= m; i++) dp[i][0] = i;
    for (int j = 0; j <= n; j++) dp[0][j] = j;
    
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (text1[i-1] == text2[j-1]) {
                dp[i][j] = dp[i-1][j-1];
            } else {
                dp[i][j] = 1 + std::min({dp[i-1][j], dp[i][j-1], dp[i-1][j-1]});
            }
        }
    }
    
    int distance = dp[m][n];
    int maxLen = std::max(m, n);
    
    return 1.0 - (static_cast<double>(distance) / maxLen);
}

bool DiffEngine::areFilesSimilar(TextFile* file1, TextFile* file2, double threshold) {
    auto lines1 = file1->getLines();
    auto lines2 = file2->getLines();
    
    std::string text1, text2;
    for (const auto& line : lines1) text1 += line + "\n";
    for (const auto& line : lines2) text2 += line + "\n";
    
    return calculateSimilarity(text1, text2) >= threshold;
}

std::vector<std::string> SimpleDiff::computeDiff(
    const std::vector<std::string>& oldLines,
    const std::vector<std::string>& newLines) {
    
    std::vector<std::string> result;
    
    result.push_back("--- old");
    result.push_back("+++ new");
    
    size_t i = 0, j = 0;
    
    while (i < oldLines.size() || j < newLines.size()) {
        if (i < oldLines.size() && j < newLines.size() && oldLines[i] == newLines[j]) {
            result.push_back(" " + oldLines[i]);
            i++;
            j++;
        } else if (j >= newLines.size() || (i < oldLines.size() && oldLines[i] < newLines[j])) {
            result.push_back("-" + oldLines[i]);
            i++;
        } else {
            result.push_back("+" + newLines[j]);
            j++;
        }
    }
    
    return result;
}

std::vector<MyersDiff::Snake> MyersDiff::findSnakes(
    const std::vector<std::string>& a,
    const std::vector<std::string>& b) {
    
    std::vector<Snake> snakes;
    return snakes;
}

std::vector<std::string> MyersDiff::computeDiff(
    const std::vector<std::string>& oldLines,
    const std::vector<std::string>& newLines) {
    
    SimpleDiff simpleDiff;
    return simpleDiff.computeDiff(oldLines, newLines);
}
