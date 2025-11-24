#ifndef FILEOBJECT_H
#define FILEOBJECT_H

#include <string>
#include <vector>
#include <memory>
#include <fstream>

class FileObject {
protected:
    std::string filepath;
    std::string hash;
    long fileSize;
    bool isModified;
    
    virtual std::string computeHash() = 0;
    
public:
    FileObject(const std::string& path);
    virtual ~FileObject() = default;
    
    virtual bool isBinary() const = 0;
    virtual std::vector<char> readContent() = 0;
    virtual void writeContent(const std::vector<char>& data) = 0;
    
    std::string getHash();
    std::string getPath() const { return filepath; }
    long getSize() const { return fileSize; }
    
    bool operator==(const FileObject& other) const;
    bool operator!=(const FileObject& other) const;
    
    friend class ObjectStore;
};

class TextFile : public FileObject {
private:
    std::vector<std::string> lines;
    std::string encoding;
    
    std::string computeHash() override;
    
public:
    TextFile(const std::string& path);
    
    bool isBinary() const override { return false; }
    std::vector<char> readContent() override;
    void writeContent(const std::vector<char>& data) override;
    
    std::vector<std::string> getLines();
    void setLines(const std::vector<std::string>& newLines);
    int getLineCount() const { return lines.size(); }
};

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

class FileFactory {
public:
    static std::unique_ptr<FileObject> createFileObject(const std::string& path);
    static bool detectBinary(const std::string& path);
};

#endif
