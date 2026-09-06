package com.javamail.service;

import com.javamail.model.Mail;
import com.javamail.model.User;
import com.javamail.repository.MailRepository;
import com.javamail.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final MailRepository mailRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, MailRepository mailRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailRepository = mailRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User registerUser(User user) {
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public boolean updateProfile(String email, String username, String dob, String contact) {
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setUsername(username);
            user.setDob(dob);
            user.setContact(contact);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isPresent()) {
            User user = optUser.get();
            if (passwordEncoder.matches(currentPassword, user.getPassword()) || legacyHashMatch(currentPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    private boolean legacyHashMatch(String raw, String hashed) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(raw.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString().equalsIgnoreCase(hashed);
        } catch (Exception e) {
            return false;
        }
    }

    @PostConstruct
    @Transactional
    public void seedInitialData() {
        if (userRepository.count() == 0) {
            User admin = new User("Admin User", "admin@javamail.com", passwordEncoder.encode("admin123"), "1990-01-01", "9999999999");
            User alice = new User("Alice Johnson", "alice@javamail.com", passwordEncoder.encode("alice123"), "1995-05-15", "9876543210");
            User bob = new User("Bob Smith", "bob@javamail.com", passwordEncoder.encode("bob123"), "1992-08-20", "9123456789");

            userRepository.save(admin);
            userRepository.save(alice);
            userRepository.save(bob);

            Mail m1 = new Mail("alice@javamail.com", "admin@javamail.com", "Welcome to JavaMail!", "Hey Admin! This is Alice. JavaMail Spring Boot app is looking great. Looking forward to using it!", Mail.Status.SENT);
            Mail m2 = new Mail("bob@javamail.com", "admin@javamail.com", "Project Update", "Hi, the project migration to Spring Boot is progressing well. All components are on track!", Mail.Status.SENT);
            Mail m3 = new Mail("alice@javamail.com", "bob@javamail.com", "Meeting Tomorrow", "Hi Bob, can we schedule a quick sync tomorrow at 10 AM? Let me know if that works.", Mail.Status.SENT);

            mailRepository.save(m1);
            mailRepository.save(m2);
            mailRepository.save(m3);
        }
    }
}
