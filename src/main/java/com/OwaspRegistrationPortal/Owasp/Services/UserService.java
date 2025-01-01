package com.OwaspRegistrationPortal.Owasp.Services;

import com.OwaspRegistrationPortal.Owasp.Entity.User;
import com.OwaspRegistrationPortal.Owasp.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class UserService {

    private String generateVerificationToken() {
        int tokenLength = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < tokenLength; i++) {
            token.append(characters.charAt(random.nextInt(characters.length())));
        }
        return token.toString();
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public ResponseEntity<?> registerUser(User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Email already exists.");
        }
        if (userRepository.findByThaparEmail(user.getThaparEmail()) != null) {
            return ResponseEntity.badRequest().body("Thapar Mail already exists.");
        }
        if (userRepository.findByPhoneNumber(user.getPhoneNumber()) != null) {
            return ResponseEntity.badRequest().body("Phone Number is already used.");
        }
        if (userRepository.findByRollNo(user.getRollNo()) != null) {
            return ResponseEntity.badRequest().body("Roll Number already exists.");
        }
        if (user.getEmail() == null || user.getPassword() == null || user.getEmail().isEmpty() || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("Email and Password are required");
        }

        user.setVerificationToken(generateVerificationToken());
        user.setVerified(false);

        // Save the user
        User savedUser = userRepository.save(user);

        // Create a response with a message and user details
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully.");
        response.put("user", savedUser);

        return ResponseEntity.ok(response);
    }


    public ResponseEntity<?> sendVerificationToken(Long userId) {
        // Fetch the user by ID
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        // Ensure user has a Thapar email
        if (!user.getThaparEmail().endsWith("@thapar.edu")) {
            return ResponseEntity.badRequest().body("User does not have a Thapar email.");
        }

        // Send the verification code to the user's email
        String subject = "Verification Code";
        String message = "Dear " + user.getUsername() + ",\n\nYour verification code is: " + user.getVerificationToken() +
                "\n\nPlease use this code to verify your profile.\n\nRegards,\nOWASP Team";
        emailService.sendEmail(user.getThaparEmail(), subject, message);

        return ResponseEntity.ok("Verification token sent successfully to " + user.getThaparEmail());
    }

    public ResponseEntity<?> verify(Long id, String verificationToken) {
        // Find the user by ID
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found."));
        }

        // Check if the token is valid (ensure the token exists and is correct)
        if (existingUser.getVerificationToken() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Verification token not generated."));
        }

        // Check if the provided token matches the stored token
        if (!existingUser.getVerificationToken().equals(verificationToken)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid verification token."));
        }

        // Set the user as verified
        existingUser.setVerified(true);
//        existingUser.setVerificationToken(null); // Clear the verification token once verified (optional)
        User savedUser = userRepository.save(existingUser);

        // Create response map
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User successfully verified.");
        response.put("user", savedUser);

        return ResponseEntity.ok(response);
    }


    public ResponseEntity<?> loginUser(User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser == null || !existingUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid credentials.");
        }
        // Create a response with a message and user details
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful.");
        response.put("user", existingUser);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> updateUserDetails(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        // Update the fields
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setCollageName(updatedUser.getCollageName());
        existingUser.setYear(updatedUser.getYear());

        // Save the updated user to the database
        User savedUser = userRepository.save(existingUser);

        // Create the response object
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User details updated successfully.");
        response.put("updatedUser", savedUser);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
