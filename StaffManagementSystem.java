import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StaffManagementSystem extends JFrame {
    private String currentUser;
    private JTable dutyTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> dutyTypeCombo;
    private JTextArea activityArea;
    private JSpinner hoursSpinner;
    private JLabel totalHoursLabel, tasksCompletedLabel;
    
    // Database connection
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Aswanth@121";
    
    // Staff data (no personal details)
    private Map<String, String> staffRoles = new HashMap<>();
    private Map<String, Double> staffHours = new HashMap<>();
    private Map<String, Integer> staffTasks = new HashMap<>();
    
    public StaffManagementSystem(String username) {
        this.currentUser = username;
        initializeStaffData();
        initializeUI();
        setupDatabase();
        loadStaffActivities();
    }
    
    private void initializeStaffData() {
        // Initialize with staff codes and roles only (no personal details)
        staffRoles.put("STF001", "Housekeeping");
        staffRoles.put("STF002", "Reception");
        staffRoles.put("STF003", "Maintenance");
        staffRoles.put("STF004", "Kitchen");
        staffRoles.put("STF005", "Security");
        staffRoles.put("STF006", "Management");
        staffRoles.put("STF007", "Housekeeping");
        staffRoles.put("STF008", "Reception");
        
        // Initialize hours and tasks counters
        for (String staffCode : staffRoles.keySet()) {
            staffHours.put(staffCode, 0.0);
            staffTasks.put(staffCode, 0);
        }
    }
    
    private void initializeUI() {
        setTitle("Hotel Staff Management System - Duty & Work Analysis");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        createHeader();
        createMainPanel();
        createActivityPanel();
        
        loadStaffActivities();
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JLabel header = new JLabel("Staff Duty Management & Work Analysis", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(Color.WHITE);
        
        JLabel userLabel = new JLabel("User: " + currentUser, SwingConstants.RIGHT);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(new Color(220, 220, 220));
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(70, 130, 180));
        topPanel.add(header, BorderLayout.CENTER);
        topPanel.add(userLabel, BorderLayout.EAST);
        
        headerPanel.add(topPanel, BorderLayout.NORTH);
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        
        // Left panel - Duty input form
        mainPanel.add(createInputPanel(), BorderLayout.WEST);
        
        // Center panel - Duty table
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setPreferredSize(new Dimension(350, 600));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Log Duty Activity"));
        formPanel.setBackground(new Color(245, 245, 245));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Staff Code (auto-assigned based on role)
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Staff Code:"), gbc);
        gbc.gridx = 1;
        JLabel staffCodeLabel = new JLabel("STF00" + (new Random().nextInt(8) + 1));
        staffCodeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        staffCodeLabel.setForeground(new Color(70, 130, 180));
        formPanel.add(staffCodeLabel, gbc);
        
        // Duty Type
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Duty Type:*"), gbc);
        gbc.gridx = 1;
        String[] dutyTypes = {"Cleaning", "Check-in/Check-out", "Maintenance", "Cooking", "Security", "Meeting", "Training", "Inventory", "Guest Service"};
        dutyTypeCombo = new JComboBox<>(dutyTypes);
        dutyTypeCombo.setPreferredSize(new Dimension(200, 35));
        dutyTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(dutyTypeCombo, gbc);
        
        // Hours Worked
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Hours Worked:*"), gbc);
        gbc.gridx = 1;
        hoursSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.5, 12.0, 0.5));
        hoursSpinner.setPreferredSize(new Dimension(80, 35));
        formPanel.add(hoursSpinner, gbc);
        
        // Activity Description
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Activity Details:*"), gbc);
        gbc.gridx = 1;
        activityArea = new JTextArea(4, 20);
        activityArea.setLineWrap(true);
        activityArea.setWrapStyleWord(true);
        activityArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(activityArea);
        scrollPane.setPreferredSize(new Dimension(200, 100));
        formPanel.add(scrollPane, gbc);
        
        // Submit Button
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitBtn = new JButton("Submit Duty Activity");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitBtn.setBackground(new Color(70, 130, 180));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setPreferredSize(new Dimension(200, 40));
        submitBtn.addActionListener(e -> submitDutyActivity(staffCodeLabel.getText()));
        formPanel.add(submitBtn, gbc);
        
        inputPanel.add(formPanel, BorderLayout.NORTH);
        return inputPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Today's Duty Activities"));
        
        String[] columns = {"Time", "Staff Code", "Duty Type", "Hours", "Activity", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        dutyTable = new JTable(tableModel);
        styleTable(dutyTable);
        
        JScrollPane scrollPane = new JScrollPane(dutyTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private void createActivityPanel() {
        JPanel activityPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        activityPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        activityPanel.setBackground(Color.WHITE);
        
        // Total Hours Panel
        JPanel hoursPanel = new JPanel(new BorderLayout());
        hoursPanel.setBorder(BorderFactory.createTitledBorder("Work Summary"));
        hoursPanel.setBackground(new Color(240, 248, 255));
        
        totalHoursLabel = new JLabel("Total Hours Today: 0.0", SwingConstants.CENTER);
        totalHoursLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalHoursLabel.setForeground(new Color(70, 130, 180));
        
        tasksCompletedLabel = new JLabel("Tasks Completed: 0", SwingConstants.CENTER);
        tasksCompletedLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tasksCompletedLabel.setForeground(new Color(70, 130, 180));
        
        JPanel statsPanel = new JPanel(new GridLayout(2, 1));
        statsPanel.setBackground(new Color(240, 248, 255));
        statsPanel.add(totalHoursLabel);
        statsPanel.add(tasksCompletedLabel);
        
        hoursPanel.add(statsPanel, BorderLayout.CENTER);
        
        // Quick Actions Panel
        JPanel actionsPanel = new JPanel(new BorderLayout());
        actionsPanel.setBorder(BorderFactory.createTitledBorder("Quick Actions"));
        actionsPanel.setBackground(new Color(240, 248, 255));
        
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonsPanel.setBackground(new Color(240, 248, 255));
        
        JButton viewScheduleBtn = createActionButton("View My Schedule", new Color(144, 238, 144));
        JButton requestLeaveBtn = createActionButton("Request Leave", new Color(255, 218, 185));
        JButton reportIssueBtn = createActionButton("Report Issue", new Color(255, 182, 193));
        
        buttonsPanel.add(viewScheduleBtn);
        buttonsPanel.add(requestLeaveBtn);
        buttonsPanel.add(reportIssueBtn);
        
        actionsPanel.add(buttonsPanel, BorderLayout.CENTER);
        
        activityPanel.add(hoursPanel);
        activityPanel.add(actionsPanel);
        
        add(activityPanel, BorderLayout.SOUTH);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return label;
    }
    
    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(150, 35));
        return btn;
    }
    
    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(176, 224, 230));
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
    }
    
    private void submitDutyActivity(String staffCode) {
        String dutyType = (String) dutyTypeCombo.getSelectedItem();
        double hours = (Double) hoursSpinner.getValue();
        String activity = activityArea.getText().trim();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        
        if (activity.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter activity details!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Add to table
        tableModel.addRow(new Object[]{
            timestamp, staffCode, dutyType, hours, activity, "Completed"
        });
        
        // Update statistics
        updateStatistics(staffCode, hours);
        
        // Clear form
        activityArea.setText("");
        hoursSpinner.setValue(1.0);
        
        JOptionPane.showMessageDialog(this, "Duty activity logged successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateStatistics(String staffCode, double hours) {
        // Update hours
        double currentHours = staffHours.getOrDefault(staffCode, 0.0);
        staffHours.put(staffCode, currentHours + hours);
        
        // Update tasks
        int currentTasks = staffTasks.getOrDefault(staffCode, 0);
        staffTasks.put(staffCode, currentTasks + 1);
        
        // Update UI
        updateSummaryDisplay();
    }
    
    private void updateSummaryDisplay() {
        double totalHours = staffHours.values().stream().mapToDouble(Double::doubleValue).sum();
        int totalTasks = staffTasks.values().stream().mapToInt(Integer::intValue).sum();
        
        totalHoursLabel.setText(String.format("Total Hours Today: %.1f", totalHours));
        tasksCompletedLabel.setText(String.format("Tasks Completed: %d", totalTasks));
    }
    
    private void loadStaffActivities() {
        // Load sample activities for demonstration
        String[][] sampleActivities = {
            {"08:00:00", "STF001", "Room Cleaning", "2.5", "Cleaned rooms 101-110", "Completed"},
            {"09:30:00", "STF002", "Guest Check-in", "1.0", "Processed 5 check-ins", "Completed"},
            {"10:15:00", "STF003", "Maintenance", "3.0", "Fixed plumbing in room 205", "Completed"},
            {"11:00:00", "STF004", "Cooking", "2.0", "Prepared breakfast buffet", "Completed"}
        };
        
        for (String[] activity : sampleActivities) {
            tableModel.addRow(activity);
            
            // Update statistics
            String staffCode = activity[1];
            double hours = Double.parseDouble(activity[3]);
            updateStatistics(staffCode, hours);
        }
    }
    
    private void setupDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, 
                "MySQL JDBC Driver not found!\nUsing demo mode.", 
                "Database Warning", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
}