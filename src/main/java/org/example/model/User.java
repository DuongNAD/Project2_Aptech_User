package org.example.model;

public class User {
    private int id;
    private String username;
    private String fullname;
    private String passwordHash;
    private String email;
    private String role;
    private String avatarUrl;
    private Boolean isActive;

    public User(String username, String fullname, String email, String passwordHash, String role, Boolean isActive) {
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.avatarUrl = "default.png";
        this.isActive = isActive;
    }


    public User(int id, String username, String fullname, String email, String passwordHash, String role, String avatarUrl, Boolean isActive) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.isActive = isActive;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getFullname() {
        return fullname;
    }
    public void setFullname(String username) {
        this.fullname = username;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

}
