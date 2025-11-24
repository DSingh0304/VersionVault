#include "ObjectStore.h"
#include <filesystem>
#include <fstream>

namespace fs = std::filesystem;

ObjectStore* ObjectStore::instance = nullptr;
std::mutex ObjectStore::mtx;

ObjectStore::ObjectStore(const std::string& path) 
    : storePath(path), objectPool(2000) {
    fs::create_directories(storePath);
}

ObjectStore* ObjectStore::getInstance(const std::string& path) {
    if (instance == nullptr) {
        std::lock_guard<std::mutex> lock(mtx);
        if (instance == nullptr) {
            std::string actualPath = path.empty() ? ".vv/objects" : path;
            instance = new ObjectStore(actualPath);
        }
    }
    return instance;
}

std::string ObjectStore::getObjectPath(const std::string& hash) {
    std::string dir = hash.substr(0, 2);
    std::string file = hash.substr(2);
    return storePath + "/" + dir + "/" + file;
}

std::string ObjectStore::storeObject(FileObject& obj) {
    std::string hash = obj.getHash();
    
    if (hasObject(hash)) {
        return hash;
    }
    
    std::vector<char> content = obj.readContent();
    objectPool.store(hash, content);
    
    std::string objPath = getObjectPath(hash);
    fs::create_directories(fs::path(objPath).parent_path());
    
    std::ofstream outFile(objPath, std::ios::binary);
    if (!outFile.is_open()) {
        throw std::runtime_error("Cannot create object file: " + objPath);
    }
    
    outFile.write(content.data(), content.size());
    outFile.close();
    
    hashToPath[hash] = obj.filepath;
    
    return hash;
}

std::unique_ptr<FileObject> ObjectStore::retrieveObject(const std::string& hash) {
    std::vector<char> content;
    
    if (objectPool.retrieve(hash, content)) {
        auto it = hashToPath.find(hash);
        std::string path = (it != hashToPath.end()) ? it->second : "temp";
        
        auto obj = FileFactory::createFileObject(path);
        obj->writeContent(content);
        return obj;
    }
    
    std::string objPath = getObjectPath(hash);
    if (!fs::exists(objPath)) {
        return nullptr;
    }
    
    std::ifstream inFile(objPath, std::ios::binary | std::ios::ate);
    if (!inFile.is_open()) {
        return nullptr;
    }
    
    size_t size = inFile.tellg();
    inFile.seekg(0, std::ios::beg);
    
    content.resize(size);
    inFile.read(content.data(), size);
    inFile.close();
    
    objectPool.store(hash, content);
    
    auto it = hashToPath.find(hash);
    std::string path = (it != hashToPath.end()) ? it->second : "temp";
    
    auto obj = FileFactory::createFileObject(path);
    obj->writeContent(content);
    return obj;
}

bool ObjectStore::hasObject(const std::string& hash) {
    if (objectPool.contains(hash)) {
        return true;
    }
    
    std::string objPath = getObjectPath(hash);
    return fs::exists(objPath);
}

void ObjectStore::compressObject(const std::string& hash) {
}

void ObjectStore::decompressObject(const std::string& hash) {
}

size_t ObjectStore::getStorageSize() const {
    size_t totalSize = 0;
    
    for (const auto& entry : fs::recursive_directory_iterator(storePath)) {
        if (entry.is_regular_file()) {
            totalSize += entry.file_size();
        }
    }
    
    return totalSize;
}

void ObjectStore::cleanup(int daysOld) {
    auto now = fs::file_time_type::clock::now();
    
    for (const auto& entry : fs::recursive_directory_iterator(storePath)) {
        if (entry.is_regular_file()) {
            auto ftime = fs::last_write_time(entry);
            auto age = std::chrono::duration_cast<std::chrono::hours>(now - ftime).count() / 24;
            
            if (age > daysOld) {
                fs::remove(entry.path());
            }
        }
    }
}

ObjectStore::~ObjectStore() {
}
