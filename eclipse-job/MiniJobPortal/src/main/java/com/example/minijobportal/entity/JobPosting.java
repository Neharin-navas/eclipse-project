package com.example.minijobportal.entity;



import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor // Lombok annotation for no-argument constructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    
    private String postedByHrUsername;
    
    // Stores candidate usernames as a comma-separated list (e.g., "user1,user2,")
    private String applicants;

    @CreationTimestamp // Automatically sets the current date/time when the entity is first persisted
    private LocalDateTime postedDate; 

    /**
     * Constructor used by the JobService when creating a new job posting.
     */
    public JobPosting(String title, String description, String postedByHrUsername) {
        this.title = title;
        this.description = description;
        this.postedByHrUsername = postedByHrUsername;
        // applicants and postedDate will be handled by their initial values/annotations
        // postedDate is automatically managed by @CreationTimestamp
    }
}