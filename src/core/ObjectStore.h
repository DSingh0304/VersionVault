#ifndef OBJECTSTORE_H
#define OBJECTSTORE_H

#include "FileObject.h"
#include <map>
#include <mutex>

template<typename T>
class StoragePool {
private:
    std::map<std::string, T> pool;
    size_t maxSize;
    
public:
    StoragePool(size_t max = 1000) : maxSize(max) {}
    
    void store(const std::string& key, const T& value) {
        if (pool.size() >= maxSize) {
            pool.erase(pool.begin());
        }
        pool[key] = value;
    }
    
    bool retrieve(const std::string& key, T& value) {
        auto it = pool.find(key);
        if (it != pool.end()) {
            value = it->second;
            return true;
        }
        return false;
    }
    
    bool contains(const std::string& key) const {
        return pool.find(key) != pool.end();
    }
    
    void clear() {
        pool.clear();
    }
    
    size_t size() const {
        return pool.size();
    }
};

class ObjectStore {
private:
    static ObjectStore* instance;
    static std::mutex mtx;
    
    std::string storePath;
    StoragePool<std::vector<char>> objectPool;
    std::map<std::string, std::string> hashToPath;
    
    ObjectStore(const std::string& path);
    ObjectStore(const ObjectStore&) = delete;
    ObjectStore& operator=(const ObjectStore&) = delete;
    
    std::string getObjectPath(const std::string& hash);
    
public:
    static ObjectStore* getInstance(const std::string& path = "");
    
    std::string storeObject(FileObject& obj);
    std::unique_ptr<FileObject> retrieveObject(const std::string& hash);
    bool hasObject(const std::string& hash);
    
    void compressObject(const std::string& hash);
    void decompressObject(const std::string& hash);
    
    size_t getStorageSize() const;
    void cleanup(int daysOld);
    
    template<typename Func>
    void forEach(Func&& func) {
        for (const auto& pair : hashToPath) {
            func(pair.first, pair.second);
        }
    }
    
    ~ObjectStore();
};

#endif
