package com.OwaspRegistrationPortal.Owasp.Services;

import com.OwaspRegistrationPortal.Owasp.Entity.JoinRequest;
import com.OwaspRegistrationPortal.Owasp.Entity.Team;
import com.OwaspRegistrationPortal.Owasp.Entity.User;
import com.OwaspRegistrationPortal.Owasp.Repository.JoinRequestRepository;
import com.OwaspRegistrationPortal.Owasp.Repository.TeamRepository;
import com.OwaspRegistrationPortal.Owasp.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JoinRequestRepository joinRequestRepository;


    public ResponseEntity<String> createTeam(Team team) {
        if (teamRepository.findByLeaderRollNo(team.getLeaderRollNo()) != null) {
            return ResponseEntity.badRequest().body("Team leader already has a team.");
        }
        User user = userRepository.findByRollNo(team.getLeaderRollNo());
        user.setTeam(team.getName());
        user.setPosition("leader");
        teamRepository.save(team);
        return ResponseEntity.ok("Team created successfully.");
    }

    public ResponseEntity<String> addMemberToTeam(Long teamId, User member) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return ResponseEntity.badRequest().body("Team not found.");
        }
        // Add member to the team
        // Logic for adding members in available slots (you can customize this part)
//        team.setMemberName(member.getUsername());
//        team.setMemberRollNo(member.getRollNo());
//        team.setMemberPhoneNo(member.getPhoneNumber());
        teamRepository.save(team);
        return ResponseEntity.ok("Member added to team.");
    }

    public ResponseEntity<String> removeMemberFromTeam(Long teamId, Long memberId) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return ResponseEntity.badRequest().body("Team not found.");
        }
        // Logic to remove a member from the team
//        team.setMemberName(null); // Clear member data
        teamRepository.save(team);
        return ResponseEntity.ok("Member removed from team.");
    }

    public ResponseEntity<String> requestJoinTeam(Long teamId, User user) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return ResponseEntity.badRequest().body("Team not found.");
        }

        // Check if the user has already requested to join this team
        JoinRequest existingRequest = joinRequestRepository.findByTeamIdAndUserId(teamId, user.getId());
        if (existingRequest != null && existingRequest.getStatus().equals("PENDING")) {
            return ResponseEntity.badRequest().body("Join request already pending.");
        }

        // Create a new join request
        JoinRequest newRequest = new JoinRequest();
        newRequest.setTeamId(teamId);
        newRequest.setUserId(user.getId());
        newRequest.setStatus("PENDING");

        joinRequestRepository.save(newRequest);
        return ResponseEntity.ok("Join request sent.");
    }

    public ResponseEntity<String> respondJoinRequest(Long teamId, Long userId, boolean accept) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return ResponseEntity.badRequest().body("Team not found.");
        }

        // Find the join request
        JoinRequest joinRequest = joinRequestRepository.findByTeamIdAndUserId(teamId, userId);
        if (joinRequest == null || !joinRequest.getStatus().equals("PENDING")) {
            return ResponseEntity.badRequest().body("No pending join request found.");
        }

        // If accepted, add the user to the team (you can check for available slots or other conditions)
        if (accept) {
            // Logic to add user to team (e.g., assign user to a specific team member position)
            // This example assumes there is space for the user (you can modify this logic to add the user in the correct slot)
            Team updatedTeam = team;
            // Add user to team (adjust team member fields as per your team's structure)
//            updatedTeam.setMemberName(userRepository.findById(userId).get().getUsername());
            teamRepository.save(updatedTeam);

            joinRequest.setStatus("ACCEPTED");
            joinRequestRepository.save(joinRequest);
            return ResponseEntity.ok("Join request accepted.");
        } else {
            joinRequest.setStatus("REJECTED");
            joinRequestRepository.save(joinRequest);
            return ResponseEntity.ok("Join request rejected.");
        }
    }
}
