package com.javamail.repository;

import com.javamail.model.Mail;
import com.javamail.model.Mail.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailRepository extends JpaRepository<Mail, Integer> {

    // Inbox
    List<Mail> findByToEmailAndStatusOrderBySentAtDesc(String toEmail, Status status);

    // Sent / Drafts
    List<Mail> findByFromEmailAndStatusOrderBySentAtDesc(String fromEmail, Status status);

    // Starred mails
    @Query("SELECT m FROM Mail m WHERE (m.toEmail = :email OR m.fromEmail = :email) AND m.isStarred = true AND m.status != 'DELETED' ORDER BY m.sentAt DESC")
    List<Mail> findStarredMails(@Param("email") String email);

    // Important mails
    @Query("SELECT m FROM Mail m WHERE (m.toEmail = :email OR m.fromEmail = :email) AND m.isImportant = true AND m.status != 'DELETED' ORDER BY m.sentAt DESC")
    List<Mail> findImportantMails(@Param("email") String email);

    // Trash mails
    @Query("SELECT m FROM Mail m WHERE (m.toEmail = :email OR m.fromEmail = :email) AND m.status = com.javamail.model.Mail.Status.DELETED ORDER BY m.sentAt DESC")
    List<Mail> findTrashMails(@Param("email") String email);

    // Spam mails
    @Query("SELECT m FROM Mail m WHERE (m.toEmail = :email OR m.fromEmail = :email) AND m.status = com.javamail.model.Mail.Status.SPAM ORDER BY m.sentAt DESC")
    List<Mail> findSpamMails(@Param("email") String email);

    // Search mails
    @Query("SELECT m FROM Mail m WHERE (m.toEmail = :email OR m.fromEmail = :email) AND (LOWER(m.subject) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.body) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.fromEmail) LIKE LOWER(CONCAT('%', :query, '%'))) ORDER BY m.sentAt DESC")
    List<Mail> searchMails(@Param("email") String email, @Param("query") String query);

    // Count unread inbox items
    long countByToEmailAndStatusAndIsReadFalse(String toEmail, Status status);

    // Count by folder
    long countByToEmailAndStatus(String toEmail, Status status);

    long countByFromEmailAndStatus(String fromEmail, Status status);

    @Query("SELECT COUNT(m) FROM Mail m WHERE (m.toEmail = :email OR m.fromEmail = :email) AND m.isStarred = true AND m.status != 'DELETED'")
    long countStarredMails(@Param("email") String email);

    @Query("SELECT COUNT(m) FROM Mail m WHERE (m.toEmail = :email OR m.fromEmail = :email) AND m.isImportant = true AND m.status != 'DELETED'")
    long countImportantMails(@Param("email") String email);

    @Query("SELECT COUNT(m) FROM Mail m WHERE (m.toEmail = :email OR m.fromEmail = :email) AND m.status = com.javamail.model.Mail.Status.DELETED")
    long countTrashMails(@Param("email") String email);

    @Query("SELECT COUNT(m) FROM Mail m WHERE (m.toEmail = :email OR m.fromEmail = :email) AND m.status = com.javamail.model.Mail.Status.SPAM")
    long countSpamMails(@Param("email") String email);
}
