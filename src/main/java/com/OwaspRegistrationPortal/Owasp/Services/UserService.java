package com.OwaspRegistrationPortal.Owasp.Services;

import com.OwaspRegistrationPortal.Owasp.Entity.User;
import com.OwaspRegistrationPortal.Owasp.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<String> registerUser(User user) {
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
        if (user.getEmail() == null || user.getPassword() == null || user.getEmail() == "" || user.getPassword() == "") {
            return ResponseEntity.badRequest().body("Email and Password are required");
        }
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully.");
    }

    public ResponseEntity<String> loginUser(User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser == null || !existingUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid credentials.");
        }
        return ResponseEntity.ok("Login successful.");
    }

    public ResponseEntity<String> updateUserDetails(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return ResponseEntity.badRequest().body("User not found.");
        }
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setCollageName(updatedUser.getCollageName());
        existingUser.setYear(updatedUser.getYear());
        userRepository.save(existingUser);
        return ResponseEntity.ok("User details updated successfully.");
    }

    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
