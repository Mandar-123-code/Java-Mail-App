-- =====================================================
-- JavaMail Application - Database Schema
-- Compatible with MySQL 8.0+
-- =====================================================

CREATE DATABASE IF NOT EXISTS javamail_db;
USE javamail_db;

-- =====================================================
-- USERS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,   -- SHA-256 hashed
    dob         DATE,
    contact     VARCHAR(15),
    profile_pic VARCHAR(255) DEFAULT 'default.png',
    is_active   TINYINT(1) DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- MAILS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS mails (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    from_email   VARCHAR(150) NOT NULL,
    to_email     VARCHAR(150) NOT NULL,
    cc_email     VARCHAR(500) DEFAULT '',
    bcc_email    VARCHAR(500) DEFAULT '',
    subject      VARCHAR(300) NOT NULL,
    body         TEXT,
    status       ENUM('SENT','DRAFT','DELETED','SPAM') DEFAULT 'SENT',
    is_read      TINYINT(1) DEFAULT 0,
    is_starred   TINYINT(1) DEFAULT 0,
    is_important TINYINT(1) DEFAULT 0,
    sent_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_email) REFERENCES users(email) ON DELETE CASCADE
);

-- =====================================================
-- LABELS / FOLDERS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS labels (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_email VARCHAR(150) NOT NULL,
    label_name VARCHAR(100) NOT NULL,
    color      VARCHAR(20) DEFAULT '#4CAF50',
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- =====================================================
-- MAIL_LABELS (many-to-many)
-- =====================================================
CREATE TABLE IF NOT EXISTS mail_labels (
    mail_id  INT NOT NULL,
    label_id INT NOT NULL,
    PRIMARY KEY (mail_id, label_id),
    FOREIGN KEY (mail_id)  REFERENCES mails(id)  ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE
);

-- =====================================================
-- ATTACHMENTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS attachments (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    mail_id    INT NOT NULL,
    file_name  VARCHAR(255) NOT NULL,
    file_path  VARCHAR(500) NOT NULL,
    file_size  BIGINT DEFAULT 0,
    mime_type  VARCHAR(100),
    FOREIGN KEY (mail_id) REFERENCES mails(id) ON DELETE CASCADE
);

-- =====================================================
-- CONTACTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS contacts (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    owner_email  VARCHAR(150) NOT NULL,
    contact_name VARCHAR(100),
    contact_email VARCHAR(150) NOT NULL,
    FOREIGN KEY (owner_email) REFERENCES users(email) ON DELETE CASCADE
);

-- =====================================================
-- SAMPLE DATA
-- =====================================================
INSERT IGNORE INTO users (username, email, password, dob, contact) VALUES
('Admin User',    'admin@javamail.com', SHA2('admin123', 256), '1990-01-01', '9999999999'),
('Alice Johnson', 'alice@javamail.com', SHA2('alice123', 256), '1995-05-15', '9876543210'),
('Bob Smith',     'bob@javamail.com',   SHA2('bob123',   256), '1992-08-20', '9123456789');

INSERT IGNORE INTO mails (from_email, to_email, subject, body, status, is_read) VALUES
('alice@javamail.com', 'admin@javamail.com', 'Welcome to JavaMail!',
 'Hey Admin! This is Alice. JavaMail is looking great. Looking forward to using it!', 'SENT', 0),
('bob@javamail.com', 'admin@javamail.com', 'Project Update',
 'Hi, the project is progressing well. All features are on track for the deadline.', 'SENT', 0),
('alice@javamail.com', 'bob@javamail.com', 'Meeting Tomorrow',
 'Hi Bob, can we schedule a quick sync tomorrow at 10 AM? Let me know if that works.', 'SENT', 1);
