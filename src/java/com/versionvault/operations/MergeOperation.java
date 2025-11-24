package com.versionvault.operations;

import com.versionvault.core.*;
import com.versionvault.merge.*;
import java.util.*;

public class MergeOperation extends VaultOperation {
    private String sourceBranch;
    private MergeStrategy strategy;
    
    public MergeOperation(Repository repo, String sourceBranch) {
        super(repo);
        this.sourceBranch = sourceBranch;
        this.strategy = null;
    }
    
    public void setStrategy(MergeStrategy strategy) {
        this.strategy = strategy;
    }
    
    @Override
    public void execute() throws OperationException {
        validate();
        
        Branch current = repository.getBranchManager().getCurrentBranch();
        Branch source = repository.getBranchManager().getBranch(sourceBranch);
        
        if (source == null) {
            throw new OperationException("Branch not found: " + sourceBranch);
        }
        
        if (current.getCurrentCommit().equals(source.getCurrentCommit())) {
            log("Already up to date");
            result.setSuccess(true);
            return;
        }
        
        Commit baseCommit = repository.getCommitHistory()
            .findCommonAncestor(current.getCurrentCommit(), source.getCurrentCommit());
        
        if (baseCommit == null) {
            throw new OperationException("No common ancestor found");
        }
        
        performMerge(baseCommit, current, source);
    }
    
    private void performMerge(Commit base, Branch current, Branch source) throws OperationException {
        Commit currentCommit = repository.getCommitHistory().getCommit(current.getCurrentCommit());
        Commit sourceCommit = repository.getCommitHistory().getCommit(source.getCurrentCommit());
        
        Map<String, String> baseFiles = base.getFileHashes();
        Map<String, String> currentFiles = currentCommit.getFileHashes();
        Map<String, String> sourceFiles = sourceCommit.getFileHashes();
        
        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(baseFiles.keySet());
        allFiles.addAll(currentFiles.keySet());
        allFiles.addAll(sourceFiles.keySet());
        
        boolean hasConflicts = false;
        
        for (String file : allFiles) {
            String baseHash = baseFiles.get(file);
            String currentHash = currentFiles.get(file);
            String sourceHash = sourceFiles.get(file);
            
            if (Objects.equals(currentHash, sourceHash)) {
                continue;
            }
            
            if (baseHash == null) {
                if (currentHash != null && sourceHash != null) {
                    hasConflicts = true;
                }
            } else if (!Objects.equals(baseHash, currentHash) && !Objects.equals(baseHash, sourceHash)) {
                hasConflicts = true;
            }
        }
        
        if (hasConflicts) {
            result.setSuccess(false);
            log("Merge conflicts detected");
        } else {
            result.setSuccess(true);
            log("Merge successful");
        }
    }
}
