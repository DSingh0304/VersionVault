package com.versionvault.cli;

import com.versionvault.core.*;
import com.versionvault.operations.*;
import com.versionvault.lock.*;
import com.versionvault.merge.*;
import java.util.*;

public class VersionVaultCLI {
    private Repository repository;
    private Scanner scanner;
    private CommandFactory commandFactory;
    
    public VersionVaultCLI() {
        this.scanner = new Scanner(System.in);
        this.commandFactory = new CommandFactory();
    }
    
    public void run(String[] args) {
        if (args.length == 0) {
            showHelp();
            return;
        }
        
        String command = args[0];
        
        try {
            if ("init".equals(command)) {
                initRepository(args);
            } else {
                loadRepository();
                executeCommand(command, args);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    private void initRepository(String[] args) throws RepositoryException {
        String path = args.length > 1 ? args[1] : ".";
        repository = new Repository(path);
        repository.initialize();
        
        System.out.println("Initialized empty VersionVault repository in " + path);
        
        configureUser();
    }
    
    private void loadRepository() throws RepositoryException {
        String currentPath = System.getProperty("user.dir");
        repository = new Repository(currentPath);
        
        if (!repository.isInitialized()) {
            throw new RepositoryException("Not a VersionVault repository");
        }
    }
    
    private void configureUser() {
        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter your email: ");
        String email = scanner.nextLine();
        
        User user = new User(name, email);
        repository.setUser(user);
    }
    
    private void executeCommand(String command, String[] args) throws Exception {
        Command cmd = commandFactory.createCommand(command, repository, args);
        if (cmd != null) {
            cmd.execute();
        } else {
            System.err.println("Unknown command: " + command);
            showHelp();
        }
    }
    
    private void showHelp() {
        System.out.println("VersionVault - Simple Version Control System");
        System.out.println();
        System.out.println("Usage: vv <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  init          Initialize a new repository");
        System.out.println("  add <file>    Add file to staging area");
        System.out.println("  commit <msg>  Commit staged changes");
        System.out.println("  branch <name> Create a new branch");
        System.out.println("  checkout <br> Switch to a branch");
        System.out.println("  merge <br>    Merge a branch");
        System.out.println("  log           Show commit history");
        System.out.println("  status        Show working tree status");
        System.out.println("  lock <file>   Lock a file");
        System.out.println("  unlock <file> Unlock a file");
    }
    
    public static void main(String[] args) {
        VersionVaultCLI cli = new VersionVaultCLI();
        cli.run(args);
    }
}

interface Command {
    void execute() throws Exception;
}

class CommandFactory {
    public Command createCommand(String commandName, Repository repo, String[] args) {
        switch (commandName) {
            case "add":
                return new AddCommand(repo, args);
            case "commit":
                return new CommitCommand(repo, args);
            case "branch":
                return new BranchCommand(repo, args);
            case "checkout":
                return new CheckoutCommand(repo, args);
            case "merge":
                return new MergeCommand(repo, args);
            case "log":
                return new LogCommand(repo, args);
            case "status":
                return new StatusCommand(repo, args);
            case "lock":
                return new LockCommand(repo, args);
            case "unlock":
                return new UnlockCommand(repo, args);
            default:
                return null;
        }
    }
}

class AddCommand implements Command {
    private Repository repo;
    private String[] args;
    
    public AddCommand(Repository repo, String[] args) {
        this.repo = repo;
        this.args = args;
    }
    
    @Override
    public void execute() throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: vv add <file>");
            return;
        }
        
        String file = args[1];
        repo.getStagingArea().addFile(file);
        System.out.println("Added " + file);
    }
}

class CommitCommand implements Command {
    private Repository repo;
    private String[] args;
    
    public CommitCommand(Repository repo, String[] args) {
        this.repo = repo;
        this.args = args;
    }
    
    @Override
    public void execute() throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: vv commit <message>");
            return;
        }
        
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            message.append(args[i]).append(" ");
        }
        
        User user = repo.getCurrentUser();
        if (user == null) {
            System.err.println("User not configured");
            return;
        }
        
        CommitOperation op = new CommitOperation(repo, message.toString().trim(), user);
        op.execute();
        
        for (String msg : op.getResult().getMessages()) {
            System.out.println(msg);
        }
    }
}

