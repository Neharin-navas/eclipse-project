package com.example.minijobportal.ui;

import com.example.minijobportal.entity.User;
import com.example.minijobportal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Stylish LoginFrame: Centered, Simplified Form.
 */
@Component
public class LoginFrame extends JFrame {

    // --- COLOR CONSTANTS ---
    private static final Color PRIMARY_BUTTON_COLOR = new Color(0, 123, 255); 
    private static final Color SECONDARY_BUTTON_COLOR = new Color(245, 246, 250); 
    private static final Color TEXT_COLOR = new Color(50, 60, 80); 

    @Autowired private UserService userService;
    @Lazy @Autowired private HRDashboardFrame hrDashboard;
    @Lazy @Autowired private CandidateDashboardFrame candidateDashboard;

    // UI components
    private JTextField usernameField = createRoundedField();
    private JPasswordField passwordField = createRoundedPasswordField();
    private JComboBox<String> roleCombo = new JComboBox<>(new String[]{"CANDIDATE", "HR"});
    private JButton loginButton = new JButton("Sign in");
    private JButton registerButton = new JButton("Create account");

    public LoginFrame() {
        super("Mini Job Portal Login"); // Updated title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(null); // center
        setLayout(new BorderLayout());

        // Gradient background panel
        JPanel background = new JPanel() {
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
        background.setLayout(new GridBagLayout());
        add(background, BorderLayout.CENTER);

        // Center card panel wrapper
        JPanel cardWrapper = new JPanel(new GridBagLayout());
        cardWrapper.setOpaque(false);

        // Card Panel - Now solely responsible for the centered form
        JPanel card = new JPanel(new GridBagLayout()) {
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
        card.setBorder(new EmptyBorder(40, 40, 40, 40)); 
        
        GridBagConstraints f = new GridBagConstraints();
        f.insets = new Insets(10, 10, 10, 10);
        f.gridx = 0; f.gridy = 0; f.gridwidth = 2; f.anchor = GridBagConstraints.WEST;

        // --- 1. Form Title (Simple Header) ---
        JLabel header = new JLabel("Mini Job Portal Login");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(TEXT_COLOR);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        f.gridy++; f.anchor = GridBagConstraints.CENTER;
        card.add(header, f);
        f.anchor = GridBagConstraints.WEST; // Reset anchor for labels

        // --- 2. Username ---
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(TEXT_COLOR);
        f.gridy++;
        card.add(userLabel, f);
        f.gridy++; f.weightx = 1.0;
        card.add(usernameField, f);

        // --- 3. Password ---
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passLabel.setForeground(TEXT_COLOR);
        f.gridy++;
        card.add(passLabel, f);
        f.gridy++;
        card.add(passwordField, f);

        // --- 4. Role ---
        JLabel roleLabel = new JLabel("Role");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(TEXT_COLOR);
        f.gridy++;
        card.add(roleLabel, f);
        f.gridy++;
        roleCombo.setPreferredSize(new Dimension(360, 38));
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleCombo.setBackground(Color.WHITE);
        card.add(roleCombo, f);

        // --- 5. Buttons (CENTERED) ---
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0)); // FlowLayout.CENTER centers buttons
        buttons.setOpaque(false);
        stylePrimaryButton(loginButton);
        styleSecondaryButton(registerButton);
        buttons.add(loginButton);
        buttons.add(registerButton);

        f.gridy++; f.anchor = GridBagConstraints.CENTER; // Center the button panel
        card.add(buttons, f);

        // Add the centered card to the background panel
        cardWrapper.add(card);
        background.add(cardWrapper);

        // Attach actions
        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> attemptRegister());

        setIconImage(createDummyIcon());
    }

    // ---------- UI helpers (No changes needed here) ----------

    private static JTextField createRoundedField() {
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(360, 38));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        tf.setOpaque(true);
        tf.setBackground(Color.WHITE);
        return tf;
    }

    private static JPasswordField createRoundedPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setPreferredSize(new Dimension(360, 38));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                new EmptyBorder(8, 12, 8, 12)
        ));
        pf.setOpaque(true);
        pf.setBackground(Color.WHITE);
        return pf;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setPreferredSize(new Dimension(170, 42));
        btn.setFocusPainted(false);
        btn.setBackground(PRIMARY_BUTTON_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(PRIMARY_BUTTON_COLOR.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(PRIMARY_BUTTON_COLOR); }
        });
    }

    private void styleSecondaryButton(JButton btn) {
        btn.setPreferredSize(new Dimension(150, 42));
        btn.setFocusPainted(false);
        btn.setBackground(SECONDARY_BUTTON_COLOR);
        btn.setForeground(TEXT_COLOR);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(235, 236, 240)); }
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

    // ---------- Actions (No changes needed here) ----------

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Missing data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = userService.login(username, password);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Invalid credentials.", "Login failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userService.getLoggedInUser();
        SwingUtilities.invokeLater(() -> {
            this.setVisible(false);
            if ("HR".equalsIgnoreCase(user.getRole())) {
                hrDashboard.initAndShow();
            } else {
                candidateDashboard.initAndShow();
            }
        });
    }

    private void attemptRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleCombo.getSelectedItem();

        if (username.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean created = userService.registerUser(username, password, role);
        if (created) {
            JOptionPane.showMessageDialog(this, "Account created for " + role + ". You can now sign in.");
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists. Try another.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void initAndShow() {
        setVisible(true);
    }
}