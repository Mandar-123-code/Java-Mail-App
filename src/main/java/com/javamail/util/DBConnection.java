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

    private static final String DRIVER = "org.postgresql.Driver";

    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    private static Connection connection = null;
    private static boolean schemaInitialized = false;

    private DBConnection() {}

    static {
        // Render provides DATABASE_URL in format: postgres://user:password@host:port/dbname
        String renderUrl = System.getenv("DATABASE_URL");
        String envUrl    = System.getenv("DB_URL");

        if (renderUrl != null && !renderUrl.isEmpty()) {
            // Parse Render's postgres:// URL into JDBC format
            try {
                java.net.URI uri = new java.net.URI(renderUrl);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath(); // e.g. /dbname
                String userInfo = uri.getUserInfo(); // e.g. user:password

                dbUrl = "jdbc:postgresql://" + host + ":" + port + path;
                if (userInfo != null && userInfo.contains(":")) {
                    dbUser = userInfo.split(":")[0];
                    dbPassword = userInfo.split(":", 2)[1];
                }
                System.out.println("[DB] Using Render DATABASE_URL -> " + host + ":" + port + path);
            } catch (Exception e) {
                System.err.println("[DB] Failed to parse DATABASE_URL: " + e.getMessage());
                dbUrl = "jdbc:postgresql://localhost:5432/javamail_db";
                dbUser = "postgres";
                dbPassword = "postgres";
            }
        } else if (envUrl != null && !envUrl.isEmpty()) {
            dbUrl = envUrl;
            dbUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "postgres";
            dbPassword = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "postgres";
        } else {
            dbUrl = "jdbc:postgresql://localhost:5432/javamail_db";
            dbUser = "postgres";
            dbPassword = "postgres";
        }
    }

    /**
     * Returns a live Connection. Creates one if it doesn't exist or is closed.
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DRIVER);
                connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                initializeSchema(connection);
                System.out.println("[DB] Connection established successfully.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found. Add postgresql to your classpath.", e);
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
                    id          SERIAL PRIMARY KEY,
                    username    VARCHAR(100) NOT NULL,
                    email       VARCHAR(150) NOT NULL UNIQUE,
                    password    VARCHAR(255) NOT NULL,
                    dob         DATE,
                    contact     VARCHAR(15),
                    profile_pic VARCHAR(255) DEFAULT 'default.png',
                    is_active   SMALLINT DEFAULT 1,
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS mails (
                    id           SERIAL PRIMARY KEY,
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
                )
                """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS labels (
                    id         SERIAL PRIMARY KEY,
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
                    id         SERIAL PRIMARY KEY,
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
                    id            SERIAL PRIMARY KEY,
                    owner_email   VARCHAR(150) NOT NULL,
                    contact_name  VARCHAR(100),
                    contact_email VARCHAR(150) NOT NULL,
                    FOREIGN KEY (owner_email) REFERENCES users(email) ON DELETE CASCADE
                )
                """);

            stmt.execute("""
                INSERT INTO users (username, email, password, dob, contact) VALUES
                ('Admin User',    'admin@javamail.com', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', '1990-01-01', '9999999999'),
                ('Alice Johnson', 'alice@javamail.com', '8a2c2084c8a29a0083ade45e0f760dafaaf986eb79f4de0bb79e5e79601d3cb4', '1995-05-15', '9876543210'),
                ('Bob Smith',     'bob@javamail.com',   'fdfb746d8ba96637e6b010b1442debe1aee1d3b038fb87002ea9a5ec92f03f38', '1992-08-20', '9123456789')
                ON CONFLICT (email) DO NOTHING
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
