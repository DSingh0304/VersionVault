package com.versionvault.gui;

import com.versionvault.core.*;
import com.versionvault.operations.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class VersionVaultGUI extends JFrame {
    private Repository repository;
    private JTextArea statusArea;
    private JList<String> fileList;
    private DefaultListModel<String> fileListModel;
    private JTextArea logArea;
    private JLabel currentBranchLabel;
    private JButton initButton, addButton, commitButton, branchButton, logButton, refreshButton;
    
    public VersionVaultGUI() {
        setTitle("VersionVault - Version Control System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initializeUI();
        updateUIState();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createFilePanel());
        splitPane.setRightComponent(createOutputPanel());
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);
        
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(45, 45, 45));
        
        currentBranchLabel = new JLabel("No Repository");
        currentBranchLabel.setForeground(Color.WHITE);
        currentBranchLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(currentBranchLabel);
        
        panel.add(Box.createHorizontalStrut(20));
        
        initButton = createStyledButton("Initialize Repo", new Color(76, 175, 80));
        initButton.addActionListener(e -> initializeRepository());
        panel.add(initButton);
        
        addButton = createStyledButton("Add Files", new Color(33, 150, 243));
        addButton.addActionListener(e -> addSelectedFiles());
        panel.add(addButton);
        
        commitButton = createStyledButton("Commit", new Color(156, 39, 176));
        commitButton.addActionListener(e -> showCommitDialog());
        panel.add(commitButton);
        
        branchButton = createStyledButton("Branches", new Color(255, 152, 0));
        branchButton.addActionListener(e -> showBranchDialog());
        panel.add(branchButton);
        
        logButton = createStyledButton("View Log", new Color(96, 125, 139));
        logButton.addActionListener(e -> viewLog());
        panel.add(logButton);
        
        refreshButton = createStyledButton("Refresh", new Color(76, 175, 80));
        refreshButton.addActionListener(e -> refreshView());
        panel.add(refreshButton);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JPanel createFilePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Working Directory"));
        
        fileListModel = new DefaultListModel<>();
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(fileList);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane statusScroll = new JScrollPane(statusArea);
        tabbedPane.addTab("Status", statusScroll);
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        tabbedPane.addTab("Commit Log", logScroll);
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(240, 240, 240));
        
        JLabel label = new JLabel("Ready");
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        panel.add(label);
        
        return panel;
    }
    
    private void initializeRepository() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Repository Location");
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String path = chooser.getSelectedFile().getAbsolutePath();
                repository = new Repository(path);
                
                if (!repository.isInitialized()) {
                    String name = JOptionPane.showInputDialog(this, "Enter your name:");
                    String email = JOptionPane.showInputDialog(this, "Enter your email:");
                    
                    if (name != null && email != null) {
                        User user = new User(name, email);
                        repository.setUser(user);
                        repository.initialize();
                        
                        updateUIState();
                        refreshView();
                        JOptionPane.showMessageDialog(this, "Repository initialized successfully!");
                    }
                } else {
                    updateUIState();
                    refreshView();
                    JOptionPane.showMessageDialog(this, "Repository loaded successfully!");
                }
            } catch (RepositoryException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void addSelectedFiles() {
        if (repository == null) {
            JOptionPane.showMessageDialog(this, "Please initialize a repository first!");
            return;
        }
        
        java.util.List<String> selectedFiles = fileList.getSelectedValuesList();
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select files to add!");
            return;
        }
        
        try {
            for (String file : selectedFiles) {
                repository.getStagingArea().addFile(file);
            }
            refreshView();
            JOptionPane.showMessageDialog(this, "Files added to staging area!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showCommitDialog() {
        if (repository == null) {
            JOptionPane.showMessageDialog(this, "Please initialize a repository first!");
            return;
        }
        
        String message = JOptionPane.showInputDialog(this, "Enter commit message:");
        if (message != null && !message.trim().isEmpty()) {
            try {
                User user = repository.getCurrentUser();
                if (user == null) {
                    JOptionPane.showMessageDialog(this, "User not configured!");
                    return;
                }
                
                CommitOperation op = new CommitOperation(repository, message, user);
                op.execute();
                
                refreshView();
                JOptionPane.showMessageDialog(this, "Commit successful!\n" + String.join("\n", op.getResult().getMessages()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void showBranchDialog() {
        if (repository == null) {
            JOptionPane.showMessageDialog(this, "Please initialize a repository first!");
            return;
        }
        
        BranchDialog dialog = new BranchDialog(this, repository);
        dialog.setVisible(true);
        updateUIState();
    }
    
    private void viewLog() {
        if (repository == null) {
            logArea.setText("No repository loaded.");
            return;
        }
        
        StringBuilder log = new StringBuilder();
        for (Commit commit : repository.getCommitHistory().getCommitsSorted()) {
            log.append("Commit: ").append(commit.getHash()).append("\n");
            log.append("Author: ").append(commit.getAuthor().getSignature()).append("\n");
            log.append("Date:   ").append(commit.getTimestamp()).append("\n");
            log.append("\n    ").append(commit.getMessage()).append("\n");
            log.append("\n");
        }
        
        logArea.setText(log.toString());
    }
    
    private void refreshView() {
        if (repository == null) {
            return;
        }
        
        fileListModel.clear();
        File repoDir = new File(repository.getRootPath());
        File[] files = repoDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().startsWith(".")) {
                    fileListModel.addElement(file.getName());
                }
            }
        }
        
        StringBuilder status = new StringBuilder();
        Branch currentBranch = repository.getBranchManager().getCurrentBranch();
        status.append("On branch: ").append(currentBranch != null ? currentBranch.getName() : "none").append("\n\n");
        
        StagingArea staging = repository.getStagingArea();
        if (!staging.isEmpty()) {
            status.append("Changes to be committed:\n");
            for (String file : staging.getStagedFiles().keySet()) {
                status.append("  modified: ").append(file).append("\n");
            }
        } else {
            status.append("Nothing to commit, working tree clean\n");
        }
        
        statusArea.setText(status.toString());
        viewLog();
    }
    
    private void updateUIState() {
        boolean hasRepo = repository != null && repository.isInitialized();
        
        addButton.setEnabled(hasRepo);
        commitButton.setEnabled(hasRepo);
        branchButton.setEnabled(hasRepo);
        logButton.setEnabled(hasRepo);
        refreshButton.setEnabled(hasRepo);
        
        if (hasRepo) {
            Branch currentBranch = repository.getBranchManager().getCurrentBranch();
            currentBranchLabel.setText("Branch: " + (currentBranch != null ? currentBranch.getName() : "none"));
        } else {
            currentBranchLabel.setText("No Repository");
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            VersionVaultGUI gui = new VersionVaultGUI();
            gui.setVisible(true);
        });
    }
}
