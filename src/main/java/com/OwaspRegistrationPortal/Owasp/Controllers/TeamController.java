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

    // Create a team with a leader
    @PostMapping("/create")
    public ResponseEntity<String> createTeam(@RequestBody Team team) {
        return teamService.createTeam(team);
    }

    // Endpoint to delete a team
    @DeleteMapping("/{teamId}")
    public ResponseEntity<String> deleteTeam(@PathVariable Long teamId) {
        return teamService.deleteTeam(teamId);
    }

    // Request to join a team
    @PostMapping("/{teamToken}/request-join")
    public ResponseEntity<String> requestJoinTeam(@PathVariable String teamToken, @RequestBody User user) {
        return teamService.requestJoinTeam(teamToken, user);
    }

    // Endpoint to get all join requests for a team
    @GetMapping("/{teamToken}/join-requests")
    public ResponseEntity<List<JoinRequest>> getJoinRequests(@PathVariable String teamToken) {
        return teamService.getJoinRequests(teamToken);
    }

    // Endpoint to respond to a join request (accept or reject)
    @PostMapping("/{teamId}/respond-request")
    public ResponseEntity<String> respondJoinRequest(
            @PathVariable Long teamId,
            @RequestParam Long userId,
            @RequestParam boolean accept) {
        return teamService.respondJoinRequest(teamId, userId, accept);
    }

    // Remove a member from a team
    @DeleteMapping("/{teamId}/remove-member/{memberId}")
    public ResponseEntity<String> removeMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        return teamService.removeMemberFromTeam(teamId, memberId);
    }
}


//// On "Yes" Button Click
//function handleAcceptRequest(teamId, userId) {
//    fetch(`/teams/${teamId}/respond-request?userId=${userId}&accept=true`, {
//        method: 'POST',
//    })
//    .then(response => response.json())
//    .then(data => alert(data.message))
//    .catch(error => console.error("Error:", error));
//}
//
//// On "No" Button Click
//function handleRejectRequest(teamId, userId) {
//    fetch(`/teams/${teamId}/respond-request?userId=${userId}&accept=false`, {
//        method: 'POST',
//    })
//    .then(response => response.json())
//    .then(data => alert(data.message))
//    .catch(error => console.error("Error:", error));
//}
