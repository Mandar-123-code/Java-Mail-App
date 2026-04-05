-- =====================================================
-- JavaMail Application - Database Schema (Reference)
-- Compatible with PostgreSQL
-- Note: Tables are auto-created by DBConnection.java
-- =====================================================

-- USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id          INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username    VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    dob         DATE,
    contact     VARCHAR(15),
    profile_pic VARCHAR(255) DEFAULT 'default.png',
    is_active   SMALLINT DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- MAILS TABLE
CREATE TABLE IF NOT EXISTS mails (
    id           INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    from_email   VARCHAR(150) NOT NULL,
    to_email     VARCHAR(150) NOT NULL,
    cc_email     VARCHAR(500) DEFAULT '',
    bcc_email    VARCHAR(500) DEFAULT '',
    subject      VARCHAR(300) NOT NULL,
    body         TEXT,
    status       VARCHAR(20) DEFAULT 'SENT',
    is_read      SMALLINT DEFAULT 0,
    is_starred   SMALLINT DEFAULT 0,
    is_important SMALLINT DEFAULT 0,
    sent_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_email) REFERENCES users(email) ON DELETE CASCADE
);

-- LABELS TABLE
CREATE TABLE IF NOT EXISTS labels (
    id         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_email VARCHAR(150) NOT NULL,
    label_name VARCHAR(100) NOT NULL,
    color      VARCHAR(20) DEFAULT '#4CAF50',
    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
);

-- MAIL_LABELS (many-to-many)
CREATE TABLE IF NOT EXISTS mail_labels (
    mail_id  INT NOT NULL,
    label_id INT NOT NULL,
    PRIMARY KEY (mail_id, label_id),
    FOREIGN KEY (mail_id)  REFERENCES mails(id)  ON DELETE CASCADE,
    FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE
);

-- ATTACHMENTS TABLE
CREATE TABLE IF NOT EXISTS attachments (
    id         INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mail_id    INT NOT NULL,
    file_name  VARCHAR(255) NOT NULL,
    file_path  VARCHAR(500) NOT NULL,
    file_size  BIGINT DEFAULT 0,
    mime_type  VARCHAR(100),
    FOREIGN KEY (mail_id) REFERENCES mails(id) ON DELETE CASCADE
);

-- CONTACTS TABLE
CREATE TABLE IF NOT EXISTS contacts (
    id            INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_email   VARCHAR(150) NOT NULL,
    contact_name  VARCHAR(100),
    contact_email VARCHAR(150) NOT NULL,
    FOREIGN KEY (owner_email) REFERENCES users(email) ON DELETE CASCADE
);

-- =====================================================
-- SAMPLE DATA
-- Passwords are SHA-256 hashes: admin123, alice123, bob123
-- =====================================================
INSERT INTO users (username, email, password, dob, contact)
SELECT 'Admin User', 'admin@javamail.com', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', DATE '1990-01-01', '9999999999'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@javamail.com');

INSERT INTO users (username, email, password, dob, contact)
SELECT 'Alice Johnson', 'alice@javamail.com', '8a2c2084c8a29a0083ade45e0f760dafaaf986eb79f4de0bb79e5e79601d3cb4', DATE '1995-05-15', '9876543210'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'alice@javamail.com');

INSERT INTO users (username, email, password, dob, contact)
SELECT 'Bob Smith', 'bob@javamail.com', 'fdfb746d8ba96637e6b010b1442debe1aee1d3b038fb87002ea9a5ec92f03f38', DATE '1992-08-20', '9123456789'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'bob@javamail.com');

INSERT INTO mails (from_email, to_email, subject, body, status, is_read) VALUES
('alice@javamail.com', 'admin@javamail.com', 'Welcome to JavaMail!',
 'Hey Admin! This is Alice. JavaMail is looking great. Looking forward to using it!', 'SENT', 0);

INSERT INTO mails (from_email, to_email, subject, body, status, is_read) VALUES
('bob@javamail.com', 'admin@javamail.com', 'Project Update',
 'Hi, the project is progressing well. All features are on track for the deadline.', 'SENT', 0);

INSERT INTO mails (from_email, to_email, subject, body, status, is_read) VALUES
('alice@javamail.com', 'bob@javamail.com', 'Meeting Tomorrow',
 'Hi Bob, can we schedule a quick sync tomorrow at 10 AM? Let me know if that works.', 'SENT', 1);
