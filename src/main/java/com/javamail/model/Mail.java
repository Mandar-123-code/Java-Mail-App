package com.javamail.model;

import java.sql.Timestamp;
import java.util.List;

/**
 * Mail - Represents an email message in the JavaMail system.
 */
public class Mail {

    public enum Status { SENT, DRAFT, DELETED, SPAM }

    private int       id;
    private String    fromEmail;
    private String    toEmail;
    private String    ccEmail;
    private String    bccEmail;
    private String    subject;
    private String    body;
    private Status    status;
    private boolean   isRead;
    private boolean   isStarred;
    private boolean   isImportant;
    private Timestamp sentAt;

    // Optional: sender display name (joined from users)
    private String    fromUsername;

    // Attachments list (optional)
    private List<Attachment> attachments;

    public Mail() {}

    public Mail(String fromEmail, String toEmail, String subject, String body, Status status) {
        this.fromEmail = fromEmail;
        this.toEmail   = toEmail;
        this.subject   = subject;
        this.body      = body;
        this.status    = status;
    }

    // ── Getters ──────────────────────────────────────────
    public int       getId()           { return id; }
    public String    getFromEmail()    { return fromEmail; }
    public String    getToEmail()      { return toEmail; }
    public String    getCcEmail()      { return ccEmail; }
    public String    getBccEmail()     { return bccEmail; }
    public String    getSubject()      { return subject; }
    public String    getBody()         { return body; }
    public Status    getStatus()       { return status; }
    public boolean   isRead()          { return isRead; }
    public boolean   isStarred()       { return isStarred; }
    public boolean   isImportant()     { return isImportant; }
    public Timestamp getSentAt()       { return sentAt; }
    public String    getFromUsername() { return fromUsername; }
    public List<Attachment> getAttachments() { return attachments; }

    // ── Setters ──────────────────────────────────────────
    public void setId(int id)                      { this.id = id; }
    public void setFromEmail(String fromEmail)     { this.fromEmail = fromEmail; }
    public void setToEmail(String toEmail)         { this.toEmail = toEmail; }
    public void setCcEmail(String ccEmail)         { this.ccEmail = ccEmail; }
    public void setBccEmail(String bccEmail)       { this.bccEmail = bccEmail; }
    public void setSubject(String subject)         { this.subject = subject; }
    public void setBody(String body)               { this.body = body; }
    public void setStatus(Status status)           { this.status = status; }
    public void setRead(boolean read)              { this.isRead = read; }
    public void setStarred(boolean starred)        { this.isStarred = starred; }
    public void setImportant(boolean important)    { this.isImportant = important; }
    public void setSentAt(Timestamp sentAt)        { this.sentAt = sentAt; }
    public void setFromUsername(String name)       { this.fromUsername = name; }
    public void setAttachments(List<Attachment> a) { this.attachments = a; }

    // ── Helpers ──────────────────────────────────────────
    public String getPreview() {
        if (body == null || body.isEmpty()) return "(no content)";
        return body.length() > 80 ? body.substring(0, 80) + "…" : body;
    }

    public String getFormattedDate() {
        if (sentAt == null) return "";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm");
        return sdf.format(sentAt);
    }
}