class BranchCommand implements Command {
    private Repository repo;
    private String[] args;
    
    public BranchCommand(Repository repo, String[] args) {
        this.repo = repo;
        this.args = args;
    }
    
    @Override
    public void execute() throws Exception {
        if (args.length < 2) {
            for (Branch branch : repo.getBranchManager().listBranches()) {
                String marker = branch.equals(repo.getBranchManager().getCurrentBranch()) ? "* " : "  ";
                System.out.println(marker + branch.getName());
            }
        } else {
            String branchName = args[1];
            repo.getBranchManager().createBranch(branchName);
            System.out.println("Created branch " + branchName);
        }
    }
}

class CheckoutCommand implements Command {
    private Repository repo;
    private String[] args;
    
    public CheckoutCommand(Repository repo, String[] args) {
        this.repo = repo;
        this.args = args;
    }
    
    @Override
    public void execute() throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: vv checkout <branch>");
            return;
        }
        
        String branchName = args[1];
        repo.getBranchManager().checkout(branchName);
        System.out.println("Switched to branch " + branchName);
    }
}

class MergeCommand implements Command {
    private Repository repo;
    private String[] args;
    
    public MergeCommand(Repository repo, String[] args) {
        this.repo = repo;
        this.args = args;
    }
    
    @Override
    public void execute() throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: vv merge <branch>");
            return;
        }
        
        String branchName = args[1];
        MergeOperation op = new MergeOperation(repo, branchName);
        op.execute();
        
        for (String msg : op.getResult().getMessages()) {
            System.out.println(msg);
        }
    }
}

class LogCommand implements Command {
    private Repository repo;
    private String[] args;
    
    public LogCommand(Repository repo, String[] args) {
        this.repo = repo;
        this.args = args;
    }
    
    @Override
    public void execute() {
        List<Commit> commits = repo.getCommitHistory().getCommitsSorted();
        
        for (Commit commit : commits) {
            System.out.println("commit " + commit.getHash());
            System.out.println("Author: " + commit.getAuthor().getSignature());
            System.out.println("Date:   " + commit.getTimestamp());
            System.out.println();
            System.out.println("    " + commit.getMessage());
            System.out.println();
        }
    }
}

class StatusCommand implements Command {
    private Repository repo;
    private String[] args;
    
    public StatusCommand(Repository repo, String[] args) {
        this.repo = repo;
        this.args = args;
    }
    
    @Override
    public void execute() {
        Branch current = repo.getBranchManager().getCurrentBranch();
        System.out.println("On branch " + (current != null ? current.getName() : "none"));
        
        StagingArea staging = repo.getStagingArea();
        
        if (!staging.isEmpty()) {
            System.out.println("\nChanges to be committed:");
            for (String file : staging.getStagedFiles().keySet()) {
                System.out.println("  modified: " + file);
            }
        } else {
            System.out.println("\nNothing to commit, working tree clean");
        }
    }
}

class LockCommand implements Command {
    private Repository repo;
    private String[] args;
    
    public LockCommand(Repository repo, String[] args) {
        this.repo = repo;
        this.args = args;
    }
    
    @Override
    public void execute() throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: vv lock <file>");
            return;
        }
        
        String file = args[1];
        User user = repo.getCurrentUser();
        
        LockManager lockManager = new LockManager(repo);
        lockManager.acquireLock(file, user, LockType.EXCLUSIVE);
        
        System.out.println("Locked " + file);
    }
}

class UnlockCommand implements Command {
    private Repository repo;
    private String[] args;
    
    public UnlockCommand(Repository repo, String[] args) {
        this.repo = repo;
        this.args = args;
    }
    
    @Override
    public void execute() throws Exception {
        if (args.length < 2) {
            System.err.println("Usage:vv unlock <file>");
            return;
        }
        
        String file = args[1];
        User user = repo.getCurrentUser();
        
        LockManager lockManager = new LockManager(repo);
        lockManager.releaseLock(file, user, false);
        
        System.out.println("Unlocked " + file);
    }
}
