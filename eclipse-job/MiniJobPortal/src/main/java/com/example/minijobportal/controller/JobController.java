package com.example.minijobportal.controller;



import com.example.minijobportal.entity.JobPosting;
import com.example.minijobportal.service.JobService;
import com.example.minijobportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired 
    private JobService jobService;
    
    @Autowired 
    private UserService userService;
    
    // --- Helper for API Testing ---
    private static final String HR_USERNAME = "api_hr";
    private static final String HR_PASS = "apipass";
    
    /**
     * Ensures a placeholder HR user exists and is logged in to allow the API
     * to bypass the role check in JobService for testing via Swagger.
     */
    private void ensureHrContext() {
        // Register the user (UserService handles hashing and checks if already exists)
        userService.registerUser(HR_USERNAME, HR_PASS, "HR"); 
        // Log the user in to set the thread-local session needed for JobService.getLoggedInUser()
        userService.login(HR_USERNAME, HR_PASS);
    }
    // ---------------------------------------------

    /**
     * Endpoint 1: View all jobs (Candidate/Public view).
     * Accessible via GET http://localhost:8080/api/jobs
     */
    @GetMapping
    public List<JobPosting> getAllJobs() {
        return jobService.findAllJobs();
    }

    /**
     * Endpoint 2: HR posts a job.
     * Accessible via POST http://localhost:8080/api/jobs/post
     * Requires the HR role.
     */
    @PostMapping("/post")
    public ResponseEntity<String> postJob(@RequestParam String title, @RequestParam String description) {
        
        // Temporarily log in the placeholder HR user for this API call to pass the service layer's role check
        ensureHrContext();
        
        if (jobService.postJob(title, description)) {
            return ResponseEntity.ok("Job posted successfully via API by " + HR_USERNAME);
        }
        // If posting fails (e.g., empty title/description), return a bad request status
        return ResponseEntity.badRequest().body("Failed to post job. Title and description cannot be empty.");
    }
}