package com.OwaspRegistrationPortal.Owasp.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String leaderName;
    private String leaderRollNo;
    private String leaderPhoneNo;
    // One team can have many users (members)
//    @OneToMany(mappedBy = "team")
//    private List<User> users; // List of users that belong to this team
}
