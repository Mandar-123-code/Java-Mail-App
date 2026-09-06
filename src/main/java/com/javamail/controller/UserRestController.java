package com.javamail.controller;

import com.javamail.model.User;
import com.javamail.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserRestController {

    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return userService.findByEmail(user.getEmail())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> payload,
                                                             @AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        String username = payload.get("username");
        String dob = payload.get("dob");
        String contact = payload.get("contact");

        boolean updated = userService.updateProfile(user.getEmail(), username, dob, contact);
        response.put("success", updated);
        response.put("message", updated ? "Profile updated successfully" : "Profile update failed");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> payload,
                                                              @AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");

        boolean changed = userService.changePassword(user.getEmail(), currentPassword, newPassword);
        response.put("success", changed);
        response.put("message", changed ? "Password changed successfully" : "Current password incorrect");
        return ResponseEntity.ok(response);
    }
}
