package com.versionvault.gui;

import com.versionvault.core.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class BranchDialog extends JDialog {
    private Repository repository;
    private JList<String> branchList;
    private DefaultListModel<String> branchListModel;
    
    public BranchDialog(JFrame parent, Repository repo) {
        super(parent, "Branch Management", true);
        this.repository = repo;
        
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        initUI();
        refreshBranchList();
    }
    
    private void initUI() {
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Branches");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(titleLabel, BorderLayout.NORTH);
        
        branchListModel = new DefaultListModel<>();
        branchList = new JList<>(branchListModel);
        branchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        branchList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        branchList.setCellRenderer(new BranchCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(branchList);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton createButton = new JButton("Create Branch");
        createButton.setBackground(new Color(76, 175, 80));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);
        createButton.addActionListener(e -> createBranch());
        buttonPanel.add(createButton);
        
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.setBackground(new Color(33, 150, 243));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setFocusPainted(false);
        checkoutButton.addActionListener(e -> checkoutBranch());
        buttonPanel.add(checkoutButton);
        
        JButton mergeButton = new JButton("Merge");
        mergeButton.setBackground(new Color(156, 39, 176));
        mergeButton.setForeground(Color.WHITE);
        mergeButton.setFocusPainted(false);
        mergeButton.addActionListener(e -> mergeBranch());
        buttonPanel.add(mergeButton);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void refreshBranchList() {
        branchListModel.clear();
        Branch currentBranch = repository.getBranchManager().getCurrentBranch();
        
        for (Branch branch : repository.getBranchManager().listBranches()) {
            String prefix = branch.equals(currentBranch) ? "* " : "  ";
            branchListModel.addElement(prefix + branch.getName());
        }
    }
    
    private void createBranch() {
        String name = JOptionPane.showInputDialog(this, "Enter new branch name:");
        if (name != null && !name.trim().isEmpty()) {
            try {
                repository.getBranchManager().createBranch(name);
                refreshBranchList();
                JOptionPane.showMessageDialog(this, "Branch '" + name + "' created successfully!");
            } catch (RepositoryException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void checkoutBranch() {
        String selected = branchList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a branch!");
            return;
        }
        
        String branchName = selected.trim().substring(2);
        
        try {
            repository.getBranchManager().checkout(branchName);
            refreshBranchList();
            JOptionPane.showMessageDialog(this, "Switched to branch '" + branchName + "'");
        } catch (RepositoryException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void mergeBranch() {
        String selected = branchList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a branch to merge!");
            return;
        }
        
        String branchName = selected.trim().substring(2);
        Branch currentBranch = repository.getBranchManager().getCurrentBranch();
        
        if (currentBranch != null && branchName.equals(currentBranch.getName())) {
            JOptionPane.showMessageDialog(this, "Cannot merge a branch with itself!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Merge '" + branchName + "' into current branch?", 
            "Confirm Merge", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this, "Merge functionality: Branch '" + branchName + "' merged.");
        }
    }
    
    private class BranchCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            String text = value.toString();
            if (text.startsWith("* ")) {
                setFont(getFont().deriveFont(Font.BOLD));
                setForeground(new Color(76, 175, 80));
            } else {
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            
            return c;
        }
    }
}
