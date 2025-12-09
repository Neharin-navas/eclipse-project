package com.example.minijobportal.service;


import com.example.minijobportal.entity.JobPosting;
import com.example.minijobportal.entity.User;
import com.example.minijobportal.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    @Autowired 
    private JobPostingRepository jobRepository;
    
    @Autowired 
    private UserService userService; 

    /**
     * Posts a new job if the logged-in user is an HR and the inputs are valid.
     */
    public boolean postJob(String title, String description) {
        User user = userService.getLoggedInUser();
        
        if (user == null || !user.getRole().equals("HR")) {
            return false; 
        }
        
        if (title == null || title.isBlank() || description == null || description.isBlank()) {
            return false;
        }
        
        JobPosting job = new JobPosting(title, description, user.getUsername());
        jobRepository.save(job);
        return true;
    }
    
    /**
     * Retrieves all available job postings.
     */
    public List<JobPosting> findAllJobs() { 
        return jobRepository.findAll(); 
    }
    
    /**
     * Retrieves a single job posting by ID.
     */
    public Optional<JobPosting> findJobById(Long jobId) {
        return jobRepository.findById(jobId);
    }
    
    /**
     * Retrieves all job postings created by the currently logged-in HR user.
     */
    public List<JobPosting> findJobsByLoggedInHR() {
        User user = userService.getLoggedInUser();
        
        // Security check: Only allow HR users to fetch their own job data
        if (user == null || !user.getRole().equals("HR")) {
            return List.of(); 
        }
        
        // Uses the custom repository method we defined earlier
        return jobRepository.findByPostedByHrUsername(user.getUsername());
    }


    /**
     * Allows a CANDIDATE to apply for a job, preventing duplicate applications.
     */
    public boolean applyForJob(Long jobId) {
        User user = userService.getLoggedInUser();
        
        if (user == null || !user.getRole().equals("CANDIDATE")) {
            return false; 
        }
        
        Optional<JobPosting> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) return false;
        
        JobPosting job = jobOpt.get();
        String candidateUsername = user.getUsername();

        String currentApplicants = job.getApplicants();
        
        if (currentApplicants != null && currentApplicants.contains(candidateUsername + ",")) {
            return false; 
        }

        job.setApplicants((currentApplicants == null ? "" : currentApplicants) + candidateUsername + ",");
        jobRepository.save(job);
        return true;
    }
     
    public boolean deleteJob(Long jobId) {
        User user = userService.getLoggedInUser();
        
        if (user == null || !user.getRole().equals("HR")) {
            return false; // Not authorized
        }
        
        Optional<JobPosting> jobOpt = jobRepository.findById(jobId);
        
        if (jobOpt.isPresent()) {
            JobPosting job = jobOpt.get();
            // Check if the currently logged-in HR is the one who posted the job
            if (job.getPostedByHrUsername().equals(user.getUsername())) {
                jobRepository.delete(job);
                return true;
            }
        }
        return false;
    }
}
