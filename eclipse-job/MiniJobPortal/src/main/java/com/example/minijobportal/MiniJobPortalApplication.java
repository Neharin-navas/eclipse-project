package com.example.minijobportal;

import com.example.minijobportal.ui.LoginFrame;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class MiniJobPortalApplication {

    public static void main(String[] args) {
        
        // 1. Initialize Spring Context
        // SpringApplicationBuilder is used to configure a non-web application (like Swing) 
        // while still starting the Tomcat server for the Swagger API.
        ConfigurableApplicationContext context = new SpringApplicationBuilder(MiniJobPortalApplication.class)
            .headless(false) // CRUCIAL: Allows the Swing UI (AWT) to run
            .run(args);

        // 2. Launch the Swing UI on the Event Dispatch Thread (EDT)
        // Swing operations must be executed on the EDT to avoid thread conflicts.
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = context.getBean(LoginFrame.class);
            loginFrame.setVisible(true);
        });
    }

}
