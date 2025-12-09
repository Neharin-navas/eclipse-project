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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * HR Dashboard (updated)
 * - split is a class field
 * - description area slightly smaller (balanced)
 * - bottom jobs panel has a minimum size to avoid collapse
 * - divider location is set after the frame becomes visible
 * - ADDED: Delete Job button functionality
 */
@Component
public class HRDashboardFrame extends JFrame {

    private static final Color PRIMARY_BUTTON_COLOR = new Color(0, 123, 255);
    private static final Color DANGER_BUTTON_COLOR = new Color(220, 53, 69);
    private static final Color TEXT_COLOR = new Color(50, 60, 80);

    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 14);

    private static final Dimension BUTTON_SIZE = new Dimension(160, 40);

    @Autowired private JobService jobService;
    @Autowired private UserService userService;
    @Lazy @Autowired private LoginFrame loginFrame;

    // make split a field so we can adjust divider after showing
    private JSplitPane split;

    // form components
    private final JTextField titleField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(8, 60); 
    private final JButton postJobButton = new JButton("Post Job");

    // jobs table and controls
    private DefaultTableModel tableModel;
    private JTable postedJobsTable;
    private final JButton viewApplicantsButton = new JButton("View Applicants");
    private final JButton refreshJobsButton = new JButton("Refresh Jobs");
    private final JButton deleteJobButton = new JButton("Delete Job"); // <-- ADDED BUTTON FIELD
    private final JButton logoutButton = new JButton("Logout");

    private final JLabel headerLabel = new JLabel("", SwingConstants.CENTER);

    public HRDashboardFrame() {
        super("HR Dashboard");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(1000, 720);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null); // center
        setLayout(new BorderLayout());

        // gradient background
        JPanel background = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                Color c1 = new Color(250,250,253);
                Color c2 = new Color(235,245,255);
                g2.setPaint(new GradientPaint(0,0,c1,0,getHeight(),c2));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        background.setLayout(new GridBagLayout());
        add(background, BorderLayout.CENTER);

        // card wrapper to center content
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setOpaque(false);
        cardWrapper.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel card = new JPanel(new BorderLayout(16, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,245));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),18,18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setPreferredSize(new Dimension(940, 680));

        // header
        headerLabel.setFont(HEADER_FONT);
        headerLabel.setForeground(TEXT_COLOR);
        card.add(headerLabel, BorderLayout.NORTH);

        // create split as a field
        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.38);           
        split.setDividerSize(8);
        split.setOneTouchExpandable(false);
        split.setContinuousLayout(true);

        // Posting form (top)
        JPanel postingPanel = buildPostingPanel();
        // Jobs area (bottom)
        JPanel jobsPanel = buildJobsListPanel();

        // ensure jobs panel has a reasonable minimum so it doesn't collapse
        jobsPanel.setMinimumSize(new Dimension(600, 200));

        split.setTopComponent(postingPanel);
        split.setBottomComponent(jobsPanel);

        // initial divider location (will be corrected after visible)
        split.setDividerLocation(320);

        card.add(split, BorderLayout.CENTER);

        // Bottom logout row (outside split so it remains visible)
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomRow.setOpaque(false);
        styleDangerButton(logoutButton);
        bottomRow.add(logoutButton);
        card.add(bottomRow, BorderLayout.SOUTH);

        cardWrapper.add(card);
        background.add(cardWrapper);

        // Actions
        postJobButton.addActionListener(e -> postJob());
        refreshJobsButton.addActionListener(e -> loadPostedJobs());
        viewApplicantsButton.addActionListener(e -> viewApplicants());
        deleteJobButton.addActionListener(e -> deleteJob()); // <-- ADDED DELETE LISTENER
        logoutButton.addActionListener(e -> logout());

        setIconImage(createDummyIcon());
    }

    private JPanel buildPostingPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title row
        JLabel titleLbl = new JLabel("Job Title:");
        titleLbl.setFont(LABEL_FONT);
        titleLbl.setForeground(TEXT_COLOR);
        titleField.setPreferredSize(new Dimension(720, 36));
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        wrapper.add(titleLbl, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        wrapper.add(titleField, gbc);

        // Description row (uses scroll pane)
        JLabel descLbl = new JLabel("Description:");
        descLbl.setFont(LABEL_FONT);
        descLbl.setForeground(TEXT_COLOR);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setRows(8); 

        // Put text area inside a JScrollPane and set preferred & minimum sizes
        JScrollPane descScroll = new JScrollPane(descriptionArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descScroll.setPreferredSize(new Dimension(720, 180)); 
        descScroll.setMinimumSize(new Dimension(600, 140));   

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.NORTHWEST; gbc.weightx = 0; gbc.weighty = 0;
        wrapper.add(descLbl, gbc);

        // Allow the description scroll pane to grow vertically but stay balanced
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH; 
        wrapper.add(descScroll, gbc);

        // Post button aligned right
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        stylePrimaryButton(postJobButton);
        btnRow.add(postJobButton);

        gbc.gridx = 1; gbc.gridy = 2; gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST;
        wrapper.add(btnRow, gbc);

        return wrapper;
    }

    private JPanel buildJobsListPanel() {
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setOpaque(false);

        // Buttons panel at top of jobs list so they are always visible
        JPanel topButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        topButtons.setOpaque(false);
        
        stylePrimaryButton(viewApplicantsButton);
        styleSecondaryButton(refreshJobsButton);
        styleDangerButton(deleteJobButton); // <-- STYLING DELETE BUTTON
        
        topButtons.add(viewApplicantsButton);
        topButtons.add(refreshJobsButton);
        topButtons.add(deleteJobButton); // <-- ADDING DELETE BUTTON

        // Table
        tableModel = new DefaultTableModel(new Object[]{"ID","Title","ApplicantsCount","DatePosted"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        postedJobsTable = new JTable(tableModel);
        postedJobsTable.setRowHeight(36);
        postedJobsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        postedJobsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        // columns widths to ensure the important columns visible
        postedJobsTable.getColumnModel().getColumn(0).setPreferredWidth(50); 
        postedJobsTable.getColumnModel().getColumn(1).setPreferredWidth(420); 
        postedJobsTable.getColumnModel().getColumn(2).setPreferredWidth(140); 
        postedJobsTable.getColumnModel().getColumn(3).setPreferredWidth(120); 

        postedJobsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // add double-click behavior to table
        postedJobsTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewApplicants();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(postedJobsTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220,220,225)));

        wrapper.add(topButtons, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    // ---------- Actions ----------

    public void initAndShow() {
        if (userService.getLoggedInUser() != null) {
            headerLabel.setText("Welcome back, HR " + userService.getLoggedInUser().getUsername() + "!");
            loadPostedJobs();
            setVisible(true);

            // FIX: Ensure divider is placed after the frame is visible and measured
            SwingUtilities.invokeLater(() -> {
                int divider = (int) (this.getHeight() * 0.38);
                divider = Math.max(divider, 220);
                divider = Math.min(divider, this.getHeight() - 250);
                split.setDividerLocation(divider);
            });
        } else {
            loginFrame.setVisible(true);
        }
    }

    private void postJob() {
        String title = titleField.getText().trim();
        String desc = descriptionArea.getText().trim();
        boolean ok = jobService.postJob(title, desc);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Job posted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            titleField.setText("");
            descriptionArea.setText("");
            loadPostedJobs();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to post. Title and description are required.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPostedJobs() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            List<JobPosting> list = jobService.findJobsByLoggedInHR();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (list != null) {
                for (JobPosting j : list) {
                    int count = 0;
                    try {
                        String applicants = j.getApplicants(); 
                        if (applicants != null && !applicants.isBlank()) {
                            count = (int) Arrays.stream(applicants.split(",")).filter(s -> !s.trim().isEmpty()).count();
                        }
                    } catch (Exception ignored) {}

                    String date = "N/A";
                    try {
                        LocalDateTime dt = j.getPostedDate();
                        if (dt != null) date = dt.format(fmt);
                    } catch (Exception ignored) {}

                    tableModel.addRow(new Object[]{ j.getId(), j.getTitle(), count + " candidate(s)", date });
                }
            }

            // hide ID column visually but keep data accessible in model
            if (postedJobsTable.getColumnModel().getColumnCount() > 0) {
                postedJobsTable.getColumnModel().getColumn(0).setMinWidth(0);
                postedJobsTable.getColumnModel().getColumn(0).setMaxWidth(0);
                postedJobsTable.getColumnModel().getColumn(0).setPreferredWidth(0);
            }
        });
    }

    private void viewApplicants() {
        int sel = postedJobsTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select a job first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Object idObj = tableModel.getValueAt(sel, 0);
        Long jobId = null;
        if (idObj instanceof Number) jobId = ((Number) idObj).longValue();
        else {
            try { jobId = Long.parseLong(idObj.toString()); } catch (Exception ex) { jobId = null; }
        }
        if (jobId == null) {
            JOptionPane.showMessageDialog(this, "Cannot determine job id.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Optional<JobPosting> opt = jobService.findJobById(jobId);
        if (opt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Job not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JobPosting job = opt.get();
        String raw = job.getApplicants();
        if (raw == null || raw.isBlank()) raw = "No applications yet.";
        else {
            String[] arr = raw.split(",");
            StringBuilder sb = new StringBuilder();
            for (String s : arr) if (!s.trim().isEmpty()) sb.append("- ").append(s.trim()).append("\n");
            raw = sb.length() == 0 ? "No applications yet." : sb.toString();
        }
        JTextArea area = new JTextArea(raw);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(520, 300));
        JOptionPane.showMessageDialog(this, sp, "Applicants for: " + job.getTitle(), JOptionPane.INFORMATION_MESSAGE);
    }
    
    // --- NEW METHOD: Deletes the selected job ---
    private void deleteJob() {
        int selectedRow = postedJobsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a job to delete.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                                    "Are you sure you want to delete this job posting?", 
                                    "Confirm Deletion", 
                                    JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Object idObj = tableModel.getValueAt(selectedRow, 0);
            Long jobId = null;
            if (idObj instanceof Number) jobId = ((Number) idObj).longValue();
            
            if (jobId != null && jobService.deleteJob(jobId)) {
                JOptionPane.showMessageDialog(this, "Job deleted successfully.");
                loadPostedJobs(); // Refresh the table after deletion
            } else {
                JOptionPane.showMessageDialog(this, "Deletion failed. You may not have permission or the job doesn't exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void logout() {
        userService.logout();
        this.setVisible(false);
        loginFrame.setVisible(true);
    }

    // ---------- Styling helpers (unchanged) ----------

    private void stylePrimaryButton(JButton btn) {
        btn.setPreferredSize(BUTTON_SIZE);
        btn.setBackground(PRIMARY_BUTTON_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(PRIMARY_BUTTON_COLOR.darker()); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(PRIMARY_BUTTON_COLOR); }
        });
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setPreferredSize(new Dimension(140, 36));
        btn.setBackground(new Color(245,245,247));
        btn.setForeground(TEXT_COLOR);
        btn.setFont(BUTTON_FONT);
        btn.setBorder(BorderFactory.createLineBorder(new Color(220,220,225)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(235,235,240)); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(new Color(245,245,247)); }
        });
    }

    private void styleDangerButton(JButton btn) {
        btn.setPreferredSize(new Dimension(120, 38));
        btn.setBackground(DANGER_BUTTON_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(BUTTON_FONT);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(DANGER_BUTTON_COLOR.darker()); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(DANGER_BUTTON_COLOR); }
        });
    }

    private Image createDummyIcon() {
        int size = 16;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0,120,215));
        g.fillRoundRect(0,0,size,size,4,4);
        g.setColor(Color.WHITE);
        g.fillOval(3,3,10,10);
        g.dispose();
        return img;
    }
}