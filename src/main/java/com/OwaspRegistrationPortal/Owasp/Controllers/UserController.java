package com.OwaspRegistrationPortal.Owasp.Controllers;

import com.OwaspRegistrationPortal.Owasp.Entity.User;
import com.OwaspRegistrationPortal.Owasp.Services.UserService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    // Register a user
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserByIdWithTeam(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Send Verification token to user email
    @GetMapping("/{userId}/send-token")
    public ResponseEntity<?> sendVerificationToken(@PathVariable Long userId) {
        return userService.sendVerificationToken(userId);
    }

    // Verify user
    @PostMapping("/{userId}/verify/{token}")
    public ResponseEntity<?> verifyUser(@PathVariable Long userId, @PathVariable String token) {
        return userService.verify(userId, token);
    }

    // Login a user
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        return userService.loginUser(user);
    }

    // Update user details
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUserDetails(@PathVariable Long id, @RequestBody User updatedUser) {
        return userService.updateUserDetails(id, updatedUser);
    }

    // Get all users (optional, for admin)
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return userService.getAllUsers();
    }
}
