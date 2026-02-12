import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LoginSystem extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, createAccountButton;
    private JLabel messageLabel;
    private JCheckBox showPasswordCheckbox;
    
    // Database connection
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Aswanth@121";
    
    // Validation patterns
    private final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private final Pattern PASSWORD_PATTERN = Pattern.compile("^.{4,}$");
    private final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    // Store user type for role-based access
    private String currentUserType;
    
    public LoginSystem() {
        initializeUI();
        setupDatabase();
    }
    
    private void initializeUI() {
        setTitle("Hotel Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Main panel with background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 245, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Login form panel
        JPanel loginPanel = createLoginPanel();
        mainPanel.add(loginPanel, BorderLayout.CENTER);
        
        // Footer panel
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 245, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Icon and title
        JLabel iconLabel = new JLabel("🏨");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel titleLabel = new JLabel("Grand Horizon Hotels - Reservation System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel subtitleLabel = new JLabel("Role-Based Access - Admin, Staff & Guest Portals");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        headerPanel.add(iconLabel, BorderLayout.NORTH);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        return headerPanel;
    }
    
    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username field
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginPanel.add(userLabel, gbc);
        
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(250, 40));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        loginPanel.add(usernameField, gbc);
        
        // Password field
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginPanel.add(passLabel, gbc);
        
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(250, 40));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        loginPanel.add(passwordField, gbc);
        
        // Show password checkbox
        gbc.gridx = 1; gbc.gridy = 2;
        showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswordCheckbox.setBackground(Color.WHITE);
        showPasswordCheckbox.addActionListener(e -> {
            if (showPasswordCheckbox.isSelected()) {
                passwordField.setEchoChar((char)0);
            } else {
                passwordField.setEchoChar('•');
            }
        });
        loginPanel.add(showPasswordCheckbox, gbc);
        
        // Demo credentials info
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JLabel demoLabel = new JLabel("<html><div style='text-align: center; color: #666; font-size: 11px;'>"
            + "<b>Demo Credentials:</b><br>"
            + "Admin: admin/admin123 | Staff: staff/staff123<br>"
            + "Guest: user/user123 | Create account for new users"
            + "</div></html>", SwingConstants.CENTER);
        loginPanel.add(demoLabel, gbc);
        
        // Message label
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        messageLabel = new JLabel(" ");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        messageLabel.setForeground(Color.RED);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loginPanel.add(messageLabel, gbc);
        
        // Login button
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        loginButton = new JButton("Login to System");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(70, 130, 180));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(300, 45));
        loginButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        loginButton.addActionListener(e -> performLogin());
        loginPanel.add(loginButton, gbc);
        
        // Enter key listener for login
        passwordField.addActionListener(e -> performLogin());
        
        return loginPanel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(240, 245, 250));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JLabel newUserLabel = new JLabel("New to our hotel system?");
        newUserLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        newUserLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        createAccountButton = new JButton("Create Guest Account");
        createAccountButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        createAccountButton.setBackground(new Color(144, 238, 144));
        createAccountButton.setForeground(Color.BLACK);
        createAccountButton.setFocusPainted(false);
        createAccountButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        createAccountButton.addActionListener(e -> showCreateAccountDialog());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 245, 250));
        buttonPanel.add(createAccountButton);
        
        footerPanel.add(newUserLabel, BorderLayout.NORTH);
        footerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return footerPanel;
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both username and password!", true);
            return;
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            showMessage("Invalid username format!", true);
            return;
        }
        
        // Authenticate user with role detection
        if (authenticateUser(username, password)) {
            showMessage("Login successful! Redirecting to " + 
                       getSystemName(currentUserType) + "...", false);
            
            // Close login window after short delay
            Timer timer = new Timer(1500, e -> {
                dispose(); // Close login window
                openRoleBasedSystem(username, currentUserType);
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            showMessage("Invalid username or password!", true);
        }
    }
    
    private String getSystemName(String userType) {
        switch (userType) {
            case "admin": return "Admin Dashboard";
            case "staff": return "Staff Portal";
            case "guest": return "Hotel Reservation System";
            default: return "System";
        }
    }
    
    private boolean authenticateUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Try to authenticate using database
            String sql = "SELECT user_type FROM users WHERE username = ? AND password_hash = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, "hashed_" + password); // Simple hashing for demo
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentUserType = rs.getString("user_type");
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            // If database fails, use demo credentials
            System.out.println("Database connection failed, using demo credentials: " + e.getMessage());
        }
        
        // Use demo credentials with role-based access
        return checkDefaultCredentialsWithRoles(username, password);
    }
    
    private boolean checkDefaultCredentialsWithRoles(String username, String password) {
        // Default credentials with roles for demo
        Map<String, String[]> credentials = new HashMap<>();
        credentials.put("admin", new String[]{"admin123", "admin"});
        credentials.put("manager", new String[]{"manager123", "admin"});
        credentials.put("staff", new String[]{"staff123", "staff"});
        credentials.put("housekeeping", new String[]{"house123", "staff"});
        credentials.put("reception", new String[]{"reception123", "staff"});
        credentials.put("user", new String[]{"user123", "guest"});
        credentials.put("guest", new String[]{"guest123", "guest"});
        credentials.put("john", new String[]{"john123", "guest"});
        credentials.put("mary", new String[]{"mary123", "guest"});
        
        if (credentials.containsKey(username)) {
            String[] creds = credentials.get(username);
            if (creds[0].equals(password)) {
                currentUserType = creds[1];
                return true;
            }
        }
        return false;
    }
    
    private void showCreateAccountDialog() {
        JDialog createDialog = new JDialog(this, "Create Guest Account", true);
        createDialog.setSize(600, 700);
        createDialog.setLocationRelativeTo(this);
        createDialog.setLayout(new BorderLayout(10, 10));
        createDialog.getContentPane().setBackground(new Color(240, 245, 250));
        
        // Header
        JLabel headerLabel = new JLabel("Create Guest Account", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(new Color(70, 130, 180));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Form fields for guest account (simpler than staff account)
        String[] labels = {"Full Name:", "Username:", "Email:", "Password:", "Confirm Password:"};
        JComponent[] fields = {
            new JTextField(100), 
            new JTextField(100), 
            new JTextField(100),
            new JPasswordField(100), 
            new JPasswordField(100)
        };
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("Segoe UI", Font.BOLD, 13));
            formPanel.add(label, gbc);
            
            gbc.gridx = 1;
            fields[i].setFont(new Font("Segoe UI", Font.PLAIN, 14));
            fields[i].setPreferredSize(new Dimension(500, 350));
            
            if (fields[i] instanceof JTextField) {
                ((JTextField) fields[i]).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
            }
            formPanel.add(fields[i], gbc);
        }
        
        // Message label
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        JLabel createMessageLabel = new JLabel(" ");
        createMessageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        createMessageLabel.setForeground(Color.RED);
        createMessageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(createMessageLabel, gbc);
        
        // Create button
        gbc.gridx = 0; gbc.gridy = labels.length + 1; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        JButton createBtn = new JButton("Create Guest Account");
        createBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        createBtn.setBackground(new Color(70, 130, 180));
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        createBtn.setPreferredSize(new Dimension(250, 40));
        createBtn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        createBtn.addActionListener(e -> {
            String fullName = ((JTextField)fields[0]).getText().trim();
            String username = ((JTextField)fields[1]).getText().trim();
            String email = ((JTextField)fields[2]).getText().trim();
            String password = new String(((JPasswordField)fields[3]).getPassword());
            String confirmPassword = new String(((JPasswordField)fields[4]).getPassword());
            
            if (createGuestAccount(fullName, username, email, password, confirmPassword, createMessageLabel)) {
                createMessageLabel.setForeground(new Color(0, 128, 0));
                createMessageLabel.setText("Guest account created successfully!");
                
                // Close dialog after delay and auto-login
                Timer timer = new Timer(2000, ev -> {
                    createDialog.dispose();
                    // Auto-login with new account
                    usernameField.setText(username);
                    passwordField.setText(password);
                    performLogin();
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
        formPanel.add(createBtn, gbc);
        
        createDialog.add(headerLabel, BorderLayout.NORTH);
        createDialog.add(formPanel, BorderLayout.CENTER);
        createDialog.setVisible(true);
    }
    
    private boolean createGuestAccount(String fullName, String username, String email, 
                                    String password, String confirmPassword,
                                    JLabel messageLabel) {
        // Validation
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("All fields are required!");
            return false;
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            messageLabel.setText("Username must be 3-20 characters (letters, numbers, underscore only)");
            return false;
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            messageLabel.setText("Please enter a valid email address");
            return false;
        }
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            messageLabel.setText("Password must be at least 4 characters long");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match!");
            return false;
        }
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Check if user already exists
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                checkStmt.setString(2, email);
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        messageLabel.setText("Username or email already exists!");
                        return false;
                    }
                }
            }
            
            // Insert new guest user
            String insertSql = "INSERT INTO users (full_name, username, email, password_hash, user_type) VALUES (?, ?, ?, ?, 'guest')";
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                // In real application, use proper password hashing like BCrypt
                String passwordHash = "hashed_" + password; // Placeholder for hashing
                
                stmt.setString(1, fullName);
                stmt.setString(2, username);
                stmt.setString(3, email);
                stmt.setString(4, passwordHash);
                
                int result = stmt.executeUpdate();
                return result > 0;
            }
            
        } catch (SQLException e) {
            messageLabel.setText("Error creating account: " + e.getMessage());
            return false;
        }
    }
    
    private void openRoleBasedSystem(String username, String userType) {
        SwingUtilities.invokeLater(() -> {
            switch (userType) {
                case "admin":
                    new AdminDashboard(username).setVisible(true);
                    break;
                case "staff":
                    new StaffManagementSystem(username).setVisible(true);
                    break;
                case "guest":
                default:
                    // For guest users, open the main HotelApp
                    new HotelApp(username).setVisible(true);
                    break;
            }
        });
    }
    
    private void showMessage(String message, boolean isError) {
        messageLabel.setForeground(isError ? Color.RED : new Color(0, 128, 0));
        messageLabel.setText(message);
        
        // Clear message after 3 seconds
        Timer timer = new Timer(3000, e -> messageLabel.setText(" "));
        timer.setRepeats(false);
        timer.start();
    }
    
    private void setupDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Create necessary tables if they don't exist
            createTablesIfNotExist();
            
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
                "MySQL JDBC Driver not found! Using demo mode with default credentials.", 
                "Database Warning", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void createTablesIfNotExist() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Create users table with guest role
            String usersTableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "full_name VARCHAR(100) NOT NULL, " +
                "username VARCHAR(50) UNIQUE NOT NULL, " +
                "email VARCHAR(100) UNIQUE NOT NULL, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "user_type ENUM('admin', 'staff', 'guest') DEFAULT 'guest', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "status ENUM('active', 'inactive') DEFAULT 'active'" +
                ")";
            stmt.execute(usersTableSQL);
            
            // Insert default guest accounts if table is empty
            String checkSql = "SELECT COUNT(*) FROM users";
            ResultSet rs = stmt.executeQuery(checkSql);
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert default accounts
                String[] defaultUsers = {
                    "('Admin User', 'admin', 'admin@hotel.com', 'hashed_admin123', 'admin', 'active')",
                    "('Staff Member', 'staff', 'staff@hotel.com', 'hashed_staff123', 'staff', 'active')",
                    "('John Doe', 'user', 'john@email.com', 'hashed_user123', 'guest', 'active')",
                    "('Mary Smith', 'mary', 'mary@email.com', 'hashed_mary123', 'guest', 'active')"
                };
                
                for (String user : defaultUsers) {
                    stmt.execute("INSERT INTO users (full_name, username, email, password_hash, user_type, status) VALUES " + user);
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Note: Using demo mode - Database tables could not be created: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Diagnostic: print where LoginSystem class was loaded from and classloader
        try {
            System.out.println("LoginSystem class code source: " +
                (LoginSystem.class.getProtectionDomain() != null ?
                 LoginSystem.class.getProtectionDomain().getCodeSource() : null));
            System.out.println("LoginSystem classloader: " + LoginSystem.class.getClassLoader());
        } catch (Throwable t) {
            // ignore diagnostic failures
            t.printStackTrace();
        }

        // Create and show login window
        SwingUtilities.invokeLater(() -> {
            LoginSystem loginSystem = new LoginSystem();
            loginSystem.setVisible(true);
        });
    }
}