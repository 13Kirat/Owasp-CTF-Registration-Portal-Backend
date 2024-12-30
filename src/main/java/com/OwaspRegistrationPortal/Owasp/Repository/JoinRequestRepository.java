package com.OwaspRegistrationPortal.Owasp.Repository;

import com.OwaspRegistrationPortal.Owasp.Entity.JoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    List<JoinRequest> findByTeamIdAndStatus(Long teamId, String status);
    JoinRequest findByTeamIdAndUserId(Long teamId, Long userId);
    List<JoinRequest> findByTeamId(Long teamId);
}
