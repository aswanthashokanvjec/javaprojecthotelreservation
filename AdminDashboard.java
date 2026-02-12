import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminDashboard extends JFrame {
    private String currentUser;
    private JLabel welcomeLabel;
    private JTabbedPane tabbedPane;
    
    // Database connection
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Aswanth@121";
    
    public AdminDashboard(String username) {
        this.currentUser = username;
        initializeUI();
        setupDatabase();
    }
    
    private void initializeUI() {
        setTitle("Hotel Management System - Admin Dashboard");
        setSize(1400, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        createHeader();
        createMainPanel();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel header = new JLabel("Hotel Management System - Admin Dashboard", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(Color.WHITE);
        
        welcomeLabel = new JLabel("Welcome, " + currentUser, SwingConstants.RIGHT);
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeLabel.setForeground(new Color(220, 220, 220));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(70, 130, 180));
        topPanel.add(header, BorderLayout.CENTER);
        topPanel.add(welcomeLabel, BorderLayout.EAST);
        
        headerPanel.add(topPanel, BorderLayout.NORTH);
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createMainPanel() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Create different admin tabs
        tabbedPane.addTab("📊 Dashboard", createDashboardPanel());
        tabbedPane.addTab("👥 Staff Management", createStaffManagementPanel());
        tabbedPane.addTab("📈 Performance Analytics", createAnalyticsPanel());
        tabbedPane.addTab("🏨 Hotel Operations", createOperationsPanel());
        tabbedPane.addTab("💰 Revenue Management", createRevenuePanel());
        tabbedPane.addTab("⚙️ System Settings", createSettingsPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(Color.WHITE);
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Quick stats panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        statsPanel.setBackground(Color.WHITE);
        
        // Create stat cards
        statsPanel.add(createStatCard("Total Staff", "24", Color.BLUE, "👥"));
        statsPanel.add(createStatCard("Active Today", "18", Color.GREEN, "✅"));
        statsPanel.add(createStatCard("On Leave", "3", Color.ORANGE, "🏖️"));
        statsPanel.add(createStatCard("Tasks Completed", "156", Color.CYAN, "✓"));
        statsPanel.add(createStatCard("Pending Tasks", "12", Color.YELLOW, "⏳"));
        statsPanel.add(createStatCard("Today's Revenue", "$2,845", Color.MAGENTA, "💰"));
        statsPanel.add(createStatCard("Occupancy Rate", "78%", Color.PINK, "🏨"));
        statsPanel.add(createStatCard("Guest Satisfaction", "92%", Color.GREEN, "⭐"));
        
        // Recent activities
        JPanel activitiesPanel = new JPanel(new BorderLayout());
        activitiesPanel.setBorder(BorderFactory.createTitledBorder("Recent Staff Activities"));
        activitiesPanel.setBackground(Color.WHITE);
        
        String[] columns = {"Time", "Staff", "Activity", "Department", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable activitiesTable = new JTable(model);
        styleTable(activitiesTable);
        
        // Sample activities
        String[][] sampleActivities = {
            {"09:00 AM", "STF001", "Room Cleaning", "Housekeeping", "Completed"},
            {"09:15 AM", "STF002", "Guest Check-in", "Reception", "Completed"},
            {"10:30 AM", "STF003", "AC Repair", "Maintenance", "In Progress"},
            {"11:00 AM", "STF004", "Breakfast Service", "Kitchen", "Completed"},
            {"11:45 AM", "STF005", "Security Round", "Security", "Completed"}
        };
        
        for (String[] activity : sampleActivities) {
            model.addRow(activity);
        }
        
        activitiesPanel.add(new JScrollPane(activitiesTable), BorderLayout.CENTER);
        
        dashboardPanel.add(statsPanel, BorderLayout.NORTH);
        dashboardPanel.add(activitiesPanel, BorderLayout.CENTER);
        
        return dashboardPanel;
    }
    
    private JPanel createStaffManagementPanel() {
        JPanel staffPanel = new JPanel(new BorderLayout());
        staffPanel.setBackground(Color.WHITE);
        staffPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Control panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(Color.WHITE);
        
        JButton addStaffBtn = createButton("Add Staff", new Color(144, 238, 144));
        JButton assignDutyBtn = createButton("Assign Duty", new Color(135, 206, 250));
        JButton viewScheduleBtn = createButton("View Schedule", new Color(255, 218, 185));
        JButton generateReportBtn = createButton("Generate Report", new Color(221, 160, 221));
        
        controlPanel.add(addStaffBtn);
        controlPanel.add(assignDutyBtn);
        controlPanel.add(viewScheduleBtn);
        controlPanel.add(generateReportBtn);
        
        // Staff table
        String[] columns = {"Staff ID", "Role", "Department", "Current Duty", "Hours Worked", "Tasks Completed", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable staffTable = new JTable(model);
        styleTable(staffTable);
        
        // Sample staff data
        String[][] staffData = {
            {"STF001", "Supervisor", "Housekeeping", "Floor Inspection", "38", "45", "Active"},
            {"STF002", "Receptionist", "Front Desk", "Guest Check-in", "40", "62", "Active"},
            {"STF003", "Technician", "Maintenance", "AC Repair", "35", "28", "Active"},
            {"STF004", "Chef", "Kitchen", "Breakfast Service", "42", "89", "Active"},
            {"STF005", "Guard", "Security", "Patrol Duty", "40", "56", "Active"},
            {"STF006", "Cleaner", "Housekeeping", "Room Cleaning", "36", "78", "On Leave"},
            {"STF007", "Manager", "Administration", "Staff Meeting", "45", "23", "Active"}
        };
        
        for (String[] staff : staffData) {
            model.addRow(staff);
        }
        
        staffPanel.add(controlPanel, BorderLayout.NORTH);
        staffPanel.add(new JScrollPane(staffTable), BorderLayout.CENTER);
        
        return staffPanel;
    }
    
    private JPanel createAnalyticsPanel() {
        JPanel analyticsPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        analyticsPanel.setBackground(Color.WHITE);
        analyticsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Department performance
        analyticsPanel.add(createChartPanel("Department Performance", 
            new String[]{"Housekeeping", "Reception", "Maintenance", "Kitchen", "Security"},
            new int[]{95, 88, 92, 96, 90}));
        
        // Task completion rate
        analyticsPanel.add(createChartPanel("Task Completion Rate", 
            new String[]{"Completed", "In Progress", "Pending", "Delayed"},
            new int[]{75, 15, 8, 2}));
        
        // Workload distribution
        analyticsPanel.add(createChartPanel("Workload Distribution", 
            new String[]{"Housekeeping", "Reception", "Maintenance", "Kitchen"},
            new int[]{35, 25, 20, 20}));
        
        // Efficiency metrics
        analyticsPanel.add(createChartPanel("Efficiency Metrics", 
            new String[]{"Time Mgmt", "Quality", "Productivity", "Teamwork"},
            new int[]{88, 92, 85, 90}));
        
        return analyticsPanel;
    }
    
    private JPanel createOperationsPanel() {
        JPanel operationsPanel = new JPanel(new BorderLayout());
        operationsPanel.setBackground(Color.WHITE);
        operationsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        gridPanel.setBackground(Color.WHITE);
        
        // Operation cards
        gridPanel.add(createOperationCard("Room Status", "🛏️", "24 Occupied / 6 Available", Color.CYAN));
        gridPanel.add(createOperationCard("Maintenance", "🔧", "3 Pending Requests", Color.ORANGE));
        gridPanel.add(createOperationCard("Housekeeping", "🧹", "All Rooms Cleaned", Color.GREEN));
        gridPanel.add(createOperationCard("Food & Beverage", "🍽️", "Breakfast: 85 Served", Color.PINK));
        gridPanel.add(createOperationCard("Security", "👮", "All Zones Secure", Color.BLUE));
        gridPanel.add(createOperationCard("Guest Services", "🎯", "12 Active Requests", Color.MAGENTA));
        
        operationsPanel.add(gridPanel, BorderLayout.CENTER);
        
        return operationsPanel;
    }
    
    private JPanel createRevenuePanel() {
        JPanel revenuePanel = new JPanel(new BorderLayout());
        revenuePanel.setBackground(Color.WHITE);
        revenuePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel metricsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        metricsPanel.setBackground(Color.WHITE);
        
        metricsPanel.add(createMetricCard("Today's Revenue", "$2,845", "+12%", Color.GREEN));
        metricsPanel.add(createMetricCard("Weekly Revenue", "$18,756", "+8%", Color.BLUE));
        metricsPanel.add(createMetricCard("Monthly Revenue", "$72,489", "+15%", Color.MAGENTA));
        metricsPanel.add(createMetricCard("Occupancy Rate", "78%", "+5%", Color.CYAN));
        metricsPanel.add(createMetricCard("Avg. Room Rate", "$156", "+3%", Color.ORANGE));
        metricsPanel.add(createMetricCard("RevPAR", "$122", "+10%", Color.PINK));
        
        revenuePanel.add(metricsPanel, BorderLayout.CENTER);
        
        return revenuePanel;
    }
    
    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel(new GridLayout(4, 2, 20, 20));
        settingsPanel.setBackground(Color.WHITE);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        settingsPanel.add(createSettingCard("Staff Roles", "Manage staff roles and permissions", "👥"));
        settingsPanel.add(createSettingCard("Shift Management", "Configure work shifts", "⏰"));
        settingsPanel.add(createSettingCard("Department Setup", "Manage departments", "🏢"));
        settingsPanel.add(createSettingCard("Task Templates", "Create duty templates", "📋"));
        settingsPanel.add(createSettingCard("Reporting", "Configure reports", "📊"));
        settingsPanel.add(createSettingCard("Notifications", "Manage alerts", "🔔"));
        settingsPanel.add(createSettingCard("Backup", "System backup", "💾"));
        settingsPanel.add(createSettingCard("System Logs", "View activity logs", "📝"));
        
        return settingsPanel;
    }
    
    // Helper methods for creating UI components
    private JPanel createStatCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color.brighter());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(color.brighter());
        contentPanel.add(iconLabel, BorderLayout.NORTH);
        contentPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(valueLabel, BorderLayout.SOUTH);
        
        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createChartPanel(String title, String[] labels, int[] values) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setBackground(Color.WHITE);
        
        DefaultTableModel model = new DefaultTableModel(new String[]{"Category", "Value"}, 0);
        JTable table = new JTable(model);
        
        for (int i = 0; i < labels.length; i++) {
            model.addRow(new Object[]{labels[i], values[i] + "%"});
        }
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createOperationCard(String title, String icon, String status, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color.brighter());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(color.brighter());
        contentPanel.add(iconLabel, BorderLayout.NORTH);
        contentPanel.add(titleLabel, BorderLayout.CENTER);
        contentPanel.add(statusLabel, BorderLayout.SOUTH);
        
        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createMetricCard(String title, String value, String change, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color.brighter());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        JLabel changeLabel = new JLabel(change);
        changeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        changeLabel.setForeground(Color.GREEN);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(changeLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createSettingCard(String title, String description, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(240, 240, 240));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        
        card.add(iconLabel, BorderLayout.NORTH);
        card.add(titleLabel, BorderLayout.CENTER);
        card.add(descLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(120, 35));
        return btn;
    }
    
    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
    
    private void setupDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
                "MySQL JDBC Driver not found!\nUsing demo data.", 
                "Database Warning", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
}
