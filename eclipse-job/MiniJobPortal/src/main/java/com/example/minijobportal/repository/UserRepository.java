package com.example.minijobportal.repository;



import com.example.minijobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// The first parameter is the Entity class, the second is the type of the Primary Key (Long)
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Custom query method defined by Spring Data JPA method naming convention.
     * Used in UserService for checking existing users during registration and for login.
     */
    User findByUsername(String username);
}