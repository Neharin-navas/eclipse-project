package com.example.minijobportal.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table; 
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
// FIX: Renames the table from the reserved SQL keyword 'USER' to 'app_user'.
@Table(name = "app_user") 
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String passwordHash; // Stores the secure BCrypt hash
    private String role; // Stores "HR" or "CANDIDATE"

    /**
     * Constructor used for creating a new User object.
     * Note: passwordHash should be the already-hashed string from UserService.
     */
    public User(String username, String passwordHash, String role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }
}