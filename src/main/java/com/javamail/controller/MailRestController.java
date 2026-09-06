package com.javamail.controller;

import com.javamail.model.Mail;
import com.javamail.model.User;
import com.javamail.service.MailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/mails")
public class MailRestController {

    private final MailService mailService;

    @Autowired
    public MailRestController(MailService mailService) {
        this.mailService = mailService;
    }

    @GetMapping
    public ResponseEntity<List<Mail>> getMails(@RequestParam(value = "folder", required = false, defaultValue = "inbox") String folder,
                                              @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        List<Mail> mails = mailService.getMailsByFolder(user.getEmail(), folder);
        return ResponseEntity.ok(mails);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mail> getMailById(@PathVariable("id") int id,
                                            @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        Optional<Mail> mail = mailService.getMailById(id);
        return mail.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMail(@RequestBody Map<String, String> payload,
                                                        @AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        String toEmail = payload.get("toEmail");
        String ccEmail = payload.get("ccEmail");
        String bccEmail = payload.get("bccEmail");
        String subject = payload.get("subject");
        String body = payload.get("body");

        Mail sentMail = mailService.sendMail(user.getEmail(), toEmail, ccEmail, bccEmail, subject, body);
        response.put("success", true);
        response.put("message", "Mail sent successfully.");
        response.put("mail", sentMail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/draft")
    public ResponseEntity<Map<String, Object>> saveDraft(@RequestBody Map<String, String> payload,
                                                         @AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        String toEmail = payload.get("toEmail");
        String ccEmail = payload.get("ccEmail");
        String bccEmail = payload.get("bccEmail");
        String subject = payload.get("subject");
        String body = payload.get("body");
        String draftIdStr = payload.get("draftId");
        Integer draftId = (draftIdStr != null && !draftIdStr.isEmpty()) ? Integer.parseInt(draftIdStr) : null;

        Mail draftMail = mailService.saveDraft(user.getEmail(), toEmail, ccEmail, bccEmail, subject, body, draftId);
        response.put("success", true);
        response.put("message", "Draft saved successfully.");
        response.put("mail", draftMail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/star")
    public ResponseEntity<Map<String, Object>> toggleStar(@PathVariable("id") int id,
                                                          @AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) return ResponseEntity.status(401).build();

        boolean isStarred = mailService.toggleStar(id);
        response.put("success", true);
        response.put("isStarred", isStarred);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/important")
    public ResponseEntity<Map<String, Object>> toggleImportant(@PathVariable("id") int id,
                                                               @AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) return ResponseEntity.status(401).build();

        boolean isImportant = mailService.toggleImportant(id);
        response.put("success", true);
        response.put("isImportant", isImportant);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable("id") int id,
                                                          @RequestParam(value = "isRead", defaultValue = "true") boolean isRead,
                                                          @AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) return ResponseEntity.status(401).build();

        boolean success = mailService.markAsRead(id, isRead);
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/trash")
    public ResponseEntity<Map<String, Object>> moveToTrash(@PathVariable("id") int id,
                                                           @AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) return ResponseEntity.status(401).build();

        boolean success = mailService.moveToTrash(id);
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreFromTrash(@PathVariable("id") int id,
                                                                @AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) return ResponseEntity.status(401).build();

        boolean success = mailService.restoreFromTrash(id);
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePermanently(@PathVariable("id") int id,
                                                                 @AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) return ResponseEntity.status(401).build();

        boolean success = mailService.deletePermanently(id);
        response.put("success", success);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Mail>> searchMails(@RequestParam("q") String query,
                                                 @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        List<Mail> mails = mailService.searchMails(user.getEmail(), query);
        return ResponseEntity.ok(mails);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getFolderStats(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        Map<String, Long> stats = mailService.getFolderCounts(user.getEmail());
        return ResponseEntity.ok(stats);
    }
}
