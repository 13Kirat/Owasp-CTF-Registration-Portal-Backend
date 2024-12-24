package com.OwaspRegistrationPortal.Owasp.Controllers;

import com.OwaspRegistrationPortal.Owasp.Entity.Team;
import com.OwaspRegistrationPortal.Owasp.Entity.User;
import com.OwaspRegistrationPortal.Owasp.Repository.TeamRepository;
import com.OwaspRegistrationPortal.Owasp.Repository.UserRepository;
import com.OwaspRegistrationPortal.Owasp.Services.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    // Create a team with a leader
    @PostMapping("/create")
    public ResponseEntity<String> createTeam(@RequestBody Team team) {
        return teamService.createTeam(team);
    }

    // Add a member to a team
    @PostMapping("/{teamId}/add-member")
    public ResponseEntity<String> addMember(@PathVariable Long teamId, @RequestBody User member) {
        return teamService.addMemberToTeam(teamId, member);
    }

    // Remove a member from a team
    @DeleteMapping("/{teamId}/remove-member/{memberId}")
    public ResponseEntity<String> removeMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        return teamService.removeMemberFromTeam(teamId, memberId);
    }

    // Request to join a team
    @PostMapping("/{teamId}/request-join")
    public ResponseEntity<String> requestJoinTeam(@PathVariable Long teamId, @RequestBody User user) {
        return teamService.requestJoinTeam(teamId, user);
    }

    // Accept or reject a join request
    @PostMapping("/{teamId}/respond-request")
    public ResponseEntity<String> respondJoinRequest(@PathVariable Long teamId, @RequestParam Long userId, @RequestParam boolean accept) {
        return teamService.respondJoinRequest(teamId, userId, accept);
    }

    // Assign a user to a team
    @PostMapping("/{teamId}/assign-user")
    public ResponseEntity<String> assignUserToTeam(@PathVariable Long teamId, @RequestBody User user) {
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team != null) {
//            user.setTeam(team);  // Assign team to the user
            userRepository.save(user);  // Save the user with the team association
            return ResponseEntity.ok("User assigned to team.");
        }
        return ResponseEntity.status(404).body("Team not found.");
    }
}
