package com.javamail.dao;

import com.javamail.model.Mail;
import com.javamail.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MailDAO - Data Access Object for Mail operations.
 * Covers: send, draft, inbox, sent, trash, spam, star, important, search, read-status.
 */
public class MailDAO {

    // ── Send a mail ──────────────────────────────────────
    public boolean sendMail(Mail mail) throws SQLException {
        String sql = "INSERT INTO mails (from_email, to_email, cc_email, bcc_email, subject, body, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'SENT')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mail.getFromEmail());
            ps.setString(2, mail.getToEmail());
            ps.setString(3, mail.getCcEmail()  != null ? mail.getCcEmail()  : "");
            ps.setString(4, mail.getBccEmail() != null ? mail.getBccEmail() : "");
            ps.setString(5, mail.getSubject());
            ps.setString(6, mail.getBody());
            return ps.executeUpdate() > 0;
        }
    }

    // ── Save as draft ────────────────────────────────────
    public boolean saveDraft(Mail mail) throws SQLException {
        String sql = "INSERT INTO mails (from_email, to_email, cc_email, bcc_email, subject, body, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'DRAFT')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, mail.getFromEmail());
            ps.setString(2, mail.getToEmail()  != null ? mail.getToEmail()  : "");
            ps.setString(3, mail.getCcEmail()  != null ? mail.getCcEmail()  : "");
            ps.setString(4, mail.getBccEmail() != null ? mail.getBccEmail() : "");
            ps.setString(5, mail.getSubject()  != null ? mail.getSubject()  : "");
            ps.setString(6, mail.getBody()     != null ? mail.getBody()     : "");
            return ps.executeUpdate() > 0;
        }
    }

    // ── Update draft & send ──────────────────────────────
    public boolean sendDraft(int mailId) throws SQLException {
        String sql = "UPDATE mails SET status = 'SENT', sent_at = NOW() WHERE id = ? AND status = 'DRAFT'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mailId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Inbox: mails received by the user ───────────────
    public List<Mail> getInbox(String email) throws SQLException {
        String sql = "SELECT m.*, u.username AS from_username " +
                     "FROM mails m LEFT JOIN users u ON m.from_email = u.email " +
                     "WHERE m.to_email = ? AND m.status = 'SENT' " +
                     "ORDER BY m.sent_at DESC";
        return fetchList(sql, email);
    }

    // ── Sent: mails sent by the user ────────────────────
    public List<Mail> getSentMails(String email) throws SQLException {
        String sql = "SELECT m.*, u.username AS from_username " +
                     "FROM mails m LEFT JOIN users u ON m.from_email = u.email " +
                     "WHERE m.from_email = ? AND m.status = 'SENT' " +
                     "ORDER BY m.sent_at DESC";
        return fetchList(sql, email);
    }

    // ── Drafts ───────────────────────────────────────────
    public List<Mail> getDrafts(String email) throws SQLException {
        String sql = "SELECT m.*, u.username AS from_username " +
                     "FROM mails m LEFT JOIN users u ON m.from_email = u.email " +
                     "WHERE m.from_email = ? AND m.status = 'DRAFT' " +
                     "ORDER BY m.sent_at DESC";
        return fetchList(sql, email);
    }

    // ── Trash ────────────────────────────────────────────
    public List<Mail> getTrash(String email) throws SQLException {
        String sql = "SELECT m.*, u.username AS from_username " +
                     "FROM mails m LEFT JOIN users u ON m.from_email = u.email " +
                     "WHERE (m.from_email = ? OR m.to_email = ?) AND m.status = 'DELETED' " +
                     "ORDER BY m.sent_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, email);
            return buildList(ps);
        }
    }

    // ── Spam ─────────────────────────────────────────────
    public List<Mail> getSpam(String email) throws SQLException {
        String sql = "SELECT m.*, u.username AS from_username " +
                     "FROM mails m LEFT JOIN users u ON m.from_email = u.email " +
                     "WHERE m.to_email = ? AND m.status = 'SPAM' " +
                     "ORDER BY m.sent_at DESC";
        return fetchList(sql, email);
    }

    // ── Starred ──────────────────────────────────────────
    public List<Mail> getStarred(String email) throws SQLException {
        String sql = "SELECT m.*, u.username AS from_username " +
                     "FROM mails m LEFT JOIN users u ON m.from_email = u.email " +
                     "WHERE (m.from_email = ? OR m.to_email = ?) AND m.is_starred = 1 " +
                     "ORDER BY m.sent_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, email);
            return buildList(ps);
        }
    }

    // ── Important ────────────────────────────────────────
    public List<Mail> getImportant(String email) throws SQLException {
        String sql = "SELECT m.*, u.username AS from_username " +
                     "FROM mails m LEFT JOIN users u ON m.from_email = u.email " +
                     "WHERE (m.from_email = ? OR m.to_email = ?) AND m.is_important = 1 " +
                     "ORDER BY m.sent_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, email);
            return buildList(ps);
        }
    }

    // ── Get mail by ID ───────────────────────────────────
    public Mail getMailById(int id) throws SQLException {
        String sql = "SELECT m.*, u.username AS from_username " +
                     "FROM mails m LEFT JOIN users u ON m.from_email = u.email " +
                     "WHERE m.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── Mark as read ─────────────────────────────────────
    public boolean markRead(int mailId) throws SQLException {
        return updateFlag("is_read", 1, mailId);
    }

    // ── Mark as unread ───────────────────────────────────
    public boolean markUnread(int mailId) throws SQLException {
        return updateFlag("is_read", 0, mailId);
    }

    // ── Toggle star ──────────────────────────────────────
    public boolean toggleStar(int mailId) throws SQLException {
        String sql = "UPDATE mails SET is_starred = 1 - is_starred WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mailId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Toggle important ─────────────────────────────────
    public boolean toggleImportant(int mailId) throws SQLException {
        String sql = "UPDATE mails SET is_important = 1 - is_important WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mailId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Move to trash ────────────────────────────────────
    public boolean moveToTrash(int mailId) throws SQLException {
        return updateStatus(mailId, "DELETED");
    }

    // ── Restore from trash ───────────────────────────────
    public boolean restoreFromTrash(int mailId) throws SQLException {
        return updateStatus(mailId, "SENT");
    }

    // ── Mark as spam ─────────────────────────────────────
    public boolean markAsSpam(int mailId) throws SQLException {
        return updateStatus(mailId, "SPAM");
    }

    // ── Permanent delete ─────────────────────────────────
    public boolean permanentDelete(int mailId) throws SQLException {
        String sql = "DELETE FROM mails WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, mailId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Empty trash ──────────────────────────────────────
    public boolean emptyTrash(String email) throws SQLException {
        String sql = "DELETE FROM mails WHERE (from_email = ? OR to_email = ?) AND status = 'DELETED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, email);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Search mails ─────────────────────────────────────
    public List<Mail> searchMails(String email, String query) throws SQLException {
        String sql = "SELECT m.*, u.username AS from_username " +
                     "FROM mails m LEFT JOIN users u ON m.from_email = u.email " +
                     "WHERE (m.from_email = ? OR m.to_email = ?) " +
                     "  AND (m.subject LIKE ? OR m.body LIKE ? OR m.from_email LIKE ?) " +
                     "  AND m.status != 'DELETED' " +
                     "ORDER BY m.sent_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String q = "%" + query + "%";
            ps.setString(1, email);
            ps.setString(2, email);
            ps.setString(3, q);
            ps.setString(4, q);
            ps.setString(5, q);
            return buildList(ps);
        }
    }

    // ── Count unread inbox mails ──────────────────────────
    public int countUnread(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mails WHERE to_email = ? AND is_read = 0 AND status = 'SENT'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ── Reply (creates new mail in reply thread) ─────────
    public boolean replyMail(String fromEmail, String toEmail, String subject, String body) throws SQLException {
        Mail reply = new Mail();
        reply.setFromEmail(fromEmail);
        reply.setToEmail(toEmail);
        reply.setSubject("Re: " + subject);
        reply.setBody(body);
        return sendMail(reply);
    }

    // ── Forward ──────────────────────────────────────────
    public boolean forwardMail(String fromEmail, String toEmail, String subject, String body) throws SQLException {
        Mail fwd = new Mail();
        fwd.setFromEmail(fromEmail);
        fwd.setToEmail(toEmail);
        fwd.setSubject("Fwd: " + subject);
        fwd.setBody(body);
        return sendMail(fwd);
    }

    // ─────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────

    private List<Mail> fetchList(String sql, String param) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            return buildList(ps);
        }
    }

    private List<Mail> buildList(PreparedStatement ps) throws SQLException {
        List<Mail> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private boolean updateFlag(String column, int value, int mailId) throws SQLException {
        String sql = "UPDATE mails SET " + column + " = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, value);
            ps.setInt(2, mailId);
            return ps.executeUpdate() > 0;
        }
    }

    private boolean updateStatus(int mailId, String status) throws SQLException {
        String sql = "UPDATE mails SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, mailId);
            return ps.executeUpdate() > 0;
        }
    }

    private Mail mapRow(ResultSet rs) throws SQLException {
        Mail m = new Mail();
        m.setId(rs.getInt("id"));
        m.setFromEmail(rs.getString("from_email"));
        m.setToEmail(rs.getString("to_email"));
        m.setCcEmail(rs.getString("cc_email"));
        m.setBccEmail(rs.getString("bcc_email"));
        m.setSubject(rs.getString("subject"));
        m.setBody(rs.getString("body"));
        m.setStatus(Mail.Status.valueOf(rs.getString("status")));
        m.setRead(rs.getBoolean("is_read"));
        m.setStarred(rs.getBoolean("is_starred"));
        m.setImportant(rs.getBoolean("is_important"));
        m.setSentAt(rs.getTimestamp("sent_at"));
        try { m.setFromUsername(rs.getString("from_username")); }
        catch (SQLException ignored) {}
        return m;
    }
}
