package com.OwaspRegistrationPortal.Owasp.Repository;

import com.OwaspRegistrationPortal.Owasp.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);  // For login validation
    User findByThaparEmail(String thaparEmail);
    User findByPhoneNumber(String phoneNumber);
    User findByRollNo(String rollNo);
    User findByUsername(String username);
}
