package com.javamail.service;

import com.javamail.model.Mail;
import com.javamail.model.Mail.Status;
import com.javamail.model.User;
import com.javamail.repository.MailRepository;
import com.javamail.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MailService {

    private final MailRepository mailRepository;
    private final UserRepository userRepository;

    @Autowired
    public MailService(MailRepository mailRepository, UserRepository userRepository) {
        this.mailRepository = mailRepository;
        this.userRepository = userRepository;
    }

    public List<Mail> getMailsByFolder(String email, String folder) {
        List<Mail> mails;
        if (folder == null) folder = "inbox";

        switch (folder.toLowerCase()) {
            case "sent":
                mails = mailRepository.findByFromEmailAndStatusOrderBySentAtDesc(email, Status.SENT);
                break;
            case "drafts":
            case "draft":
                mails = mailRepository.findByFromEmailAndStatusOrderBySentAtDesc(email, Status.DRAFT);
                break;
            case "starred":
                mails = mailRepository.findStarredMails(email);
                break;
            case "important":
                mails = mailRepository.findImportantMails(email);
                break;
            case "trash":
                mails = mailRepository.findTrashMails(email);
                break;
            case "spam":
                mails = mailRepository.findByToEmailAndStatusOrderBySentAtDesc(email, Status.SPAM);
                break;
            case "inbox":
            default:
                mails = mailRepository.findByToEmailAndStatusOrderBySentAtDesc(email, Status.SENT);
                break;
        }

        // Attach display usernames
        for (Mail m : mails) {
            Optional<User> u = userRepository.findByEmail(m.getFromEmail());
            u.ifPresent(user -> m.setFromUsername(user.getUsername()));
        }

        return mails;
    }

    public Optional<Mail> getMailById(int id) {
        Optional<Mail> mail = mailRepository.findById(id);
        mail.ifPresent(m -> {
            userRepository.findByEmail(m.getFromEmail())
                    .ifPresent(u -> m.setFromUsername(u.getUsername()));
        });
        return mail;
    }

    @Transactional
    public Mail sendMail(String fromEmail, String toEmail, String ccEmail, String bccEmail, String subject, String body) {
        Mail mail = new Mail(fromEmail, toEmail, subject, body, Status.SENT);
        mail.setCcEmail(ccEmail != null ? ccEmail : "");
        mail.setBccEmail(bccEmail != null ? bccEmail : "");
        return mailRepository.save(mail);
    }

    @Transactional
    public Mail saveDraft(String fromEmail, String toEmail, String ccEmail, String bccEmail, String subject, String body, Integer existingMailId) {
        Mail mail;
        if (existingMailId != null && existingMailId > 0) {
            Optional<Mail> optMail = mailRepository.findById(existingMailId);
            if (optMail.isPresent()) {
                mail = optMail.get();
                mail.setToEmail(toEmail);
                mail.setCcEmail(ccEmail != null ? ccEmail : "");
                mail.setBccEmail(bccEmail != null ? bccEmail : "");
                mail.setSubject(subject);
                mail.setBody(body);
                mail.setStatus(Status.DRAFT);
                return mailRepository.save(mail);
            }
        }
        mail = new Mail(fromEmail, toEmail, subject, body, Status.DRAFT);
        mail.setCcEmail(ccEmail != null ? ccEmail : "");
        mail.setBccEmail(bccEmail != null ? bccEmail : "");
        return mailRepository.save(mail);
    }

    @Transactional
    public boolean markAsRead(int id, boolean isRead) {
        Optional<Mail> mail = mailRepository.findById(id);
        if (mail.isPresent()) {
            mail.get().setRead(isRead);
            mailRepository.save(mail.get());
            return true;
        }
        return false;
    }

    @Transactional
    public boolean toggleStar(int id) {
        Optional<Mail> mail = mailRepository.findById(id);
        if (mail.isPresent()) {
            Mail m = mail.get();
            m.setStarred(!m.isStarred());
            mailRepository.save(m);
            return m.isStarred();
        }
        return false;
    }

    @Transactional
    public boolean toggleImportant(int id) {
        Optional<Mail> mail = mailRepository.findById(id);
        if (mail.isPresent()) {
            Mail m = mail.get();
            m.setImportant(!m.isImportant());
            mailRepository.save(m);
            return m.isImportant();
        }
        return false;
    }

    @Transactional
    public boolean moveToTrash(int id) {
        Optional<Mail> mail = mailRepository.findById(id);
        if (mail.isPresent()) {
            Mail m = mail.get();
            m.setStatus(Status.DELETED);
            mailRepository.save(m);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean restoreFromTrash(int id) {
        Optional<Mail> mail = mailRepository.findById(id);
        if (mail.isPresent()) {
            Mail m = mail.get();
            m.setStatus(Status.SENT);
            mailRepository.save(m);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean deletePermanently(int id) {
        if (mailRepository.existsById(id)) {
            mailRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean moveToSpam(int id) {
        Optional<Mail> mail = mailRepository.findById(id);
        if (mail.isPresent()) {
            Mail m = mail.get();
            m.setStatus(Status.SPAM);
            mailRepository.save(m);
            return true;
        }
        return false;
    }

    @Transactional
    public void emptyTrash(String email) {
        List<Mail> trashMails = mailRepository.findTrashMails(email);
        mailRepository.deleteAll(trashMails);
    }

    public List<Mail> searchMails(String email, String query) {
        List<Mail> mails = mailRepository.searchMails(email, query);
        for (Mail m : mails) {
            userRepository.findByEmail(m.getFromEmail())
                    .ifPresent(u -> m.setFromUsername(u.getUsername()));
        }
        return mails;
    }

    public long getUnreadCount(String email) {
        return mailRepository.countByToEmailAndStatusAndIsReadFalse(email, Status.SENT);
    }

    public Map<String, Long> getFolderCounts(String email) {
        Map<String, Long> counts = new HashMap<>();
        counts.put("inbox", mailRepository.countByToEmailAndStatus(email, Status.SENT));
        counts.put("sent", mailRepository.countByFromEmailAndStatus(email, Status.SENT));
        counts.put("drafts", mailRepository.countByFromEmailAndStatus(email, Status.DRAFT));
        counts.put("starred", mailRepository.countStarredMails(email));
        counts.put("important", mailRepository.countImportantMails(email));
        counts.put("trash", mailRepository.countTrashMails(email));
        counts.put("unread", getUnreadCount(email));
        return counts;
    }
}
