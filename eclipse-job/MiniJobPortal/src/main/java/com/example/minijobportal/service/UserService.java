package com.example.minijobportal.service;



import com.example.minijobportal.entity.User;
import com.example.minijobportal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Injects the BCrypt PasswordEncoder bean from SecurityConfig
    @Autowired
    private PasswordEncoder passwordEncoder; 

    // Simple session management
    private User loggedInUser; 

    public User getLoggedInUser() { 
        return loggedInUser; 
    }
    
    public void logout() { 
        this.loggedInUser = null; 
    }

    /**
     * Registers a new user with a securely hashed password.
     */
    public boolean registerUser(String username, String password, String role) {
        if (userRepository.findByUsername(username) != null) {
            return false; // User already exists
        }
        
        // SECURITY: Use BCrypt to hash the password before saving
        String hashedPassword = passwordEncoder.encode(password);
        
        User newUser = new User(username, hashedPassword, role.toUpperCase());
        userRepository.save(newUser);
        return true;
    }

    /**
     * Authenticates a user by comparing the entered password against the stored hash.
     */
    public boolean login(String username, String password) {
        User user = userRepository.findByUsername(username);
        
        if (user != null) {
            // SECURITY: Use passwordEncoder.matches() to safely compare the raw password 
            // with the stored BCrypt hash.
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                this.loggedInUser = user; // Successful login, set session
                return true;
            }
        }
        return false; // Login failed
    }
}