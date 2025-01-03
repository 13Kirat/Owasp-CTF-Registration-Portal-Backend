package com.OwaspRegistrationPortal.Owasp.Controllers;

import com.OwaspRegistrationPortal.Owasp.Entity.JoinRequest;
import com.OwaspRegistrationPortal.Owasp.Entity.Team;
import com.OwaspRegistrationPortal.Owasp.Entity.User;
import com.OwaspRegistrationPortal.Owasp.Repository.TeamRepository;
import com.OwaspRegistrationPortal.Owasp.Repository.UserRepository;
import com.OwaspRegistrationPortal.Owasp.Services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/all")
    public ResponseEntity<List<Team>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/{teamName}")
    public ResponseEntity<?> getTeamByName(@PathVariable String teamName) {
        try {
            ResponseEntity<?> team = teamService.getTeamByName(teamName);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Create a team with a leader
    @PostMapping("/create")
    public ResponseEntity<?> createTeam(@RequestBody Team team) {
        return teamService.createTeam(team);
    }

    // Endpoint to delete a team
    @DeleteMapping("/{teamId}")
    public ResponseEntity<?> deleteTeam(
            @PathVariable Long teamId,
            @RequestParam Long userId) {
        try {
            return teamService.deleteTeam(teamId, userId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the team: " + e.getMessage());
        }
    }

    // Request to join a team
    @PostMapping("/{teamToken}/request-join")
    public ResponseEntity<?> requestJoinTeam(@PathVariable String teamToken, @RequestBody User user) {
        return teamService.requestJoinTeam(teamToken, user);
    }

    // Endpoint to get all join requests for a team
    @GetMapping("/{teamToken}/join-requests")
    public ResponseEntity<List<Map<String, Object>>> getJoinRequests(@PathVariable String teamToken) {
        return teamService.getJoinRequests(teamToken);
    }

    // Endpoint to respond to a join request (accept or reject)
    @PostMapping("/{teamId}/respond-request")
    public ResponseEntity<Map<String, Object>> respondJoinRequest(
            @PathVariable Long teamId,
            @RequestParam Long userId,
            @RequestParam boolean accept) {
        return teamService.respondJoinRequest(teamId, userId, accept);
    }

    // Remove a member from a team
    @DeleteMapping("/{teamId}/remove-member/{memberId}")
    public ResponseEntity<Map<String, Object>> removeMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        return teamService.removeMemberFromTeam(teamId, memberId);
    }

    // Route to delete a join request
    @DeleteMapping("/{teamId}/joinRequests/{userId}")
    public ResponseEntity<Map<String, Object>> deleteJoinRequest(
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        return teamService.deleteJoinRequest(teamId, userId);
    }

    // Get join requests for a specific user
    @GetMapping("/{userId}/joinRequests")
    public ResponseEntity<?> getUserJoinRequests(@PathVariable Long userId) {
        return teamService.getUserJoinRequests(userId);
    }
}