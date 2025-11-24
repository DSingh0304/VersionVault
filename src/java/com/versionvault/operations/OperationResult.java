package com.versionvault.operations;

import java.util.*;

public class OperationResult {
    private boolean success;
    private List<String> messages;
    
    public OperationResult() {
        this.success = false;
        this.messages = new ArrayList<>();
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void addMessage(String message) {
        messages.add(message);
    }
    
    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }
}
