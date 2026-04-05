package com.javamail.dao;

import com.javamail.model.User;
import com.javamail.util.DBConnection;
import com.javamail.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserDAO - Data Access Object for User operations.
 * Handles all CRUD operations against the users table.
 */
public class UserDAO {

    // ── Register a new user ──────────────────────────────
    public boolean register(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password, dob, contact) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, PasswordUtil.hash(user.getPassword()));
            String dobStr = user.getDob();
            java.sql.Date dob = null;

            if (dobStr != null && !dobStr.isEmpty()) {
                dob = java.sql.Date.valueOf(dobStr);
            }

            if (dob != null) {
                ps.setDate(4, dob);
            } else {
                ps.setNull(4, java.sql.Types.DATE);
            }
            ps.setString(5, user.getContact());
            return ps.executeUpdate() > 0;
        }
    }

    // ── Login: email + SHA2 password match ──────────────
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ? AND is_active = 1";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, PasswordUtil.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    // ── Check if email already registered ───────────────
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ── Fetch user by email ──────────────────────────────
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    // ── Fetch user by ID ─────────────────────────────────
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        }
        return null;
    }

    // ── Update profile ───────────────────────────────────
    public boolean updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, dob = ?, contact = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            String dobStr = user.getDob();
            java.sql.Date dob = null;

            if (dobStr != null && !dobStr.isEmpty()) {
                dob = java.sql.Date.valueOf(dobStr);
            }

            if (dob != null) {
                ps.setDate(2, dob);
            } else {
                ps.setNull(2, java.sql.Types.DATE);
            }
            ps.setString(3, user.getContact());
            ps.setString(4, user.getEmail());
            return ps.executeUpdate() > 0;
        }
    }

    // ── Change password ──────────────────────────────────
    public boolean changePassword(String email, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash(newPassword));
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Deactivate / Delete account ──────────────────────
    public boolean deactivateAccount(String email) throws SQLException {
        String sql = "UPDATE users SET is_active = 0 WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Search users by name / email (autocomplete) ──────
    public List<User> searchUsers(String query) throws SQLException {
        String sql = "SELECT * FROM users WHERE (username LIKE ? OR email LIKE ?) AND is_active = 1 LIMIT 10";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + query + "%";
            ps.setString(1, q);
            ps.setString(2, q);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── All users (admin) ────────────────────────────────
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM users WHERE is_active = 1 ORDER BY created_at DESC";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        }
        return list;
    }

    // ── Map ResultSet row to User object ─────────────────
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setDob(rs.getString("dob"));
        u.setContact(rs.getString("contact"));
        u.setProfilePic(rs.getString("profile_pic"));
        u.setActive(rs.getBoolean("is_active"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
