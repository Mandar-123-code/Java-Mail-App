package com.javamail.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBConnection - Singleton JDBC connection utility.
 * Manages the database connection for the JavaMail application.
 */
public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/javamail_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "root";  // Change to your MySQL password
    private static final String DRIVER   = "com.mysql.cj.jdbc.Driver";

    private static Connection connection = null;
    private static boolean schemaInitialized = false;

    private DBConnection() {}

    /**
     * Returns a live Connection. Creates one if it doesn't exist or is closed.
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DRIVER);
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                initializeSchema(connection);
                System.out.println("[DB] Connection established successfully.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-j to your classpath.", e);
        }
        return connection;
    }

    /**
     * Creates required tables and seed data when missing.
     * This keeps first-run setup simple in local/dev environments.
     */
    private static synchronized void initializeSchema(Connection conn) throws SQLException {
        if (schemaInitialized) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id          INT AUTO_INCREMENT PRIMARY KEY,
                    username    VARCHAR(100) NOT NULL,
                    email       VARCHAR(150) NOT NULL UNIQUE,
                    password    VARCHAR(255) NOT NULL,
                    dob         DATE,
                    contact     VARCHAR(15),
                    profile_pic VARCHAR(255) DEFAULT 'default.png',
                    is_active   TINYINT(1) DEFAULT 1,
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            stmt.execute("""
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
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS labels (
                    id         INT AUTO_INCREMENT PRIMARY KEY,
                    user_email VARCHAR(150) NOT NULL,
                    label_name VARCHAR(100) NOT NULL,
                    color      VARCHAR(20) DEFAULT '#4CAF50',
                    FOREIGN KEY (user_email) REFERENCES users(email) ON DELETE CASCADE
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS mail_labels (
                    mail_id  INT NOT NULL,
                    label_id INT NOT NULL,
                    PRIMARY KEY (mail_id, label_id),
                    FOREIGN KEY (mail_id) REFERENCES mails(id) ON DELETE CASCADE,
                    FOREIGN KEY (label_id) REFERENCES labels(id) ON DELETE CASCADE
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS attachments (
                    id         INT AUTO_INCREMENT PRIMARY KEY,
                    mail_id    INT NOT NULL,
                    file_name  VARCHAR(255) NOT NULL,
                    file_path  VARCHAR(500) NOT NULL,
                    file_size  BIGINT DEFAULT 0,
                    mime_type  VARCHAR(100),
                    FOREIGN KEY (mail_id) REFERENCES mails(id) ON DELETE CASCADE
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS contacts (
                    id            INT AUTO_INCREMENT PRIMARY KEY,
                    owner_email   VARCHAR(150) NOT NULL,
                    contact_name  VARCHAR(100),
                    contact_email VARCHAR(150) NOT NULL,
                    FOREIGN KEY (owner_email) REFERENCES users(email) ON DELETE CASCADE
                )
                """);

            stmt.execute("""
                INSERT IGNORE INTO users (username, email, password, dob, contact) VALUES
                ('Admin User',    'admin@javamail.com', SHA2('admin123', 256), '1990-01-01', '9999999999'),
                ('Alice Johnson', 'alice@javamail.com', SHA2('alice123', 256), '1995-05-15', '9876543210'),
                ('Bob Smith',     'bob@javamail.com',   SHA2('bob123',   256), '1992-08-20', '9123456789')
                """);
        }

        schemaInitialized = true;
    }

    /**
     * Closes the connection if open.
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing connection: " + e.getMessage());
        }
    }
}
