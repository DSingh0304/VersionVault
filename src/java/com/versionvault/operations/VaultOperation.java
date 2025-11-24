package com.versionvault.operations;

import com.versionvault.core.Repository;

public abstract class VaultOperation {
    protected Repository repository;
    protected OperationResult result;
    
    public VaultOperation(Repository repo) {
        this.repository = repo;
        this.result = new OperationResult();
    }
    
    public abstract void execute() throws OperationException;
    
    public void validate() throws OperationException {
        if (!repository.isInitialized()) {
            throw new OperationException("Repository not initialized");
        }
    }
    
    public OperationResult getResult() {
        return result;
    }
    
    protected void log(String message) {
        result.addMessage(message);
    }
}
