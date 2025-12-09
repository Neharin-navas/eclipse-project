package com.example.minijobportal.config;
 

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; // 2. Must import @Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // 3. Must import BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder; // 4. Must import PasswordEncoder

@Configuration // Tells Spring this class defines configuration/beans
public class SecurityConfig {

    @Bean // Tells Spring to instantiate and manage this object
    public PasswordEncoder passwordEncoder() {
        // Returns the concrete implementation you want to use for hashing
        return new BCryptPasswordEncoder();
    }
}