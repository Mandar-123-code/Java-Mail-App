# ✉ JavaMail — Full Java Major Project

A complete, production-grade **Java Mail Application** built with:
- **Java 17 + Servlets** (Jakarta EE)
- **JSP** front-end with a dark editorial CSS theme
- **MySQL + JDBC** for full persistence
- **Apache Tomcat 10** as the app server
- **Maven** for build management

---

## 📁 Project Structure

```
JavaMailProject/
├── pom.xml                                  ← Maven build
├── sql/
│   └── schema.sql                           ← DB schema + sample data
└── src/main/
    ├── java/com/javamail/
    │   ├── model/
    │   │   ├── User.java                    ← User entity
    │   │   ├── Mail.java                    ← Mail entity (SENT/DRAFT/DELETED/SPAM)
    │   │   └── Attachment.java              ← Attachment entity
    │   ├── dao/
    │   │   ├── UserDAO.java                 ← User CRUD (register, login, profile, password)
    │   │   └── MailDAO.java                 ← Mail CRUD (send, draft, inbox, trash, search…)
    │   ├── servlet/
    │   │   ├── UserServlet.java             ← /user/* (login, register, logout, profile)
    │   │   └── MailServlet.java             ← /mail/* (inbox, sent, draft, compose, view…)
    │   └── util/
    │       ├── DBConnection.java            ← JDBC singleton connection
    │       └── PasswordUtil.java            ← SHA-256 hashing
    └── webapp/
        ├── index.jsp                        ← Root redirect
        ├── login.jsp                        ← Login page
        ├── register.jsp                     ← Registration page
        ├── css/style.css                    ← Full dark theme stylesheet
        ├── js/app.js                        ← Client-side JS (toasts, AJAX, etc.)
        └── WEB-INF/
            ├── web.xml                      ← Deployment descriptor
            ├── sidebar.jsp                  ← Shared sidebar fragment
            ├── mailbox.jsp                  ← Mail list view (inbox/sent/draft/…)
            ├── viewmail.jsp                 ← Single mail view
            ├── compose.jsp                  ← Compose / Reply / Forward
            ├── profile.jsp                  ← User profile & password change
            ├── error404.jsp
            └── error500.jsp
```

---

## ⚙️ Setup Instructions

### 1. Prerequisites
| Tool         | Version     |
|--------------|-------------|
| JDK          | 17+         |
| Apache Maven | 3.8+        |
| MySQL        | 8.0+        |
| Apache Tomcat| 10.x        |

### 2. Database Setup

```bash
mysql -u root -p < sql/schema.sql
```

This creates the `javamail_db` database with all tables and sample users:

| Email                | Password    |
|----------------------|-------------|
| admin@javamail.com   | admin123    |
| alice@javamail.com   | alice123    |
| bob@javamail.com     | bob123      |

### 3. Configure Database Connection

Edit `src/main/java/com/javamail/util/DBConnection.java`:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/javamail_db";
private static final String USER     = "root";
private static final String PASSWORD = "your_mysql_password";
```

### 4. Build the WAR

```bash
cd JavaMailProject
mvn clean package
```

Output: `target/JavaMailApp.war`

### 5. Deploy to Tomcat

Copy the WAR to Tomcat's webapps directory:

```bash
cp target/JavaMailApp.war /path/to/tomcat/webapps/
```

Or using the Tomcat Manager at `http://localhost:8080/manager`.

### 6. Access the App

```
http://localhost:8080/JavaMailApp/
```

---

## 🚀 Features

### Auth
- ✅ Register with first name, last name, email, password, DOB, contact
- ✅ Login with SHA-256 hashed password
- ✅ Session management (30-minute timeout)
- ✅ Logout

### Mail Operations
| Feature         | Description |
|-----------------|-------------|
| 📥 Inbox        | All received mails, unread count badge |
| 📤 Sent         | All sent mails |
| 📝 Drafts       | Save & resume draft mails |
| ⭐ Starred      | Star/unstar any mail |
| 🔖 Important    | Mark/unmark important |
| 🚫 Spam         | Mark mail as spam |
| 🗑 Trash        | Move to trash, restore, empty trash |
| 🔍 Search       | Search by subject, body, sender |
| ↩ Reply         | Reply with pre-filled quote |
| ↪ Forward       | Forward with pre-filled original |
| 📋 CC / BCC     | Toggle CC/BCC fields in compose |
| ☑ Bulk Select   | Select multiple mails for bulk actions |

### Profile
- ✅ View and update username, DOB, contact
- ✅ Change password (with current password verification)
- ✅ Account stats display

### UI
- ✅ Dark editorial theme (CSS custom properties)
- ✅ Syne + DM Sans Google Fonts
- ✅ Toast notifications (success/error)
- ✅ Responsive sidebar with active state
- ✅ Empty state illustrations
- ✅ Color-coded avatar initials
- ✅ AJAX star/important toggle (no page reload)
- ✅ Ctrl+S draft shortcut
- ✅ Unread mail highlight + accent border

---

## 🗄️ Database Schema

```sql
users        -- id, username, email, password(SHA2), dob, contact, is_active, created_at
mails        -- id, from_email, to_email, cc, bcc, subject, body, status, is_read,
             --    is_starred, is_important, sent_at
labels       -- id, user_email, label_name, color
mail_labels  -- mail_id ↔ label_id (many-to-many)
attachments  -- id, mail_id, file_name, file_path, file_size, mime_type
contacts     -- id, owner_email, contact_name, contact_email
```

---

## 🔒 Security Notes

- Passwords stored as **SHA-256 hashes** via MySQL's `SHA2()` function
- All DB queries use **PreparedStatements** (SQL injection safe)
- Sessions validated on every protected servlet
- WEB-INF JSPs are not directly accessible by URL

---

## 🛠️ Extending the Project

| Idea                      | Where to add |
|---------------------------|--------------|
| File attachments           | `AttachmentDAO` + multipart in `MailServlet` |
| Labels / custom folders    | `LabelDAO` + label UI in sidebar |
| Pagination                 | Modify DAO queries with `LIMIT / OFFSET` |
| Email notifications        | JavaMail API (`javax.mail`) in `MailDAO.sendMail()` |
| Admin panel                | New `AdminServlet` + `getAllUsers()` in `UserDAO` |
| REST API                   | Add `@WebServlet` with JSON output (`Gson`/`Jackson`) |
