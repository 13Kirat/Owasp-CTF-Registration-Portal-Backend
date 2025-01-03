package com.OwaspRegistrationPortal.Owasp.Services;

import com.OwaspRegistrationPortal.Owasp.Entity.JoinRequest;
import com.OwaspRegistrationPortal.Owasp.Entity.Team;
import com.OwaspRegistrationPortal.Owasp.Entity.User;
import com.OwaspRegistrationPortal.Owasp.Repository.JoinRequestRepository;
import com.OwaspRegistrationPortal.Owasp.Repository.TeamRepository;
import com.OwaspRegistrationPortal.Owasp.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;


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

    // Helper method to create a map for team details
    private Map<String, Object> createTeamDetailsMap(Team team) {
        Map<String, Object> teamDetails = new HashMap<>();
        teamDetails.put("teamId", team.getId());
        teamDetails.put("teamName", team.getTeamName());
        teamDetails.put("teamToken", team.getTeamToken());
        teamDetails.put("leaderName", team.getLeaderName());
        teamDetails.put("leaderRollNo", team.getLeaderRollNo());
        teamDetails.put("leaderPhoneNo", team.getLeaderPhoneNo());
        return teamDetails;
    }

    // Helper method to create a map for user details
    private Map<String, Object> createUserDetailsMap(User user) {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("userId", user.getId());
        userDetails.put("username", user.getUsername());
        userDetails.put("email", user.getEmail());
        userDetails.put("phoneNumber", user.getPhoneNumber());
        userDetails.put("rollNo", user.getRollNo());
        return userDetails;
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

    public ResponseEntity<?> createTeam(Team team) {
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

        // Check if the leader has any pending or accepted join requests
        List<JoinRequest> leaderJoinRequests = joinRequestRepository.findByUserId(leader.getId());
        for (JoinRequest joinRequest : leaderJoinRequests) {
            if ("PENDING".equals(joinRequest.getStatus()) || "ACCEPTED".equals(joinRequest.getStatus())) {
                return ResponseEntity.badRequest().body("Leader has a pending or accepted join request and cannot create a new team.");
            }
        }

        // Auto-generate a 6-letter alphanumeric team token
        String teamToken = generateTeamToken();
        team.setTeamToken(teamToken);

        // Set leader details in the team
        team.setLeaderName(leader.getUsername());
        team.setLeaderRollNo(leader.getRollNo());
        team.setLeaderPhoneNo(leader.getPhoneNumber());

        // Add the leader to the team's members list
        if (team.getMembers() == null) {
            team.setMembers(new ArrayList<>());
        }
        team.getMembers().add(leader);

        // Associate the leader with the new team
        leader.setTeam(team); // Set the `team` object reference
        leader.setPosition("leader"); // Update the role to "leader"
        leader.setTeamName(team.getTeamName());

        // Save the team and leader updates
        Team savedTeam = teamRepository.save(team);
        userRepository.save(leader);

        // Create response object
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Team created successfully.");
        response.put("teamToken", teamToken);
        response.put("teamDetails", savedTeam);

        return ResponseEntity.ok(response);
    }

    // Method to delete a team
    @Transactional
    public ResponseEntity<?> deleteTeam(Long teamId, Long userId) {
        // Fetch the team by ID
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            return ResponseEntity.badRequest().body("Team not found.");
        }

        // Fetch the user by ID (team leader verification)
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found.");
        }

        // Check if the user is the team leader
        if (!user.getRollNo().equals(team.getLeaderRollNo())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only the team leader can delete the team.");
        }

        // Prepare a response object with team details before deletion
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Team and all related data cleared successfully.");
        response.put("deletedTeamDetails", team);

        // Get all team members from the team's member list
        List<User> teamMembers = team.getMembers();

        // Clear the team for each member
        for (User member : teamMembers) {
            member.setTeam(null);
            member.setPosition(null);
            member.setTeamName(null);
            userRepository.save(member); // Update each user's status in the database
        }

        // Clear the team member list explicitly (optional)
        team.getMembers().clear();

        // Delete the team
        teamRepository.delete(team);

        // Return the response
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> requestJoinTeam(String teamToken, User user) {
        Map<String, Object> response = new HashMap<>();

        // Find the team by its token
        Team team = teamRepository.findByTeamToken(teamToken);
        if (team == null) {
            response.put("message", "Invalid team token. Team not found.");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if the user exists in the database
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser == null) {
            response.put("message", "User not found.");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if the user is already part of a team
        if (existingUser.getTeam() != null) {
            response.put("message", "User is already part of a team.");
            response.put("user", existingUser);
            response.put("team", team);
            return ResponseEntity.badRequest().body(response);
        }

        // Check if the user already has a pending join request for another team
        JoinRequest alreadyExistingRequest = joinRequestRepository.findByUserIdAndStatus(existingUser.getId(), "PENDING");
        if (alreadyExistingRequest != null) {
            response.put("message", "User already has a pending join request to another team.");
            response.put("user", existingUser);
            response.put("team", team);
            return ResponseEntity.badRequest().body(response);
        }

        // Check if the team already has the maximum number of members
        if (team.getMembers().size() >= 5) {
            response.put("message", "Team is already full. Cannot accept more members.");
            response.put("user", existingUser);
            response.put("team", team);
            return ResponseEntity.badRequest().body(response);
        }

        // Check if the user already sent a join request to this team
        JoinRequest existingRequest = joinRequestRepository.findByTeamIdAndUserId(team.getId(), existingUser.getId());
        if (existingRequest != null) {
            if ("PENDING".equals(existingRequest.getStatus())) {
                response.put("message", "A join request to this team is already pending.");
                response.put("user", existingUser);
                response.put("team", team);
                response.put("requestStatus", existingRequest.getStatus());
                return ResponseEntity.badRequest().body(response);
            } else if ("ACCEPTED".equals(existingRequest.getStatus())) {
                response.put("message", "You are already a member of this team.");
                response.put("user", existingUser);
                response.put("team", team);
                response.put("requestStatus", existingRequest.getStatus());
                return ResponseEntity.badRequest().body(response);
            } else if ("REJECTED".equals(existingRequest.getStatus())) {
                // Allow re-submission if the previous request was rejected
                existingRequest.setStatus("PENDING");
                joinRequestRepository.save(existingRequest);
                response.put("message", "Join request re-submitted successfully.");
                response.put("user", existingUser);
                response.put("team", team);
                response.put("requestStatus", "PENDING");
                return ResponseEntity.ok().body(response);
            }
        }

        // Create a new join request
        JoinRequest newRequest = new JoinRequest();
        newRequest.setTeamId(team.getId());
        newRequest.setUserId(existingUser.getId());
        newRequest.setStatus("PENDING");

        joinRequestRepository.save(newRequest);
        response.put("message", "Join request sent successfully.");
        response.put("user", existingUser);
        response.put("team", team);
        response.put("requestStatus", "PENDING");
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<List<Map<String, Object>>> getJoinRequests(String teamToken) {
        // Create a list to hold the responses
        List<Map<String, Object>> responseList = new ArrayList<>();

        // Find the team by its token
        Team team = teamRepository.findByTeamToken(teamToken);
        if (team == null) {
            return ResponseEntity.badRequest().body(null);
        }

        // Get all join requests for the team
        List<JoinRequest> joinRequests = joinRequestRepository.findByTeamId(team.getId());

        // Loop through the join requests and create a response with detailed information
        for (JoinRequest joinRequest : joinRequests) {
            Map<String, Object> responseMap = new HashMap<>();

            // Get the user details associated with this join request
            User user = userRepository.findById(joinRequest.getUserId()).orElse(null);
            if (user != null) {
                responseMap.put("requestId", joinRequest.getId());
                responseMap.put("team", createTeamDetailsMap(team));
                responseMap.put("user", createUserDetailsMap(user));
                responseMap.put("status", joinRequest.getStatus());
            }

            // Add the response map to the list
            responseList.add(responseMap);
        }

        return ResponseEntity.ok(responseList);
    }

    public ResponseEntity<Map<String, Object>> respondJoinRequest(Long teamId, Long userId, boolean accept) {
        // Create a response map to hold the response details
        Map<String, Object> responseMap = new HashMap<>();

        // Find the team by its ID
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            responseMap.put("message", "Team not found.");
            return ResponseEntity.badRequest().body(responseMap);
        }

        // Find the join request for the user
        JoinRequest joinRequest = joinRequestRepository.findByTeamIdAndUserId(teamId, userId);
        if (joinRequest == null || !joinRequest.getStatus().equals("PENDING")) {
            responseMap.put("message", "No pending join request found.");
            return ResponseEntity.badRequest().body(responseMap);
        }

        if (accept) {
            // Check if there is space in the team
            if (team.getMembers().size() >= 5) {
                responseMap.put("message", "Team is already full.");
                return ResponseEntity.badRequest().body(responseMap);
            }

            // Assign user to team
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                responseMap.put("message", "User not found.");
                return ResponseEntity.badRequest().body(responseMap);
            }

            user.setTeam(team); // Link the user to the team
            user.setPosition("member"); // Assign position
            user.setTeamName(team.getTeamName()); // Assign team name
            userRepository.save(user);

            // Add the user to the team's members list
            team.getMembers().add(user);
            teamRepository.save(team);

            // Update join request status to accepted
            joinRequest.setStatus("ACCEPTED");
            joinRequestRepository.save(joinRequest);

            // Populate the response map
            responseMap.put("message", "Join request accepted.");
            responseMap.put("status", "ACCEPTED");
            responseMap.put("requestId", joinRequest.getId());
            responseMap.put("user", createUserDetailsMap(user));
            responseMap.put("team", createTeamDetailsMap(team));

            return ResponseEntity.ok(responseMap);
        } else {
            // Reject the join request
            joinRequest.setStatus("REJECTED");
            joinRequestRepository.save(joinRequest);

            // Populate the response map for rejection
            responseMap.put("message", "Join request rejected.");
            responseMap.put("status", "REJECTED");
            responseMap.put("requestId", joinRequest.getId());
            responseMap.put("user", createUserDetailsMap(Objects.requireNonNull(userRepository.findById(userId).orElse(null)))); // Add user details
            responseMap.put("team", createTeamDetailsMap(team));

            return ResponseEntity.ok(responseMap);
        }
    }

    public ResponseEntity<Map<String, Object>> removeMemberFromTeam(Long teamId, Long memberId) {
        // Create a response map to hold the response details
        Map<String, Object> responseMap = new HashMap<>();

        // Find the team
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            responseMap.put("message", "Team not found.");
            return ResponseEntity.badRequest().body(responseMap);
        }

        // Find the user to be removed
        User user = userRepository.findById(memberId).orElse(null);
        if (user == null) {
            responseMap.put("message", "Member not found.");
            return ResponseEntity.badRequest().body(responseMap);
        }

        // Check if the user is part of the team
        if (!user.getTeam().getTeamName().equals(team.getTeamName())) {
            responseMap.put("message", "User is not a member of this team.");
            return ResponseEntity.badRequest().body(responseMap);
        }

        // Ensure the leader is the one performing the action
        User leader = userRepository.findByRollNo(team.getLeaderRollNo());
        if (leader.getId().equals(memberId)) {
            responseMap.put("message", "The team leader cannot remove themselves.");
            return ResponseEntity.badRequest().body(responseMap);
        }

        // Find the join request for the user
        JoinRequest joinRequest = joinRequestRepository.findByTeamIdAndUserId(teamId, memberId);
        if (joinRequest != null) {
            // Set the status of the join request to REJECTED
            joinRequest.setStatus("REJECTED");
            joinRequestRepository.save(joinRequest);
        }

        // Remove the user from the team
        user.setTeam(null);
        user.setPosition(null);
        user.setTeamName(null);
        userRepository.save(user);

        // Remove the user from the team's member list
        List<User> teamMembers = team.getMembers();
        teamMembers.removeIf(member -> member.getId().equals(memberId));
        team.setMembers(teamMembers);

        teamRepository.save(team);

        // Populate the response map with message, user details, and team details
        responseMap.put("message", "Member removed from the team.");
        responseMap.put("user", createUserDetailsMap(user));
        responseMap.put("team", createTeamDetailsMap(team));
        responseMap.put("joinRequestStatus", "REJECTED");

        return ResponseEntity.ok(responseMap);
    }

    public ResponseEntity<Map<String, Object>> deleteJoinRequest(Long teamId, Long userId) {
        // Create a response map to hold the response details
        Map<String, Object> responseMap = new HashMap<>();

        // Find the team by its ID
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            responseMap.put("message", "Team not found.");
            return ResponseEntity.badRequest().body(responseMap);
        }

        // Find the user by its ID
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            responseMap.put("message", "User not found.");
            return ResponseEntity.badRequest().body(responseMap);
        }

        // Find the join request for the user in the team
        JoinRequest joinRequest = joinRequestRepository.findByTeamIdAndUserId(teamId, userId);
        if (joinRequest == null) {
            responseMap.put("message", "No join request found for the user.");
            return ResponseEntity.badRequest().body(responseMap);
        }

        // Check if the join request is in "PENDING" state
        if (!"PENDING".equals(joinRequest.getStatus())) {
            responseMap.put("message", "Join request is not in PENDING state and cannot be deleted.");
            return ResponseEntity.badRequest().body(responseMap);
        }

        // Delete the join request
        joinRequestRepository.delete(joinRequest);

        // Prepare the response map with additional team and user details
        responseMap.put("message", "Join request deleted successfully.");
        responseMap.put("joinRequestId", joinRequest.getId());
        responseMap.put("teamId", teamId);
        responseMap.put("userId", userId);

        // Add team and user details
        Map<String, Object> teamDetails = new HashMap<>();
        teamDetails.put("teamName", team.getTeamName());
        teamDetails.put("leaderRollNo", team.getLeaderRollNo());
        teamDetails.put("membersCount", team.getMembers().size());
        responseMap.put("teamDetails", teamDetails);

        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("username", user.getUsername());
        userDetails.put("email", user.getEmail());
        userDetails.put("phoneNumber", user.getPhoneNumber());
        responseMap.put("userDetails", userDetails);

        return ResponseEntity.ok(responseMap);
    }

    public ResponseEntity<?> getUserJoinRequests(Long userId) {
        Map<String, Object> response = new HashMap<>();

        // Find the user by their ID
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            response.put("message", "User not found.");
            return ResponseEntity.badRequest().body(response);
        }

        // Get the list of join requests made by the user
        List<JoinRequest> joinRequests = joinRequestRepository.findByUserId(userId);

        if (joinRequests.isEmpty()) {
            response.put("message", "No join requests found for this user.");
            return ResponseEntity.ok().body(response);
        }

        // Prepare the response with the join requests, including the team and status
        List<Map<String, Object>> joinRequestDetails = new ArrayList<>();
        for (JoinRequest request : joinRequests) {
            Map<String, Object> requestDetails = new HashMap<>();
            Team team = teamRepository.findById(request.getTeamId()).orElse(null);
            if (team != null) {
                requestDetails.put("team", team);
            }
            requestDetails.put("requestStatus", request.getStatus());
            requestDetails.put("requestId", request.getId());

            joinRequestDetails.add(requestDetails);
        }

        response.put("joinRequests", joinRequestDetails);
        return ResponseEntity.ok().body(response);
    }

    public ResponseEntity<?> getTeamByName(String teamName) {
        Team team = teamRepository.findByTeamName(teamName);
        if(team == null){
            return ResponseEntity.badRequest().body("Team not found");
        }
        return ResponseEntity.ok(team);
    }
}
