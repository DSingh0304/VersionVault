package com.versionvault.merge;

import com.versionvault.core.*;
import java.util.*;

public interface MergeStrategy {
    MergeResult merge(List<String> base, List<String> ours, List<String> theirs);
    boolean hasConflicts();
    List<ConflictRegion> getConflicts();
}

abstract class BaseMergeStrategy implements MergeStrategy {
    protected List<ConflictRegion> conflicts;
    
    public BaseMergeStrategy() {
        this.conflicts = new ArrayList<>();
    }
    
    @Override
    public boolean hasConflicts() {
        return !conflicts.isEmpty();
    }
    
    @Override
    public List<ConflictRegion> getConflicts() {
        return new ArrayList<>(conflicts);
    }
    
    protected abstract List<String> performMerge(List<String> base, List<String> ours, List<String> theirs);
    
    @Override
    public MergeResult merge(List<String> base, List<String> ours, List<String> theirs) {
        conflicts.clear();
        List<String> result = performMerge(base, ours, theirs);
        
        if (hasConflicts()) {
            return new MergeResult(MergeStatus.CONFLICT, result, conflicts);
        }
        
        return new MergeResult(MergeStatus.SUCCESS, result, conflicts);
    }
}

class ThreeWayMerge extends BaseMergeStrategy {
    
    @Override
    protected List<String> performMerge(List<String> base, List<String> ours, List<String> theirs) {
        List<String> result = new ArrayList<>();
        
        if (base == null || base.isEmpty()) {
            return mergeTwoWay(ours, theirs);
        }
        
        int i = 0, j = 0, k = 0;
        int baseSize = base.size();
        int oursSize = ours.size();
        int theirsSize = theirs.size();
        
        while (i < baseSize || j < oursSize || k < theirsSize) {
            String baseLine = i < baseSize ? base.get(i) : null;
            String ourLine = j < oursSize ? ours.get(j) : null;
            String theirLine = k < theirsSize ? theirs.get(k) : null;
            
            if (Objects.equals(baseLine, ourLine) && Objects.equals(baseLine, theirLine)) {
                if (baseLine != null) {
                    result.add(baseLine);
                }
                i++; j++; k++;
            } else if (Objects.equals(baseLine, ourLine) && !Objects.equals(baseLine, theirLine)) {
                if (theirLine != null) {
                    result.add(theirLine);
                }
                i++; j++; k++;
            } else if (Objects.equals(baseLine, theirLine) && !Objects.equals(baseLine, ourLine)) {
                if (ourLine != null) {
                    result.add(ourLine);
                }
                i++; j++; k++;
            } else {
                List<String> oursChunk = new ArrayList<>();
                List<String> theirsChunk = new ArrayList<>();
                
                while (j < oursSize && !Objects.equals(ours.get(j), theirLine)) {
                    oursChunk.add(ours.get(j));
                    j++;
                }
                
                while (k < theirsSize && !Objects.equals(theirs.get(k), ourLine)) {
                    theirsChunk.add(theirs.get(k));
                    k++;
                }
                
                createConflict(result, oursChunk, theirsChunk);
                
                if (i < baseSize) i++;
            }
        }
        
        return result;
    }
    
    private List<String> mergeTwoWay(List<String> ours, List<String> theirs) {
        if (ours == null || ours.isEmpty()) {
            return theirs != null ? new ArrayList<>(theirs) : new ArrayList<>();
        }
        
        if (theirs == null || theirs.isEmpty()) {
            return new ArrayList<>(ours);
        }
        
        if (ours.equals(theirs)) {
            return new ArrayList<>(ours);
        }
        
        List<String> result = new ArrayList<>();
        createConflict(result, ours, theirs);
        return result;
    }
    
    private void createConflict(List<String> result, List<String> ours, List<String> theirs) {
        ConflictRegion conflict = new ConflictRegion(ours, theirs);
        conflicts.add(conflict);
        
        result.add("<<<<<<< OURS");
        result.addAll(ours);
        result.add("=======");
        result.addAll(theirs);
        result.add(">>>>>>> THEIRS");
    }
}

class OursMerge extends BaseMergeStrategy {
    @Override
    protected List<String> performMerge(List<String> base, List<String> ours, List<String> theirs) {
        return ours != null ? new ArrayList<>(ours) : new ArrayList<>();
    }
}

class TheirsMerge extends BaseMergeStrategy {
    @Override
    protected List<String> performMerge(List<String> base, List<String> ours, List<String> theirs) {
        return theirs != null ? new ArrayList<>(theirs) : new ArrayList<>();
    }
}

class MergeResult {
    private MergeStatus status;
    private List<String> content;
    private List<ConflictRegion> conflicts;
    
    public MergeResult(MergeStatus status, List<String> content, List<ConflictRegion> conflicts) {
        this.status = status;
        this.content = content;
        this.conflicts = conflicts;
    }
    
    public MergeStatus getStatus() { return status; }
    public List<String> getContent() { return content; }
    public List<ConflictRegion> getConflicts() { return conflicts; }
}

class ConflictRegion {
    private List<String> ours;
    private List<String> theirs;
    
    public ConflictRegion(List<String> ours, List<String> theirs) {
        this.ours = ours;
        this.theirs = theirs;
    }
    
    public List<String> getOurs() { return ours; }
    public List<String> getTheirs() { return theirs; }
}

enum MergeStatus {
    SUCCESS,
    CONFLICT,
    AUTOMATIC
}
