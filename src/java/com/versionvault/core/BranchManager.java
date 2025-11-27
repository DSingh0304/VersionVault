package com.versionvault.core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BranchManager {
    private Repository repository;
    private Map<String, Branch> branches;
    private Branch currentBranch;

    public BranchManager(Repository repo) {
        this.repository = repo;
        this.branches = new HashMap<>();
        loadBranches();
    }

    public void createBranch(String name) throws RepositoryException {
        if (branches.containsKey(name)) {
            throw new RepositoryException("Branch already exists: " + name);
        }

        Branch branch = new Branch(name);

        if (currentBranch != null && currentBranch.getCurrentCommit() != null) {
            branch.setCurrentCommit(currentBranch.getCurrentCommit());
        }

        branches.put(name, branch);
        saveBranch(branch);
    }

    public void deleteBranch(String name) throws RepositoryException {
        if (currentBranch != null && currentBranch.getName().equals(name)) {
            throw new RepositoryException("Cannot delete current branch");
        }

        if (!branches.containsKey(name)) {
            throw new RepositoryException("Branch does not exist: " + name);
        }

        branches.remove(name);

        try {
            Path branchFile = Paths.get(repository.getVVPath(), "refs", "heads", name);
            Files.deleteIfExists(branchFile);
        } catch (IOException e) {
            throw new RepositoryException("Failed to delete branch file", e);
        }
    }

    public void checkout(String name) throws RepositoryException {
        if (!branches.containsKey(name)) {
            throw new RepositoryException("Branch does not exist: " + name);
        }

        currentBranch = branches.get(name);
        updateHEAD();
    }

    public Branch getCurrentBranch() {
        return currentBranch;
    }

    public List<Branch> listBranches() {
        return new ArrayList<>(branches.values());
    }

    public Branch getBranch(String name) {
        return branches.get(name);
    }

    public void updateCurrentBranchCommit(String commitHash) {
        if (currentBranch != null) {
            currentBranch.setCurrentCommit(commitHash);
            saveBranch(currentBranch);
            updateHEAD();
        }
    }

    private void saveBranch(Branch branch) {
        try {
            Path branchFile = Paths.get(repository.getVVPath(), "refs", "heads", branch.getName());
            Files.createDirectories(branchFile.getParent());

            String commitHash = branch.getCurrentCommit();
            Files.writeString(branchFile, commitHash != null ? commitHash : "");
        } catch (IOException e) {
            throw new RuntimeException("Failed to save branch", e);
        }
    }

    private void loadBranches() {
        Path headsPath = Paths.get(repository.getVVPath(), "refs", "heads");
        if (!Files.exists(headsPath)) {
            return;
        }

        try {
            Files.walk(headsPath, 1)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            String name = file.getFileName().toString();
                            String commitHash = Files.readString(file).trim();

                            Branch branch = new Branch(name);
                            branch.setCurrentCommit(commitHash);
                            branches.put(name, branch);

                        } catch (IOException e) {
                        }
                    });
        } catch (IOException e) {
        }

        loadCurrentBranch();
    }

    private void loadCurrentBranch() {
        Path headFile = Paths.get(repository.getVVPath(), "HEAD");
        if (Files.exists(headFile)) {
            try {
                String content = Files.readString(headFile).trim();
                if (content.startsWith("ref: refs/heads/")) {
                    String branchName = content.substring("ref: refs/heads/".length());
                    currentBranch = branches.get(branchName);
                }
            } catch (IOException e) {
            }
        }
    }

    private void updateHEAD() {
        try {
            Path headFile = Paths.get(repository.getVVPath(), "HEAD");
            if (currentBranch != null) {
                String content = "ref: refs/heads/" + currentBranch.getName();
                Files.writeString(headFile, content);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update HEAD", e);
        }
    }
}
