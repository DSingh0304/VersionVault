#include "FileObject.h"
#include <openssl/sha.h>
#include <sstream>
#include <iomanip>
#include <stdexcept>

FileObject::FileObject(const std::string& path) 
    : filepath(path), fileSize(0), isModified(false) {
}

std::string FileObject::getHash() {
    if (hash.empty() || isModified) {
        hash = computeHash();
        isModified = false;
    }
    return hash;
}

bool FileObject::operator==(const FileObject& other) const {
    return const_cast<FileObject*>(this)->getHash() == 
           const_cast<FileObject&>(other).getHash();
}

bool FileObject::operator!=(const FileObject& other) const {
    return !(*this == other);
}

TextFile::TextFile(const std::string& path) 
    : FileObject(path), encoding("UTF-8") {
}

std::string TextFile::computeHash() {
    std::stringstream ss;
    for (const auto& line : lines) {
        ss << line << "\n";
    }
    
    std::string content = ss.str();
    unsigned char digest[SHA256_DIGEST_LENGTH];
    SHA256(reinterpret_cast<const unsigned char*>(content.c_str()), 
           content.length(), digest);
    
    std::stringstream hashStream;
    for (int i = 0; i < SHA256_DIGEST_LENGTH; i++) {
        hashStream << std::hex << std::setw(2) << std::setfill('0') 
                   << static_cast<int>(digest[i]);
    }
    
    return hashStream.str();
}

std::vector<char> TextFile::readContent() {
    std::ifstream file(filepath);
    if (!file.is_open()) {
        throw std::runtime_error("Cannot open file: " + filepath);
    }
    
    lines.clear();
    std::string line;
    while (std::getline(file, line)) {
        lines.push_back(line);
    }
    file.close();
    
    std::stringstream ss;
    for (const auto& l : lines) {
        ss << l << "\n";
    }
    
    std::string content = ss.str();
    return std::vector<char>(content.begin(), content.end());
}

void TextFile::writeContent(const std::vector<char>& data) {
    std::ofstream file(filepath);
    if (!file.is_open()) {
        throw std::runtime_error("Cannot write to file: " + filepath);
    }
    
    file.write(data.data(), data.size());
    file.close();
    isModified = true;
}

std::vector<std::string> TextFile::getLines() {
    if (lines.empty()) {
        readContent();
    }
    return lines;
}

void TextFile::setLines(const std::vector<std::string>& newLines) {
    lines = newLines;
    isModified = true;
}

BinaryFile::BinaryFile(const std::string& path) 
    : FileObject(path) {
}

std::string BinaryFile::computeHash() {
    unsigned char digest[SHA256_DIGEST_LENGTH];
    SHA256(reinterpret_cast<const unsigned char*>(data.data()), 
           data.size(), digest);
    
    std::stringstream hashStream;
    for (int i = 0; i < SHA256_DIGEST_LENGTH; i++) {
        hashStream << std::hex << std::setw(2) << std::setfill('0') 
                   << static_cast<int>(digest[i]);
    }
    
    return hashStream.str();
}

std::vector<char> BinaryFile::readContent() {
    std::ifstream file(filepath, std::ios::binary | std::ios::ate);
    if (!file.is_open()) {
        throw std::runtime_error("Cannot open file: " + filepath);
    }
    
    fileSize = file.tellg();
    file.seekg(0, std::ios::beg);
    
    data.resize(fileSize);
    file.read(data.data(), fileSize);
    file.close();
    
    return data;
}

void BinaryFile::writeContent(const std::vector<char>& content) {
    std::ofstream file(filepath, std::ios::binary);
    if (!file.is_open()) {
        throw std::runtime_error("Cannot write to file: " + filepath);
    }
    
    file.write(content.data(), content.size());
    file.close();
    data = content;
    isModified = true;
}

std::unique_ptr<FileObject> FileFactory::createFileObject(const std::string& path) {
    if (detectBinary(path)) {
        return std::make_unique<BinaryFile>(path);
    }
    return std::make_unique<TextFile>(path);
}

bool FileFactory::detectBinary(const std::string& path) {
    std::ifstream file(path, std::ios::binary);
    if (!file.is_open()) {
        return false;
    }
    
    char buffer[512];
    file.read(buffer, sizeof(buffer));
    std::streamsize bytesRead = file.gcount();
    file.close();
    
    for (std::streamsize i = 0; i < bytesRead; i++) {
        if (buffer[i] == 0) {
            return true;
        }
    }
    
    return false;
}
