package com.OwaspRegistrationPortal.Owasp.Services;

import com.OwaspRegistrationPortal.Owasp.Entity.JoinRequest;
import com.OwaspRegistrationPortal.Owasp.Entity.Team;
import com.OwaspRegistrationPortal.Owasp.Entity.User;
import com.OwaspRegistrationPortal.Owasp.Repository.JoinRequestRepository;
import com.OwaspRegistrationPortal.Owasp.Repository.TeamRepository;
import com.OwaspRegistrationPortal.Owasp.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;


@Service
public class TeamService {

    private String generateTeamToken() {
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
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JoinRequestRepository joinRequestRepository;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public ResponseEntity<String> createTeam(Team team) {
        // Check if the team name already exists
        if (teamRepository.findByTeamName(team.getTeamName()) != null) {
            return ResponseEntity.badRequest().body("Team name is already in use. Please choose another name.");
        }

        // Fetch the leader user by roll number
        User leader = userRepository.findByRollNo(team.getLeaderRollNo());
        if (leader == null) {
            return ResponseEntity.badRequest().body("Leader with the provided roll number does not exist.");
        }

        // Ensure the leader is not already part of another team
        if (leader.getTeam() != null) {
            return ResponseEntity.badRequest().body("Leader is already part of another team.");
        }

        // Auto-generate a 6-letter alphanumeric team token
        String teamToken = generateTeamToken();
        team.setTeamToken(teamToken);

        // Set leader details in the team
        team.setLeaderName(leader.getUsername());
        team.setLeaderRollNo(leader.getRollNo());
        team.setLeaderPhoneNo(leader.getPhoneNumber());

        // Associate the leader with the new team
        leader.setTeam(team); // Set the `team` object reference
        leader.setPosition("leader"); // Update the role to "leader"

        // Save the team and leader updates
        teamRepository.save(team);
        userRepository.save(leader);

        return ResponseEntity.ok("Team created successfully with token: " + teamToken);
    }

    // Method to delete a team
    @Transactional
    public ResponseEntity<String> deleteTeam(Long teamId) {
        // Fetch the team by ID
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return ResponseEntity.badRequest().body("Team not found.");
        }

        // Get all team members from the Team's member list
        List<User> teamMembers = team.getMembers();

        // Clear the team for each member
        for (User member : teamMembers) {
            member.setTeam(null);
            member.setPosition(null);
            userRepository.save(member); // Update each user's status in the database
        }

        // Clear the team member list
//        team.getMembers().clear();

        // Delete the team
        teamRepository.delete(team);

        return ResponseEntity.ok("Team and all related data cleared successfully.");
    }

    public ResponseEntity<String> requestJoinTeam(String teamToken, User user) {
        // Find the team by its token
        Team team = teamRepository.findByTeamToken(teamToken);
        if (team == null) {
            return ResponseEntity.badRequest().body("Invalid team token. Team not found.");
        }

        // Check if the user exists in the database
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser == null) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        // Check if the team already has the maximum number of members
        if (team.getMembers().size() >= 5) {
            return ResponseEntity.badRequest().body("Team is already full. Cannot accept more members.");
        }

        // Check if the user is already part of a team
        if (existingUser.getTeam() != null) {
            return ResponseEntity.badRequest().body("User is already part of a team.");
        }

        // Check if the user already sent a join request to this team
        JoinRequest existingRequest = joinRequestRepository.findByTeamIdAndUserId(team.getId(), existingUser.getId());
        if (existingRequest != null) {
            if ("PENDING".equals(existingRequest.getStatus())) {
                return ResponseEntity.badRequest().body("A join request to this team is already pending.");
            } else if ("ACCEPTED".equals(existingRequest.getStatus())) {
                return ResponseEntity.badRequest().body("You are already a member of this team.");
            }
        }

        // Create a new join request
        JoinRequest newRequest = new JoinRequest();
        newRequest.setTeamId(team.getId());
        newRequest.setUserId(existingUser.getId());
        newRequest.setStatus("PENDING");

        joinRequestRepository.save(newRequest);
        return ResponseEntity.ok("Join request sent successfully.");
    }


    public ResponseEntity<List<JoinRequest>> getJoinRequests(String teamToken) {
        // Find the team by its token
        Team team = teamRepository.findByTeamToken(teamToken);
        if (team == null) {
            return ResponseEntity.badRequest().body(null);
        }

        // Get all join requests for the team
        List<JoinRequest> joinRequests = joinRequestRepository.findByTeamId(team.getId());
        return ResponseEntity.ok(joinRequests);
    }

    public ResponseEntity<String> respondJoinRequest(Long teamId, Long userId, boolean accept) {
        // Find the team by its ID
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return ResponseEntity.badRequest().body("Team not found.");
        }

        // Find the join request for the user
        JoinRequest joinRequest = joinRequestRepository.findByTeamIdAndUserId(teamId, userId);
        if (joinRequest == null || !joinRequest.getStatus().equals("PENDING")) {
            return ResponseEntity.badRequest().body("No pending join request found.");
        }

        if (accept) {
            // Check if there is space in the team
            if (team.getMembers().size() >= 5) {
                return ResponseEntity.badRequest().body("Team is already full.");
            }

            // Assign user to team
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found.");
            }

            user.setTeam(team); // Link the user to the team
            user.setPosition("member"); // Assign position
            userRepository.save(user);

            // Add the user to the team's members list
            team.getMembers().add(user);
            teamRepository.save(team);

            // Update join request status to accepted
            joinRequest.setStatus("ACCEPTED");
            joinRequestRepository.save(joinRequest);

            return ResponseEntity.ok("Join request accepted.");
        } else {
            // Reject the join request
            joinRequest.setStatus("REJECTED");
            joinRequestRepository.save(joinRequest);
            return ResponseEntity.ok("Join request rejected.");
        }
    }

    public ResponseEntity<String> removeMemberFromTeam(Long teamId, Long memberId) {
        // Find the team
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return ResponseEntity.badRequest().body("Team not found.");
        }

        // Find the user to be removed
        User user = userRepository.findById(memberId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("Member not found.");
        }

        // Check if the user is part of the team
        if (!user.getTeam().getTeamName().equals(team.getTeamName())) {
            return ResponseEntity.badRequest().body("User is not a member of this team.");
        }

        // Ensure the leader is the one performing the action
        User leader = userRepository.findByRollNo(team.getLeaderRollNo());
        if (leader.getId().equals(memberId)) {
            return ResponseEntity.badRequest().body("The team leader cannot remove themselves.");
        }

        // Remove the user from the team
        user.setTeam(null);
        user.setPosition(null);
        userRepository.save(user);

        // Remove the user from the team's member list
        List<User> teamMembers = team.getMembers();
        teamMembers.removeIf(member -> member.getId().equals(memberId));
        team.setMembers(teamMembers);

        teamRepository.save(team);
        return ResponseEntity.ok("Member removed from the team.");
    }
}
