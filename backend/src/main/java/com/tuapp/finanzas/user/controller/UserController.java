package com.tuapp.finanzas.user.controller;

import com.tuapp.finanzas.user.dto.CreateUserRequest;
import com.tuapp.finanzas.user.dto.UserDto;
import com.tuapp.finanzas.user.dto.UserSessionDto;
import com.tuapp.finanzas.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile() {
        // In a real app, username would be extracted from the SecurityContext
        String currentUsername = "testuser"; 
        return ResponseEntity.ok(userService.getProfile(currentUsername));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@RequestBody PasswordUpdateRequest req) {
        String currentUsername = "testuser";
        userService.updatePassword(currentUsername, req.getCurrentPassword(), req.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<UserSessionDto>> getSessions() {
        String currentUsername = "testuser";
        return ResponseEntity.ok(userService.getActiveSessions(currentUsername));
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Void> terminateOtherSessions() {
        String currentUsername = "testuser";
        String currentToken = "fake-token";
        userService.terminateOtherSessions(currentUsername, currentToken);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> list() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> get(@PathVariable Long id) {
        UserDto u = userService.findById(id);
        return u == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(u);
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest req) {
        UserDto created = userService.create(req);
        return ResponseEntity.ok(created);
    }

    public static class PasswordUpdateRequest {
        private String currentPassword;
        private String newPassword;
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String cp) { this.currentPassword = cp; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String np) { this.newPassword = np; }
    }
}
