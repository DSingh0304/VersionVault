package com.versionvault.core;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ObjectStore {
    private Repository repository;

    public ObjectStore(Repository repo) {
        this.repository = repo;
    }

    public String storeBlob(Path file) throws IOException {
        byte[] content = Files.readAllBytes(file);
        String hash = calculateHash(content);

        Path objectPath = getObjectPath(hash);

        if (!Files.exists(objectPath)) {
            Files.createDirectories(objectPath.getParent());
            Files.write(objectPath, content);
        }

        return hash;
    }

    public void restoreBlob(String hash, Path targetFile) throws IOException {
        Path objectPath = getObjectPath(hash);

        if (!Files.exists(objectPath)) {
            throw new IOException("Object not found: " + hash);
        }

        byte[] content = Files.readAllBytes(objectPath);
        Files.createDirectories(targetFile.getParent());
        Files.write(targetFile, content);
    }

    private Path getObjectPath(String hash) {
        return Paths.get(repository.getVVPath(), "objects", hash.substring(0, 2), hash.substring(2));
    }

    private String calculateHash(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(content);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 algorithm not found", e);
        }
    }
}
