package com.javamail.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;

/**
 * User - Represents a registered user in the JavaMail system.
 * Annotated as a Spring Data JPA Entity and implements Spring Security UserDetails.
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "dob")
    private String dob;

    @Column(length = 15)
    private String contact;

    @Column(name = "profile_pic", length = 255)
    private String profilePic = "default.png";

    @Column(name = "is_active")
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
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

    // ── Spring Security UserDetails Implementation ──────
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return isActive; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return isActive; }

    @Override
    public String toString() {
        return "User{id=" + id + ", email='" + email + "', username='" + username + "'}";
    }
}

