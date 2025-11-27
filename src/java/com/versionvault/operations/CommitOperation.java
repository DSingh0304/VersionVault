package com.versionvault.operations;

import com.versionvault.core.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CommitOperation extends VaultOperation {
    private String message;
    private User author;

    public CommitOperation(Repository repo, String message, User author) {
        super(repo);
        this.message = message;
        this.author = author;
    }

    @Override
    public void execute() throws OperationException {
        validate();

        StagingArea staging = repository.getStagingArea();
        if (staging.isEmpty()) {
            throw new OperationException("Nothing to commit");
        }

        Commit commit = new Commit(message, author);

        Branch currentBranch = repository.getBranchManager().getCurrentBranch();
        if (currentBranch != null && currentBranch.getCurrentCommit() != null) {
            String parentHash = currentBranch.getCurrentCommit();
            commit.addParent(parentHash);

            // Copy files from parent commit
            Commit parent = repository.getCommitHistory().getCommit(parentHash);
            if (parent != null) {
                Map<String, String> parentFiles = parent.getFileHashes();
                for (Map.Entry<String, String> entry : parentFiles.entrySet()) {
                    commit.addFile(entry.getKey(), entry.getValue());
                }
            }
        }

        for (Map.Entry<String, StagedFile> entry : staging.getStagedFiles().entrySet()) {
            String path = entry.getKey();
            String fileHash = hashFile(path);
            commit.addFile(path, fileHash);
        }

        for (String removedPath : staging.getRemovedFiles()) {
            commit.removeFile(removedPath);
        }

        commit.calculateHash();

        repository.getCommitHistory().addCommit(commit);
        repository.getBranchManager().updateCurrentBranchCommit(commit.getHash());

        staging.clear();

        result.setSuccess(true);
        log("Created commit " + commit.getHash().substring(0, 8));
    }

    private String hashFile(String path) throws OperationException {
        try {
            return repository.getObjectStore().storeBlob(Paths.get(repository.getRootPath(), path));
        } catch (IOException e) {
            throw new OperationException("Failed to store file: " + path + " - " + e.getMessage());
        }
    }
}
