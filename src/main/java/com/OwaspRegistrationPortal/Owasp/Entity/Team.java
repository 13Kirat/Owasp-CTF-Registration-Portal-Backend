package com.OwaspRegistrationPortal.Owasp.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "team_token", unique = true, nullable = false)
    private String teamToken;

    @Column(name = "team_name", unique = true, nullable = false)
    private String teamName;

    @Column(name = "leader_name", nullable = false)
    private String leaderName;

    @Column(name = "leader_roll_no", nullable = false)
    private String leaderRollNo;

    @Column(name = "leader_phone_no", nullable = false)
    private String leaderPhoneNo;

    // One team can have up to 5 users (members)
    @OneToMany(mappedBy = "team")
    private List<User> members = new ArrayList<>();
}
