package com.OwaspRegistrationPortal.Owasp.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "users") // Specify the table name explicitly
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Ensure ID is auto-generated
    private Long id;

    @Column(name = "username") // Map fields to the exact column names in the database
    private String username;

    @Column(name = "collage_name")
    private String collageName;

    @Column(name = "thapar_email", unique = true)
    private String thaparEmail;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @Column(name = "roll_no")
    private String rollNo;

    @Column(name = "role")
    private String role;

    @Column(name = "year")
    private String year;

    @Column(name = "position")
    private String position;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "verification_token", unique = true)
    private String verificationToken;

    @Column(name = "verified")
    private boolean verified;

    @ManyToOne
    @JoinColumn(name = "team_id", referencedColumnName = "id")
    @JsonIgnore
    private Team team;
}
