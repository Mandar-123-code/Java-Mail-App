package com.javamail.model;

/**
 * Attachment - Represents a file attached to a mail.
 */
public class Attachment {

    private int    id;
    private int    mailId;
    private String fileName;
    private String filePath;
    private long   fileSize;
    private String mimeType;

    public Attachment() {}

    // ── Getters ──────────────────────────────────────────
    public int    getId()       { return id; }
    public int    getMailId()   { return mailId; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public long   getFileSize() { return fileSize; }
    public String getMimeType() { return mimeType; }

    // ── Setters ──────────────────────────────────────────
    public void setId(int id)            { this.id = id; }
    public void setMailId(int mailId)    { this.mailId = mailId; }
    public void setFileName(String name) { this.fileName = name; }
    public void setFilePath(String path) { this.filePath = path; }
    public void setFileSize(long size)   { this.fileSize = size; }
    public void setMimeType(String type) { this.mimeType = type; }

    public String getFormattedSize() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return (fileSize / 1024) + " KB";
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }
}
