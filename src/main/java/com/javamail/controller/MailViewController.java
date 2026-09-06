package com.javamail.controller;

import com.javamail.model.Mail;
import com.javamail.model.User;
import com.javamail.service.MailService;
import com.javamail.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class MailViewController {

    private final MailService mailService;
    private final UserService userService;

    @Autowired
    public MailViewController(MailService mailService, UserService userService) {
        this.mailService = mailService;
        this.userService = userService;
    }

    private User getAuthenticatedUser(@AuthenticationPrincipal User principal, HttpSession session) {
        if (principal != null) {
            session.setAttribute("user", principal);
            return principal;
        }
        return (User) session.getAttribute("user");
    }

    private void populateSidebarData(User currentUser, Model model) {
        if (currentUser == null)
            return;
        Map<String, Long> counts = mailService.getFolderCounts(currentUser.getEmail());
        model.addAttribute("counts", counts);
        model.addAttribute("unreadCount", counts.getOrDefault("unread", 0L));
    }

    @GetMapping({ "/mailbox", "/mail/inbox", "/mail/sent", "/mail/draft", "/mail/starred", "/mail/important",
            "/mail/trash", "/mail/spam", "/mail/search" })
    public String mailbox(@RequestParam(value = "folder", required = false) String folderParam,
            @RequestParam(value = "q", required = false) String query,
            HttpServletRequest request,
            @AuthenticationPrincipal User principal,
            HttpSession session,
            Model model) {
        User user = getAuthenticatedUser(principal, session);
        if (user == null)
            return "redirect:/login";

        String path = request.getRequestURI();
        String folder = folderParam;
        if (folder == null || folder.isEmpty()) {
            if (path.contains("/mail/sent"))
                folder = "sent";
            else if (path.contains("/mail/draft"))
                folder = "drafts";
            else if (path.contains("/mail/starred"))
                folder = "starred";
            else if (path.contains("/mail/important"))
                folder = "important";
            else if (path.contains("/mail/trash"))
                folder = "trash";
            else if (path.contains("/mail/spam"))
                folder = "spam";
            else
                folder = "inbox";
        }

        model.addAttribute("user", user);
        model.addAttribute("folder", folder);

        List<Mail> mails;
        if (query != null && !query.trim().isEmpty()) {
            mails = mailService.searchMails(user.getEmail(), query.trim());
            model.addAttribute("q", query);
        } else {
            mails = mailService.getMailsByFolder(user.getEmail(), folder);
        }

        model.addAttribute("mails", mails);
        populateSidebarData(user, model);

        return "mailbox";
    }

    @GetMapping({ "/viewmail", "/mail/view" })
    public String viewMail(@RequestParam("id") int id,
            @AuthenticationPrincipal User principal,
            HttpSession session,
            Model model) {
        User user = getAuthenticatedUser(principal, session);
        if (user == null)
            return "redirect:/login";

        model.addAttribute("user", user);

        Optional<Mail> optMail = mailService.getMailById(id);
        if (optMail.isPresent()) {
            Mail mail = optMail.get();
            if (user.getEmail().equalsIgnoreCase(mail.getToEmail()) && !mail.isRead()) {
                mailService.markAsRead(id, true);
                mail.setRead(true);
            }
            model.addAttribute("mail", mail);
        } else {
            model.addAttribute("errorMessage", "Mail not found!");
            return "redirect:/mailbox";
        }

        populateSidebarData(user, model);
        return "viewmail";
    }

    @GetMapping({ "/compose", "/mail/compose", "/mail/reply", "/mail/forward" })
    public String compose(@RequestParam(value = "to", required = false) String to,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "body", required = false) String body,
            @RequestParam(value = "draftId", required = false) Integer draftId,
            @RequestParam(value = "id", required = false) Integer mailId,
            HttpServletRequest request,
            @AuthenticationPrincipal User principal,
            HttpSession session,
            Model model) {
        User user = getAuthenticatedUser(principal, session);
        if (user == null)
            return "redirect:/login";

        model.addAttribute("user", user);
        String uri = request.getRequestURI();

        if (mailId != null && mailId > 0) {
            Optional<Mail> optMail = mailService.getMailById(mailId);
            if (optMail.isPresent()) {
                Mail original = optMail.get();
                if (uri.contains("/reply")) {
                    to = original.getFromEmail();
                    subject = original.getSubject().startsWith("Re:") ? original.getSubject()
                            : "Re: " + original.getSubject();
                    body = "\n\n--- Original Message ---\nFrom: " + original.getFromEmail() + "\nDate: "
                            + original.getFormattedDate() + "\nSubject: " + original.getSubject() + "\n\n"
                            + original.getBody();
                } else if (uri.contains("/forward")) {
                    subject = original.getSubject().startsWith("Fwd:") ? original.getSubject()
                            : "Fwd: " + original.getSubject();
                    body = "\n\n--- Forwarded Message ---\nFrom: " + original.getFromEmail() + "\nTo: "
                            + original.getToEmail() + "\nDate: " + original.getFormattedDate() + "\nSubject: "
                            + original.getSubject() + "\n\n" + original.getBody();
                }
            }
        }

        model.addAttribute("to", to != null ? to : "");
        model.addAttribute("subject", subject != null ? subject : "");
        model.addAttribute("body", body != null ? body : "");

        if (draftId != null && draftId > 0) {
            Optional<Mail> draft = mailService.getMailById(draftId);
            draft.ifPresent(m -> {
                model.addAttribute("mail", m);
                model.addAttribute("to", m.getToEmail());
                model.addAttribute("subject", m.getSubject());
                model.addAttribute("body", m.getBody());
            });
        }

        populateSidebarData(user, model);
        return "compose";
    }

    @PostMapping({ "/sendmail", "/mail/send", "/mail/draft/save" })
    public String sendMail(@RequestParam(value = "toEmail", required = false) String toEmailParam,
            @RequestParam(value = "to", required = false) String toParam,
            @RequestParam(value = "ccEmail", required = false) String ccEmailParam,
            @RequestParam(value = "cc", required = false) String ccParam,
            @RequestParam(value = "bccEmail", required = false) String bccEmailParam,
            @RequestParam(value = "bcc", required = false) String bccParam,
            @RequestParam(value = "subject", required = false, defaultValue = "") String subject,
            @RequestParam(value = "body", required = false, defaultValue = "") String body,
            @RequestParam(value = "action", required = false, defaultValue = "send") String action,
            @RequestParam(value = "draftId", required = false) Integer draftId,
            HttpServletRequest request,
            @AuthenticationPrincipal User principal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(principal, session);
        if (user == null)
            return "redirect:/login";

        String toEmail = (toEmailParam != null && !toEmailParam.trim().isEmpty()) ? toEmailParam.trim()
                : (toParam != null ? toParam.trim() : "");
        String ccEmail = (ccEmailParam != null && !ccEmailParam.trim().isEmpty()) ? ccEmailParam.trim()
                : (ccParam != null ? ccParam.trim() : "");
        String bccEmail = (bccEmailParam != null && !bccEmailParam.trim().isEmpty()) ? bccEmailParam.trim()
                : (bccParam != null ? bccParam.trim() : "");

        boolean isDraft = "draft".equalsIgnoreCase(action) || request.getRequestURI().contains("/draft");

        if (!isDraft && toEmail.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Recipient email ('To') is required.");
            return "redirect:/compose";
        }

        if (isDraft) {
            mailService.saveDraft(user.getEmail(), toEmail, ccEmail, bccEmail, subject, body, draftId);
            redirectAttributes.addFlashAttribute("successMessage", "Draft saved successfully.");
            return "redirect:/mailbox?folder=drafts";
        } else {
            mailService.sendMail(user.getEmail(), toEmail, ccEmail, bccEmail, subject, body);
            if (draftId != null && draftId > 0) {
                mailService.deletePermanently(draftId);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Mail sent successfully.");
            return "redirect:/mailbox?folder=sent";
        }
    }

    @PostMapping("/mail/delete")
    public String deletePost(@RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "mailId", required = false) Integer mailId,
            @RequestParam(value = "redirect", required = false, defaultValue = "inbox") String folder,
            RedirectAttributes redirectAttributes) {
        int resolvedId = id != null ? id : (mailId != null ? mailId : 0);
        if (resolvedId > 0) {
            mailService.moveToTrash(resolvedId);
            redirectAttributes.addFlashAttribute("successMessage", "Mail moved to trash.");
        }
        return "redirect:/mailbox?folder=" + folder;
    }

    @PostMapping("/mail/spam")
    public String spamPost(@RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "mailId", required = false) Integer mailId,
            @RequestParam(value = "redirect", required = false, defaultValue = "inbox") String folder,
            RedirectAttributes redirectAttributes) {
        int resolvedId = id != null ? id : (mailId != null ? mailId : 0);
        if (resolvedId > 0) {
            mailService.moveToSpam(resolvedId);
            redirectAttributes.addFlashAttribute("successMessage", "Mail marked as spam.");
        }
        return "redirect:/mailbox?folder=spam";
    }

    @PostMapping("/mail/star")
    public String toggleStarPost(@RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "mailId", required = false) Integer mailId,
            @RequestParam(value = "redirect", required = false, defaultValue = "inbox") String folder,
            RedirectAttributes redirectAttributes) {
        int resolvedId = id != null ? id : (mailId != null ? mailId : 0);
        if (resolvedId > 0)
            mailService.toggleStar(resolvedId);
        return "redirect:/viewmail?id=" + resolvedId;
    }

    @PostMapping("/mail/important")
    public String toggleImportantPost(@RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "mailId", required = false) Integer mailId,
            @RequestParam(value = "redirect", required = false, defaultValue = "inbox") String folder,
            RedirectAttributes redirectAttributes) {
        int resolvedId = id != null ? id : (mailId != null ? mailId : 0);
        if (resolvedId > 0)
            mailService.toggleImportant(resolvedId);
        return "redirect:/viewmail?id=" + resolvedId;
    }

    @PostMapping("/mail/restore")
    public String restorePost(@RequestParam(value = "id", required = false) Integer id,
            @RequestParam(value = "mailId", required = false) Integer mailId,
            RedirectAttributes redirectAttributes) {
        int resolvedId = id != null ? id : (mailId != null ? mailId : 0);
        if (resolvedId > 0) {
            mailService.restoreFromTrash(resolvedId);
            redirectAttributes.addFlashAttribute("successMessage", "Mail restored to inbox.");
        }
        return "redirect:/mailbox?folder=inbox";
    }

    @PostMapping("/mail/empty-trash")
    public String emptyTrash(@AuthenticationPrincipal User principal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(principal, session);
        if (user == null)
            return "redirect:/login";
        mailService.emptyTrash(user.getEmail());
        redirectAttributes.addFlashAttribute("successMessage", "Trash emptied.");
        return "redirect:/mailbox?folder=trash";
    }

    @GetMapping({ "/profile", "/user/profile" })
    public String profile(@AuthenticationPrincipal User principal,
            HttpSession session,
            Model model) {
        User user = getAuthenticatedUser(principal, session);
        if (user == null)
            return "redirect:/login";

        Optional<User> freshUser = userService.findByEmail(user.getEmail());
        freshUser.ifPresent(u -> {
            model.addAttribute("user", u);
            session.setAttribute("user", u);
        });

        populateSidebarData(user, model);
        return "profile";
    }

    @PostMapping({ "/profile/update", "/user/profile" })
    public String updateProfile(@RequestParam("username") String username,
            @RequestParam(value = "dob", required = false) String dob,
            @RequestParam(value = "contact", required = false) String contact,
            @AuthenticationPrincipal User principal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(principal, session);
        if (user == null)
            return "redirect:/login";

        boolean updated = userService.updateProfile(user.getEmail(), username, dob, contact);
        if (updated) {
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update profile.");
        }
        return "redirect:/profile";
    }

    @PostMapping({ "/profile/password", "/user/password" })
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @AuthenticationPrincipal User principal,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = getAuthenticatedUser(principal, session);
        if (user == null)
            return "redirect:/login";

        boolean changed = userService.changePassword(user.getEmail(), currentPassword, newPassword);
        if (changed) {
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
        }
        return "redirect:/profile";
    }
}
