package com.javamail.model;

import java.sql.Timestamp;

/**
 * User - Represents a registered user in the JavaMail system.
 */
public class User {

    private int       id;
    private String    username;
    private String    email;
    private String    password;
    private String    dob;
    private String    contact;
    private String    profilePic;
    private boolean   isActive;
    private Timestamp createdAt;

    public User() {}

    public User(String username, String email, String password, String dob, String contact) {
        this.username   = username;
        this.email      = email;
        this.password   = password;
        this.dob        = dob;
        this.contact    = contact;
        this.profilePic = "default.png";
        this.isActive   = true;
    }

    // ── Getters ──────────────────────────────────────────
    public int       getId()         { return id; }
    public String    getUsername()   { return username; }
    public String    getEmail()      { return email; }
    public String    getPassword()   { return password; }
    public String    getDob()        { return dob; }
    public String    getContact()    { return contact; }
    public String    getProfilePic() { return profilePic; }
    public boolean   isActive()      { return isActive; }
    public Timestamp getCreatedAt()  { return createdAt; }

    // ── Setters ──────────────────────────────────────────
    public void setId(int id)                  { this.id = id; }
    public void setUsername(String username)   { this.username = username; }
    public void setEmail(String email)         { this.email = email; }
    public void setPassword(String password)   { this.password = password; }
    public void setDob(String dob)             { this.dob = dob; }
    public void setContact(String contact)     { this.contact = contact; }
    public void setProfilePic(String pic)      { this.profilePic = pic; }
    public void setActive(boolean active)      { this.isActive = active; }
    public void setCreatedAt(Timestamp ts)     { this.createdAt = ts; }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', username='" + username + "'}";
    }
}
