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
            commit.addParent(currentBranch.getCurrentCommit());
        }
        
        for (Map.Entry<String, StagedFile> entry : staging.getStagedFiles().entrySet()) {
            String path = entry.getKey();
            String fileHash = hashFile(path);
            commit.addFile(path, fileHash);
        }
        
        commit.calculateHash();
        
        repository.getCommitHistory().addCommit(commit);
        repository.getBranchManager().updateCurrentBranchCommit(commit.getHash());
        
        staging.clear();
        
        result.setSuccess(true);
        log("Created commit " + commit.getHash().substring(0, 8));
    }
    
    private String hashFile(String path) {
        return "temp_hash_" + path.hashCode();
    }
}
