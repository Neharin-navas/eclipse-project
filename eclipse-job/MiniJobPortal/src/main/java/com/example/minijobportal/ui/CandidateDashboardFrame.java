package com.example.minijobportal.ui;

import com.example.minijobportal.entity.JobPosting;
import com.example.minijobportal.service.JobService;
import com.example.minijobportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

@Component
public class CandidateDashboardFrame extends JFrame {

    // --- ELEGANT COLOR CONSTANTS ---
    private static final Color PRIMARY_BUTTON_COLOR = new Color(0, 123, 255); 
    private static final Color SECONDARY_BUTTON_COLOR = new Color(220, 53, 69); 
    private static final Color TEXT_COLOR = new Color(50, 60, 80); 
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    @Autowired private JobService jobService;
    @Autowired private UserService userService;
    @Lazy @Autowired private LoginFrame loginFrame;

    private JTable jobTable;
    private DefaultTableModel tableModel;
    private JButton applyButton = new JButton("Apply for Selected Job");
    private JButton refreshButton = new JButton("Refresh Jobs");
    private JButton logoutButton = new JButton("Logout");
    
    // Header Label to be updated dynamically
    private JLabel headerLabel = new JLabel("", SwingConstants.CENTER); 

    public CandidateDashboardFrame() {
        super("Candidate Dashboard");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());

        // --- 1. Gradient Background Panel ---
        JPanel background = createGradientBackgroundPanel();
        add(background, BorderLayout.CENTER);

        // --- 2. Center Card Panel ---
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setOpaque(false);

        JPanel card = createStyledCardPanel();
        
        // --- Card Layout Setup (BorderLayout for Title/Table/Buttons) ---
        card.setLayout(new BorderLayout(15, 15));
        
        // Header Setup (Will be populated in initAndShow)
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        card.add(headerLabel, BorderLayout.NORTH);

        // --- Table Setup ---
        tableModel = new DefaultTableModel(new Object[]{"ID", "Title", "Description", "Posted By"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        jobTable = new JTable(tableModel);
        jobTable.setRowHeight(25);
        jobTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        jobTable.getTableHeader().setFont(BUTTON_FONT);
        
        JScrollPane tableScrollPane = new JScrollPane(jobTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));
        card.add(tableScrollPane, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        
        stylePrimaryButton(applyButton, 250, 42); 
        styleSecondaryButton(refreshButton, 150, 42); 
        styleDangerButton(logoutButton, 120, 42); 

        buttonPanel.add(applyButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);
        
        card.add(buttonPanel, BorderLayout.SOUTH);

        // Add card to background
        cardWrapper.add(card);
        background.add(cardWrapper);

        // Action Listeners
        applyButton.addActionListener(e -> applyForJob());
        refreshButton.addActionListener(e -> loadJobs());
        logoutButton.addActionListener(e -> logout());
        
        setIconImage(createDummyIcon());
    }
    
    // --- Dashboard Specific Methods ---

    public void initAndShow() {
        if (userService.getLoggedInUser() != null) {
            String username = userService.getLoggedInUser().getUsername();
            
            // Set the personalized header text
            headerLabel.setText("Welcome, " + username + "!"); 
            
            setTitle("Candidate Dashboard - " + username);
            loadJobs();
            setVisible(true);
        } else {
            loginFrame.setVisible(true); // Fallback
        }
    }
    
    private void loadJobs() {
        tableModel.setRowCount(0); 
        List<JobPosting> currentJobList = jobService.findAllJobs();

        for (JobPosting job : currentJobList) {
            tableModel.addRow(new Object[]{
                job.getId(), 
                job.getTitle(), 
                job.getDescription(), 
                job.getPostedByHrUsername()
            });
        }
        if (jobTable.getColumnModel().getColumnCount() > 0) {
            jobTable.getColumnModel().getColumn(0).setPreferredWidth(50);
            jobTable.getColumnModel().getColumn(0).setMaxWidth(50);
        }
    }

    private void applyForJob() {
        int selectedRow = jobTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a job to apply.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Long jobId = (Long) tableModel.getValueAt(selectedRow, 0);

        if (jobService.applyForJob(jobId)) {
            JOptionPane.showMessageDialog(this, "Successfully applied for the job!");
        } else {
            JOptionPane.showMessageDialog(this, "Application failed. You may have already applied.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void logout() {
        userService.logout();
        this.setVisible(false);
        loginFrame.setVisible(true);
    }
    
    // --- UI Helper Methods (Unchanged) ---
    
    private JPanel createGradientBackgroundPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Color c1 = new Color(250, 250, 253); 
                Color c2 = new Color(230, 240, 255); 
                int w = getWidth(), h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);
                g2.dispose();
            }
        };
    }

    private JPanel createStyledCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(30, 30, 30, 30));
        return card;
    }

    private void stylePrimaryButton(JButton btn, int width, int height) {
        btn.setPreferredSize(new Dimension(width, height));
        btn.setFocusPainted(false);
        btn.setBackground(PRIMARY_BUTTON_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(BUTTON_FONT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(PRIMARY_BUTTON_COLOR.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(PRIMARY_BUTTON_COLOR); }
        });
    }

    private void styleSecondaryButton(JButton btn, int width, int height) {
        btn.setPreferredSize(new Dimension(width, height));
        btn.setFocusPainted(false);
        btn.setBackground(Color.decode("#CCCCCC")); 
        btn.setForeground(TEXT_COLOR);
        btn.setFont(BUTTON_FONT);
        btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(230, 230, 230)); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(Color.decode("#CCCCCC")); }
        });
    }
    
    private void styleDangerButton(JButton btn, int width, int height) {
        btn.setPreferredSize(new Dimension(width, height));
        btn.setFocusPainted(false);
        btn.setBackground(SECONDARY_BUTTON_COLOR); 
        btn.setForeground(Color.WHITE);
        btn.setFont(BUTTON_FONT);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(SECONDARY_BUTTON_COLOR.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(SECONDARY_BUTTON_COLOR); }
        });
    }
    
    private Image createDummyIcon() {
        int size = 16;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0, 120, 215));
        g.fillRoundRect(0, 0, size, size, 4, 4);
        g.setColor(Color.WHITE);
        g.fillOval(3, 3, 10, 10);
        g.dispose();
        return img;
    }
}
