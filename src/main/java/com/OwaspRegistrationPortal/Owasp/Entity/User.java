package com.OwaspRegistrationPortal.Owasp.Entity;

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

    @Column(name = "thapar_email")
    private String thaparEmail;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "roll_no")
    private String rollNo;

    @Column(name = "role")
    private String role;

    @Column(name = "year")
    private String year;

    @Column(name = "position")
    private String position;

//    @ManyToOne
//    @JoinColumn(name = "team_id", referencedColumnName = "id")
    @Column(name = "team")
    private String team;
}