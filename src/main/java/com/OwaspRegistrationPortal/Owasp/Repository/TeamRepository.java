package com.OwaspRegistrationPortal.Owasp.Repository;

import com.OwaspRegistrationPortal.Owasp.Entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Team findByLeaderRollNo(String leaderRollNo);
}
