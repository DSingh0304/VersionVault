package com.versionvault.core;

import java.time.LocalDateTime;
import java.util.Objects;

public class User implements Comparable<User> {
    private String name;
    private String email;
    private LocalDateTime lastLogin;
    private UserRole role;
    
    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.lastLogin = LocalDateTime.now();
        this.role = UserRole.DEVELOPER;
    }
    
    public User(String name, String email, UserRole role) {
        this(name, email);
        this.role = role;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getSignature() {
        return name + " <" + email + ">";
    }
    
    public void updateLoginTime() {
        this.lastLogin = LocalDateTime.now();
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public boolean hasPermission(Permission perm) {
        return role.hasPermission(perm);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        User other = (User) obj;
        return Objects.equals(email, other.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
    
    @Override
    public String toString() {
        return getSignature();
    }
    
    @Override
    public int compareTo(User other) {
        return this.email.compareTo(other.email);
    }
}

enum UserRole {
    ADMIN(Permission.values()),
    DEVELOPER(new Permission[]{Permission.READ, Permission.WRITE, Permission.COMMIT}),
    VIEWER(new Permission[]{Permission.READ});
    
    private final Permission[] permissions;
    
    UserRole(Permission[] permissions) {
        this.permissions = permissions;
    }
    
    public boolean hasPermission(Permission perm) {
        for (Permission p : permissions) {
            if (p == perm) return true;
        }
        return false;
    }
}

enum Permission {
    READ,
    WRITE,
    COMMIT,
    BRANCH,
    MERGE,
    DELETE,
    ADMIN
}
