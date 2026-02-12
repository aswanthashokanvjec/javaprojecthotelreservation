import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class HotelApp extends JFrame {
    // Constants
    private static final String APP_TITLE = "Grand Horizon Hotels - Reservation System";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Aswanth@121";

    // Room rates per night
    private static final double STANDARD_RATE = 1000.0;
    private static final double DELUXE_RATE = 1500.0;
    private static final double SUITE_RATE = 2500.0;

    // Hotel names
    private static final String[] HOTEL_NAMES = {
        "Grand Horizon Downtown", 
        "Skyline Luxury Suites", 
        "Oceanview Resort & Spa",
        "Mountain Peak Inn", 
        "City Center Plaza", 
        "Royal Garden Hotel"
    };

    // UI Components
    private DefaultTableModel tableModel;
    private JTable reservationsTable;
    private JTextField guestField, roomField, contactField, emailField;
    private JComboBox<String> roomTypeCombo, paymentMethodCombo, hotelCombo;
    private JSpinner nightsSpinner, adultsSpinner, childrenSpinner;
    private JCheckBox breakfastCheckbox, parkingCheckbox;
    private JTextArea billArea;
    private JLabel totalAmountLabel;
    private double currentTotal = 0.0;
    private JPanel mainPanel;
    private JButton viewReservationsBtn;
    
    // User session
    private String currentUser;

    // Validation patterns
    private final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]{2,50}$");
    private final Pattern CONTACT_PATTERN = Pattern.compile("^[\\d\\s\\-+()]{10,20}$");
    private final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Modified constructor with username
    public HotelApp(String username) {
        this.currentUser = username;
        initializeUI();
        setupDatabaseConnection();
        initializeAvailableRooms(); // Initialize available rooms data
    }

    // No-arg constructor for compatibility
    public HotelApp() {
        this("Guest");
    }

    private void initializeUI() {
        setTitle(APP_TITLE);
        setSize(1400, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        createHeader();
        createMainPanel();
        createFormPanel();
        createBillPanel();
        createButtonPanel();

        loadReservations();
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(70, 130, 180));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel header = new JLabel(APP_TITLE, SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setForeground(Color.WHITE);

        // Show current user in header
        JLabel userLabel = new JLabel("Welcome, " + currentUser, SwingConstants.RIGHT);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(new Color(220, 220, 220));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(70, 130, 180));
        topPanel.add(header, BorderLayout.CENTER);
        topPanel.add(userLabel, BorderLayout.EAST);

        headerPanel.add(topPanel, BorderLayout.NORTH);
        
        JLabel subHeader = new JLabel("Luxury Stays & Memorable Experiences", SwingConstants.CENTER);
        subHeader.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subHeader.setForeground(new Color(220, 220, 220));
        headerPanel.add(subHeader, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel, BorderLayout.CENTER);

        // Create a welcome panel
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel welcomeLabel = new JLabel("<html><div style='text-align: center;'>"
                + "<h1 style='color: #4682B4;'>Welcome to Hotel Reservation System</h1>"
                + "<p style='font-size: 16px; margin-top: 20px; color: #666;'>"
                + "Manage your hotel reservations efficiently and effectively.<br>"
                + "Create new reservations, calculate bills, and view analytics."
                + "</p>"
                + "<p style='font-size: 14px; margin-top: 30px; color: #888;'>"
                + "Click the 'View Reservations' button to see all current reservations."
                + "</p></div></html>", SwingConstants.CENTER);

        viewReservationsBtn = new JButton("View Current Reservations");
        viewReservationsBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        viewReservationsBtn.setBackground(new Color(70, 130, 180));
        viewReservationsBtn.setForeground(Color.WHITE);
        viewReservationsBtn.setFocusPainted(false);
        viewReservationsBtn.setPreferredSize(new Dimension(350, 60));
        viewReservationsBtn.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(viewReservationsBtn);

        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);
        welcomePanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(welcomePanel, BorderLayout.CENTER);

        viewReservationsBtn.addActionListener(e -> showReservationsWindow());
    }

    private void showReservationsWindow() {
        JDialog reservationsDialog = new JDialog(this, "Current Reservations", false);
        reservationsDialog.setSize(1200, 700);
        reservationsDialog.setLocationRelativeTo(this);
        reservationsDialog.setLayout(new BorderLayout(10, 10));

        // Create table for reservations
        String[] columns = {"ID", "Hotel", "Guest Name", "Room No", "Room Type", "Contact", "Check-In", "Check-Out", "Total Amount", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0, 3 -> Integer.class;
                    case 8 -> Double.class;
                    default -> String.class;
                };
            }
        };

        reservationsTable = new JTable(tableModel);
        styleTable(reservationsTable);

        JScrollPane scrollPane = new JScrollPane(reservationsTable);
        scrollPane.setPreferredSize(new Dimension(1000, 500));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Current Reservations"));

        // Button panel for the dialog
        JPanel dialogButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        dialogButtonPanel.setBackground(Color.WHITE);
        
        JButton refreshBtn = createDialogButton("Refresh", new Color(135, 206, 250));
        JButton selectBtn = createDialogButton("Select Reservation", new Color(144, 238, 144));
        JButton closeBtn = createDialogButton("Close", new Color(250, 128, 114));

        refreshBtn.addActionListener(e -> loadReservations());
        closeBtn.addActionListener(e -> reservationsDialog.dispose());
        selectBtn.addActionListener(e -> {
            if (reservationsTable.getSelectedRow() >= 0) {
                populateFormFromSelectedRow();
                reservationsDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(reservationsDialog, "Please select a reservation first.");
            }
        });

        dialogButtonPanel.add(refreshBtn);
        dialogButtonPanel.add(selectBtn);
        dialogButtonPanel.add(closeBtn);

        reservationsDialog.add(scrollPane, BorderLayout.CENTER);
        reservationsDialog.add(dialogButtonPanel, BorderLayout.SOUTH);

        // Load data and show dialog
        loadReservations();
        reservationsDialog.setVisible(true);
    }

    // === FIND AVAILABLE ROOMS FEATURE ===
    private void showAvailableRoomsDialog() {
        JDialog availableRoomsDialog = new JDialog(this, "Find Available Rooms", true);
        availableRoomsDialog.setSize(1000, 700);
        availableRoomsDialog.setLocationRelativeTo(this);
        availableRoomsDialog.setLayout(new BorderLayout(10, 10));

        // Search criteria panel
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Criteria"));
        searchPanel.setBackground(new Color(245, 245, 245));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Hotel selection
        JComboBox<String> searchHotelCombo = new JComboBox<>(HOTEL_NAMES);
        searchHotelCombo.insertItemAt("All Hotels", 0);
        searchHotelCombo.setSelectedIndex(0);
        searchHotelCombo.setPreferredSize(new Dimension(200, 35));

        // Room type selection
        String[] roomTypes = {"All Types", "Standard", "Deluxe", "Suite"};
        JComboBox<String> searchRoomTypeCombo = new JComboBox<>(roomTypes);
        searchRoomTypeCombo.setPreferredSize(new Dimension(150, 35));

        // Max price spinner
        JSpinner maxPriceSpinner = new JSpinner(new SpinnerNumberModel(5000, 500, 10000, 500));
        maxPriceSpinner.setPreferredSize(new Dimension(120, 35));

        // Capacity spinner
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));
        capacitySpinner.setPreferredSize(new Dimension(80, 35));

        int row = 0;
        
        gbc.gridx = 0; gbc.gridy = row;
        searchPanel.add(createLabel("Hotel:"), gbc);
        gbc.gridx = 1;
        searchPanel.add(searchHotelCombo, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        searchPanel.add(createLabel("Room Type:"), gbc);
        gbc.gridx = 1;
        searchPanel.add(searchRoomTypeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        searchPanel.add(createLabel("Max Price per Night:"), gbc);
        gbc.gridx = 1;
        searchPanel.add(maxPriceSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = ++row;
        searchPanel.add(createLabel("Minimum Capacity:"), gbc);
        gbc.gridx = 1;
        searchPanel.add(capacitySpinner, gbc);

        // Search button
        JButton searchBtn = new JButton("Search Available Rooms");
        searchBtn.setBackground(new Color(70, 130, 180));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchBtn.setPreferredSize(new Dimension(200, 40));

        gbc.gridx = 0; gbc.gridy = ++row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        searchPanel.add(searchBtn, gbc);

        // Results table
        String[] columns = {"Hotel", "Room Number", "Room Type", "Capacity", "Price/Night", "Amenities", "Status"};
        DefaultTableModel availableRoomsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable availableRoomsTable = new JTable(availableRoomsModel);
        styleTable(availableRoomsTable);
        availableRoomsTable.setRowHeight(30);

        JScrollPane tableScrollPane = new JScrollPane(availableRoomsTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Available Rooms"));

        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout());
        JButton selectRoomBtn = createDialogButton("Select Room", new Color(144, 238, 144));
        JButton closeDialogBtn = createDialogButton("Close", new Color(250, 128, 114));

        selectRoomBtn.addActionListener(e -> {
            int selectedRow = availableRoomsTable.getSelectedRow();
            if (selectedRow >= 0) {
                selectAvailableRoom(availableRoomsModel, selectedRow);
                availableRoomsDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(availableRoomsDialog, "Please select a room first.");
            }
        });

        closeDialogBtn.addActionListener(e -> availableRoomsDialog.dispose());

        actionPanel.add(selectRoomBtn);
        actionPanel.add(closeDialogBtn);

        // Search button action
        searchBtn.addActionListener(e -> {
            String selectedHotel = searchHotelCombo.getSelectedIndex() == 0 ? null : (String) searchHotelCombo.getSelectedItem();
            String selectedRoomType = searchRoomTypeCombo.getSelectedIndex() == 0 ? null : (String) searchRoomTypeCombo.getSelectedItem();
            double maxPrice = (Double) maxPriceSpinner.getValue();
            int capacity = (Integer) capacitySpinner.getValue();
            
            searchAvailableRooms(availableRoomsModel, selectedHotel, selectedRoomType, maxPrice, capacity);
        });

        // Layout the dialog
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);

        availableRoomsDialog.add(topPanel, BorderLayout.NORTH);
        availableRoomsDialog.add(tableScrollPane, BorderLayout.CENTER);
        availableRoomsDialog.add(actionPanel, BorderLayout.SOUTH);

        // Perform initial search
        searchAvailableRooms(availableRoomsModel, null, null, 5000, 2);
        availableRoomsDialog.setVisible(true);
    }

    private void searchAvailableRooms(DefaultTableModel model, String hotel, String roomType, double maxPrice, int capacity) {
        model.setRowCount(0);
        
        try (Connection conn = getConnection()) {
            // Get all rooms that match criteria and are not currently booked
            String sql = """
                SELECT r.hotel, r.room_number, r.room_type, r.capacity, r.price_per_night, r.amenities,
                       CASE WHEN res.room_number IS NULL THEN 'Available' ELSE 'Booked' END as status
                FROM available_rooms r
                LEFT JOIN reservations res ON r.hotel = res.hotel AND r.room_number = res.room_number 
                    AND res.status NOT IN ('Cancelled', 'Checked Out')
                WHERE r.price_per_night <= ? AND r.capacity >= ?
                    AND (? IS NULL OR r.hotel = ?)
                    AND (? IS NULL OR r.room_type = ?)
                ORDER BY r.hotel, r.room_number
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, maxPrice);
                stmt.setInt(2, capacity);
                stmt.setString(3, hotel);
                stmt.setString(4, hotel);
                stmt.setString(5, roomType);
                stmt.setString(6, roomType);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        // Only show available rooms
                        if ("Available".equals(rs.getString("status"))) {
                            model.addRow(new Object[]{
                                rs.getString("hotel"),
                                rs.getInt("room_number"),
                                rs.getString("room_type"),
                                rs.getInt("capacity"),
                                String.format("$%.2f", rs.getDouble("price_per_night")),
                                rs.getString("amenities"),
                                rs.getString("status")
                            });
                        }
                    }
                }
            }
            
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No available rooms found matching your criteria.", "No Results", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            showError("Error searching available rooms: " + e.getMessage());
        }
    }

    private void selectAvailableRoom(DefaultTableModel model, int selectedRow) {
        String hotel = (String) model.getValueAt(selectedRow, 0);
        int roomNumber = (Integer) model.getValueAt(selectedRow, 1);
        String roomType = (String) model.getValueAt(selectedRow, 2);
        
        // Auto-fill the form with selected room details
        hotelCombo.setSelectedItem(hotel);
        roomField.setText(String.valueOf(roomNumber));
        roomTypeCombo.setSelectedItem(roomType);
        
        // Calculate bill with selected room type
        calculateBill();
        
        showSuccess("Room " + roomNumber + " at " + hotel + " has been selected. Please complete the reservation form.");
    }

    private void initializeAvailableRooms() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Create available_rooms table if it doesn't exist
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS available_rooms (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    hotel VARCHAR(100) NOT NULL,
                    room_number INT NOT NULL,
                    room_type VARCHAR(20) NOT NULL,
                    capacity INT NOT NULL,
                    price_per_night DECIMAL(10,2) NOT NULL,
                    amenities TEXT,
                    UNIQUE KEY unique_room (hotel, room_number)
                )
                """;
            stmt.execute(createTableSQL);
            
            // Insert sample room data if table is empty
            String checkSQL = "SELECT COUNT(*) FROM available_rooms";
            try (ResultSet rs = stmt.executeQuery(checkSQL)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    insertSampleRoomData(conn);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error initializing available rooms: " + e.getMessage());
        }
    }

    private void insertSampleRoomData(Connection conn) throws SQLException {
        String insertSQL = """
            INSERT INTO available_rooms (hotel, room_number, room_type, capacity, price_per_night, amenities) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            // Sample data for each hotel
            for (String hotel : HOTEL_NAMES) {
                // Standard rooms
                for (int i = 101; i <= 110; i++) {
                    stmt.setString(1, hotel);
                    stmt.setInt(2, i);
                    stmt.setString(3, "Standard");
                    stmt.setInt(4, 2);
                    stmt.setDouble(5, STANDARD_RATE);
                    stmt.setString(6, "WiFi, TV, AC, Mini Bar");
                    stmt.addBatch();
                }
                
                // Deluxe rooms
                for (int i = 201; i <= 205; i++) {
                    stmt.setString(1, hotel);
                    stmt.setInt(2, i);
                    stmt.setString(3, "Deluxe");
                    stmt.setInt(4, 3);
                    stmt.setDouble(5, DELUXE_RATE);
                    stmt.setString(6, "WiFi, TV, AC, Mini Bar, Balcony, Coffee Maker");
                    stmt.addBatch();
                }
                
                // Suite rooms
                for (int i = 301; i <= 303; i++) {
                    stmt.setString(1, hotel);
                    stmt.setInt(2, i);
                    stmt.setString(3, "Suite");
                    stmt.setInt(4, 4);
                    stmt.setDouble(5, SUITE_RATE);
                    stmt.setString(6, "WiFi, TV, AC, Mini Bar, Balcony, Jacuzzi, Living Room");
                    stmt.addBatch();
                }
            }
            stmt.executeBatch();
        }
    }

    // Rest of the existing methods remain the same...
    private JButton createDialogButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(180, 45));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(176, 224, 230));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(220, 220, 220));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(70, 130, 180));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0 || i == 3 || i == 8) {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    private void createFormPanel() {
        JPanel formContainer = new JPanel(new BorderLayout());
        formContainer.setBackground(Color.WHITE);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Reservation Details"));
        formPanel.setBackground(new Color(245, 245, 245));
        formPanel.setPreferredSize(new Dimension(500, 600));

        // Initialize fields with larger size
        guestField = createTextField(25);
        roomField = createTextField(25);
        contactField = createTextField(25);
        emailField = createTextField(25);

        hotelCombo = new JComboBox<>(HOTEL_NAMES);
        hotelCombo.setPreferredSize(new Dimension(250, 35));
        hotelCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String[] roomTypes = {"Standard", "Deluxe", "Suite"};
        roomTypeCombo = new JComboBox<>(roomTypes);
        roomTypeCombo.setPreferredSize(new Dimension(250, 35));
        roomTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String[] paymentMethods = {"Credit Card", "Debit Card", "Cash", "UPI", "PayPal"};
        paymentMethodCombo = new JComboBox<>(paymentMethods);
        paymentMethodCombo.setPreferredSize(new Dimension(250, 35));
        paymentMethodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        nightsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
        nightsSpinner.setPreferredSize(new Dimension(80, 35));
        
        adultsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
        adultsSpinner.setPreferredSize(new Dimension(80, 35));
        
        childrenSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 4, 1));
        childrenSpinner.setPreferredSize(new Dimension(80, 35));

        breakfastCheckbox = new JCheckBox("Breakfast Included ($500/day)");
        breakfastCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        parkingCheckbox = new JCheckBox("Parking ($50/day)");
        parkingCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Hotel Selection
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Hotel:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(hotelCombo, gbc);

        // Guest Name
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Guest Name:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(guestField, gbc);

        // Contact
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Contact:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(contactField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);

        // Room Number
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Room Number:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(roomField, gbc);

        // Room Type
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Room Type:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(roomTypeCombo, gbc);

        // Nights
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Nights:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(nightsSpinner, gbc);

        // Adults
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Adults:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(adultsSpinner, gbc);

        // Children
        gbc.gridx = 0; gbc.gridy = row++;
        formPanel.add(createLabel("Children:"), gbc);
        gbc.gridx = 1;
        formPanel.add(childrenSpinner, gbc);

        // Checkboxes
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(breakfastCheckbox, gbc);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(parkingCheckbox, gbc);

        // Payment Method
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 1;
        formPanel.add(createLabel("Payment Method:*"), gbc);
        gbc.gridx = 1;
        formPanel.add(paymentMethodCombo, gbc);

        formContainer.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(formContainer, BorderLayout.WEST);

        // Add listeners for real-time bill calculation
        roomTypeCombo.addActionListener(e -> calculateBill());
        nightsSpinner.addChangeListener(e -> calculateBill());
        breakfastCheckbox.addActionListener(e -> calculateBill());
        parkingCheckbox.addActionListener(e -> calculateBill());
        adultsSpinner.addChangeListener(e -> calculateBill());
        childrenSpinner.addChangeListener(e -> calculateBill());
    }

    private void createBillPanel() {
        JPanel billContainer = new JPanel(new BorderLayout());
        billContainer.setBackground(Color.WHITE);
        
        JPanel billPanel = new JPanel(new BorderLayout());
        billPanel.setBorder(BorderFactory.createTitledBorder("Bill Details"));
        billPanel.setPreferredSize(new Dimension(400, 400));
        billPanel.setBackground(new Color(245, 245, 245));

        billArea = new JTextArea(12, 35);
        billArea.setEditable(false);
        billArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        billArea.setBackground(new Color(240, 240, 240));
        billArea.setMargin(new Insets(15, 15, 15, 15));

        totalAmountLabel = new JLabel("Total: $0.00", SwingConstants.CENTER);
        totalAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalAmountLabel.setForeground(new Color(0, 100, 0));
        totalAmountLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        billPanel.add(new JScrollPane(billArea), BorderLayout.CENTER);
        billPanel.add(totalAmountLabel, BorderLayout.SOUTH);

        billContainer.add(billPanel, BorderLayout.CENTER);
        mainPanel.add(billContainer, BorderLayout.CENTER);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setPreferredSize(new Dimension(120, 25));
        return label;
    }

    private JTextField createTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(250, 35));
        return field;
    }

    private void createButtonPanel() {
        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.setBackground(Color.WHITE);
        
        JPanel buttonPanel = new JPanel(new GridLayout(9, 1, 12, 12)); // Changed to 9 rows
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.setPreferredSize(new Dimension(220, 650));

        JButton findRoomsBtn = createActionButton("Find Available Rooms", new Color(100, 149, 237)); // New button
        JButton reserveBtn = createActionButton("Reserve Room", new Color(144, 238, 144));
        JButton updateBtn = createActionButton("Update Reservation", new Color(135, 206, 250));
        JButton deleteBtn = createActionButton("Cancel Reservation", new Color(250, 128, 114));
        JButton clearBtn = createActionButton("Clear Form", new Color(255, 228, 181));
        JButton refreshBtn = createActionButton("Refresh", new Color(221, 160, 221));
        JButton calculateBtn = createActionButton("Calculate Bill", new Color(255, 215, 0));
        JButton chartBtn = createActionButton("Reservation Chart", new Color(173, 216, 230));
        JButton logoutBtn = createActionButton("Logout", new Color(220, 220, 220));

        // Add the new button at the top
        buttonPanel.add(findRoomsBtn);
        buttonPanel.add(reserveBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(calculateBtn);
        buttonPanel.add(chartBtn);
        buttonPanel.add(logoutBtn);

        buttonContainer.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(buttonContainer, BorderLayout.EAST);

        // Add action listener for the new button
        findRoomsBtn.addActionListener(e -> showAvailableRoomsDialog());
        reserveBtn.addActionListener(e -> reserveRoom());
        updateBtn.addActionListener(e -> updateReservation());
        deleteBtn.addActionListener(e -> deleteReservation());
        clearBtn.addActionListener(e -> clearForm());
        refreshBtn.addActionListener(e -> loadReservations());
        calculateBtn.addActionListener(e -> calculateBill());
        chartBtn.addActionListener(e -> showReservationChart());
        logoutBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose(); // Close hotel app
                new LoginSystem().setVisible(true); // Show login again
            }
        });
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 55));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bg.darker(), 2),
            BorderFactory.createEmptyBorder(12, 8, 12, 8)
        ));
        return btn;
    }

    // === RESERVATION CHART ===
    private void showReservationChart() {
        JDialog chartDialog = new JDialog(this, "Hotel Reservation Analytics", true);
        chartDialog.setSize(900, 600);
        chartDialog.setLocationRelativeTo(this);
        chartDialog.setLayout(new BorderLayout(10, 10));

        // Create chart data
        Map<String, Integer> hotelReservations = new HashMap<>();
        Map<String, Integer> roomTypeStats = new HashMap<>();
        double totalRevenue = 0;
        int totalReservations = 0;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT hotel, room_type, total_amount FROM reservations WHERE status != 'Cancelled'");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String hotel = rs.getString("hotel");
                String roomType = rs.getString("room_type");
                double amount = rs.getDouble("total_amount");

                hotelReservations.put(hotel, hotelReservations.getOrDefault(hotel, 0) + 1);
                roomTypeStats.put(roomType, roomTypeStats.getOrDefault(roomType, 0) + 1);
                totalRevenue += amount;
                totalReservations++;
            }
        } catch (SQLException e) {
            showError("Error loading chart data: " + e.getMessage());
            return;
        }

        // Create chart panel
        JPanel chartPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.WHITE);

        // Hotel reservations chart
        chartPanel.add(createChartPanel("Reservations by Hotel", hotelReservations, Color.BLUE));
        
        // Room type chart
        chartPanel.add(createChartPanel("Room Type Distribution", roomTypeStats, Color.GREEN));

        // Statistics panel
        JPanel statsPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Key Statistics"));
        statsPanel.add(createStatCard("Total Reservations", String.valueOf(totalReservations), Color.CYAN));
        statsPanel.add(createStatCard("Total Revenue", String.format("$%.2f", totalRevenue), Color.ORANGE));
        statsPanel.add(createStatCard("Active Hotels", String.valueOf(hotelReservations.size()), Color.MAGENTA));
        statsPanel.add(createStatCard("Avg. Revenue", String.format("$%.2f", totalReservations > 0 ? totalRevenue / totalReservations : 0), Color.PINK));

        chartDialog.add(chartPanel, BorderLayout.CENTER);
        chartDialog.add(statsPanel, BorderLayout.SOUTH);

        chartDialog.setVisible(true);
    }

    private JPanel createChartPanel(String title, Map<String, Integer> data, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.setBackground(Color.WHITE);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Item", "Reservations"}, 0);
        JTable table = new JTable(model);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        data.forEach((key, value) -> model.addRow(new Object[]{key, value}));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color.brighter());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    // === BILLING ===
    private void calculateBill() {
        try {
            String roomType = (String) roomTypeCombo.getSelectedItem();
            int nights = (int) nightsSpinner.getValue();
            boolean breakfast = breakfastCheckbox.isSelected();
            boolean parking = parkingCheckbox.isSelected();
            int adults = (int) adultsSpinner.getValue();
            int children = (int) childrenSpinner.getValue();

            double roomRate = getRoomRate(roomType);
            double roomCost = roomRate * nights;
            double breakfastCost = breakfast ? 500 * nights : 0;
            double parkingCost = parking ? 50 * nights : 0;
            double extraPersonCost = Math.max(0, adults - 2) * 20 * nights;

            currentTotal = roomCost + breakfastCost + parkingCost + extraPersonCost;
            double tax = currentTotal * 0.1;
            double finalTotal = currentTotal + tax;

            StringBuilder bill = new StringBuilder();
            bill.append("=== HOTEL BILL ===\n\n");
            bill.append(String.format("Room Type: %s\n", roomType));
            bill.append(String.format("Nights: %d\n", nights));
            bill.append(String.format("Guests: %d Adults, %d Children\n\n", adults, children));
            bill.append("-------------------\n");
            bill.append(String.format("Room Cost: $%.2f\n", roomCost));
            if (breakfast) bill.append(String.format("Breakfast: $%.2f\n", breakfastCost));
            if (parking) bill.append(String.format("Parking: $%.2f\n", parkingCost));
            if (extraPersonCost > 0) bill.append(String.format("Extra Person: $%.2f\n", extraPersonCost));
            bill.append(String.format("Subtotal: $%.2f\n", currentTotal));
            bill.append(String.format("Tax (10%%): $%.2f\n", tax));
            bill.append("-------------------\n");
            bill.append(String.format("TOTAL: $%.2f\n", finalTotal));

            billArea.setText(bill.toString());
            totalAmountLabel.setText(String.format("Total: $%.2f", finalTotal));
        } catch (Exception e) {
            showError("Error calculating bill: " + e.getMessage());
        }
    }

    private double getRoomRate(String roomType) {
        return switch (roomType) {
            case "Standard" -> STANDARD_RATE;
            case "Deluxe" -> DELUXE_RATE;
            case "Suite" -> SUITE_RATE;
            default -> STANDARD_RATE;
        };
    }

    private void populateFormFromSelectedRow() {
        if (reservationsTable == null) return;
        
        int row = reservationsTable.getSelectedRow();
        if (row < 0) return;

        Object idObj = tableModel.getValueAt(row, 0);
        Object hotelObj = tableModel.getValueAt(row, 1);
        Object guestObj = tableModel.getValueAt(row, 2);
        Object roomNumObj = tableModel.getValueAt(row, 3);
        Object roomTypeObj = tableModel.getValueAt(row, 4);
        Object contactObj = tableModel.getValueAt(row, 5);
        Object totalObj = tableModel.getValueAt(row, 8);

        hotelCombo.setSelectedItem(hotelObj != null ? hotelObj.toString() : HOTEL_NAMES[0]);
        guestField.setText(guestObj != null ? guestObj.toString() : "");
        roomField.setText(roomNumObj != null ? roomNumObj.toString() : "");
        contactField.setText(contactObj != null ? contactObj.toString() : "");
        roomTypeCombo.setSelectedItem(roomTypeObj != null ? roomTypeObj.toString() : "Standard");
        totalAmountLabel.setText(totalObj != null ? String.format("Total: $%.2f", Double.parseDouble(totalObj.toString())) : "Total: $0.00");
    }

    // === DATABASE ===
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void setupDatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
                String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS reservations (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        hotel VARCHAR(100) NOT NULL,
                        guest VARCHAR(100) NOT NULL,
                        room_number INT NOT NULL,
                        room_type VARCHAR(20) NOT NULL,
                        contact VARCHAR(20) NOT NULL,
                        email VARCHAR(100),
                        adults INT NOT NULL,
                        children INT DEFAULT 0,
                        nights INT NOT NULL,
                        breakfast BOOLEAN DEFAULT FALSE,
                        parking BOOLEAN DEFAULT FALSE,
                        payment_method VARCHAR(20) NOT NULL,
                        total_amount DECIMAL(10,2) NOT NULL,
                        reservation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        check_in_date DATE,
                        check_out_date DATE,
                        status VARCHAR(20) DEFAULT 'Confirmed'
                    )
                    """;
                stmt.execute(createTableSQL);
            }
        } catch (Exception e) {
            showError("Database initialization error: " + e.getMessage());
        }
    }

    private void loadReservations() {
        if (tableModel == null) return;
        
        tableModel.setRowCount(0);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id, hotel, guest, room_number, room_type, contact, reservation_date, " +
                     "check_in_date, check_out_date, total_amount, status FROM reservations ORDER BY reservation_date DESC");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("hotel"),
                    rs.getString("guest"),
                    rs.getInt("room_number"),
                    rs.getString("room_type"),
                    rs.getString("contact"),
                    rs.getTimestamp("reservation_date") != null ?
                        rs.getTimestamp("reservation_date").toLocalDateTime().format(DATE_FORMATTER) : "N/A",
                    rs.getDate("check_out_date") != null ? rs.getDate("check_out_date").toString() : "N/A",
                    rs.getDouble("total_amount"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            showError("Error loading reservations: " + e.getMessage());
        }
    }

    private void reserveRoom() {
        try {
            calculateBill();
            ReservationData data = validateAndGetReservationData();
            if (data == null) return;

            try (Connection conn = getConnection()) {
                if (isRoomAlreadyBooked(conn, data.hotel(), data.roomNumber())) {
                    showError("Room " + data.roomNumber() + " at " + data.hotel() + " is already booked!");
                    return;
                }

                int reservationId = createReservation(conn, data);
                loadReservations();
                showBillConfirmation(reservationId, data);
                clearForm();
            }
        } catch (Exception e) {
            showError("Error reserving room: " + e.getMessage());
        }
    }

    private void showBillConfirmation(int reservationId, ReservationData data) {
        String confirmation = String.format("""
            RESERVATION CONFIRMED!

            Reservation ID: %d
            Hotel: %s
            Guest: %s
            Room: %d (%s)
            Contact: %s
            Nights: %d
            Check-in: %s
            Check-out: %s
            Total Amount: $%.2f
            Status: %s

            Thank you for choosing %s!
            """,
            reservationId, data.hotel(), data.guestName(), data.roomNumber(), data.roomType(),
            data.contact(), data.nights(), data.checkInDate(), data.checkOutDate(),
            data.totalAmount(), data.status(), data.hotel());

        JTextArea textArea = new JTextArea(confirmation);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(450, 350));

        JOptionPane.showMessageDialog(this, scrollPane, "Reservation Confirmed - " + data.hotel(),
            JOptionPane.INFORMATION_MESSAGE);
    }

    private ReservationData validateAndGetReservationData() {
        String hotel = (String) hotelCombo.getSelectedItem();
        String guestName = guestField.getText().trim();
        String roomText = roomField.getText().trim();
        String contact = contactField.getText().trim();
        String email = emailField.getText().trim();
        String roomType = (String) roomTypeCombo.getSelectedItem();
        int nights = (int) nightsSpinner.getValue();
        int adults = (int) adultsSpinner.getValue();
        int children = (int) childrenSpinner.getValue();
        boolean breakfast = breakfastCheckbox.isSelected();
        boolean parking = parkingCheckbox.isSelected();
        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();

        // Validation
        if (guestName.isEmpty() || roomText.isEmpty() || contact.isEmpty()) {
            showError("Fields marked with * are required!");
            return null;
        }

        if (!NAME_PATTERN.matcher(guestName).matches()) {
            showError("Please enter a valid guest name (2-50 alphabetic characters)");
            return null;
        }

        if (!CONTACT_PATTERN.matcher(contact).matches()) {
            showError("Please enter a valid contact number (10-20 digits)");
            return null;
        }

        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return null;
        }

        try {
            int roomNumber = Integer.parseInt(roomText);
            if (roomNumber <= 0 || roomNumber > 1000) {
                showError("Room number must be between 1 and 1000");
                return null;
            }

            LocalDateTime checkIn = LocalDateTime.now();
            LocalDateTime checkOut = checkIn.plusDays(nights);

            double total = currentTotal;
            double tax = total * 0.1;
            double finalTotal = total + tax;

            return new ReservationData(
                hotel, guestName, roomNumber, roomType, contact, email,
                adults, children, nights, breakfast, parking,
                paymentMethod, finalTotal, "Confirmed",
                checkIn.toLocalDate().toString(),
                checkOut.toLocalDate().toString()
            );
        } catch (NumberFormatException e) {
            showError("Room number must be a valid integer");
            return null;
        }
    }

    private boolean isRoomAlreadyBooked(Connection conn, String hotel, int roomNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE hotel = ? AND room_number = ? AND status != 'Cancelled'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hotel);
            stmt.setInt(2, roomNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private int createReservation(Connection conn, ReservationData data) throws SQLException {
        String sql = """
            INSERT INTO reservations(hotel, guest, room_number, room_type, contact, email, adults, children, 
            nights, breakfast, parking, payment_method, total_amount, status, check_in_date, check_out_date) 
            VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, data.hotel());
            stmt.setString(2, data.guestName());
            stmt.setInt(3, data.roomNumber());
            stmt.setString(4, data.roomType());
            stmt.setString(5, data.contact());
            stmt.setString(6, data.email());
            stmt.setInt(7, data.adults());
            stmt.setInt(8, data.children());
            stmt.setInt(9, data.nights());
            stmt.setBoolean(10, data.breakfast());
            stmt.setBoolean(11, data.parking());
            stmt.setString(12, data.paymentMethod());
            stmt.setDouble(13, data.totalAmount());
            stmt.setString(14, data.status());
            stmt.setDate(15, Date.valueOf(data.checkInDate()));
            stmt.setDate(16, Date.valueOf(data.checkOutDate()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return -1;
            }
        }
    }

    private void updateReservation() {
        if (reservationsTable == null) {
            showError("Please select a reservation from the reservations window first.");
            return;
        }
        
        int row = reservationsTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a reservation to update.");
            return;
        }

        Object idObj = tableModel.getValueAt(row, 0);
        if (idObj == null) {
            showError("Selected reservation has no ID.");
            return;
        }
        int id = Integer.parseInt(idObj.toString());

        calculateBill();
        ReservationData data = validateAndGetReservationData();
        if (data == null) return;

        String sql = """
            UPDATE reservations SET hotel=?, guest=?, room_number=?, room_type=?, contact=?, email=?, adults=?, children=?, 
            nights=?, breakfast=?, parking=?, payment_method=?, total_amount=?, check_in_date=?, check_out_date=?, status=? 
            WHERE id=?
            """;

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, data.hotel());
            stmt.setString(2, data.guestName());
            stmt.setInt(3, data.roomNumber());
            stmt.setString(4, data.roomType());
            stmt.setString(5, data.contact());
            stmt.setString(6, data.email());
            stmt.setInt(7, data.adults());
            stmt.setInt(8, data.children());
            stmt.setInt(9, data.nights());
            stmt.setBoolean(10, data.breakfast());
            stmt.setBoolean(11, data.parking());
            stmt.setString(12, data.paymentMethod());
            stmt.setDouble(13, data.totalAmount());
            stmt.setDate(14, Date.valueOf(data.checkInDate()));
            stmt.setDate(15, Date.valueOf(data.checkOutDate()));
            stmt.setString(16, data.status());
            stmt.setInt(17, id);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                showSuccess("Reservation updated successfully.");
                loadReservations();
                clearForm();
            } else {
                showError("No reservation updated. Please check the selected item.");
            }
        } catch (SQLException e) {
            showError("Error updating reservation: " + e.getMessage());
        }
    }

    private void deleteReservation() {
        if (reservationsTable == null) {
            showError("Please select a reservation from the reservations window first.");
            return;
        }
        
        int row = reservationsTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a reservation to delete.");
            return;
        }

        Object idObj = tableModel.getValueAt(row, 0);
        if (idObj == null) {
            showError("Selected reservation has no ID.");
            return;
        }
        int id = Integer.parseInt(idObj.toString());

        int confirm = JOptionPane.showConfirmDialog(this, "Mark this reservation as Cancelled?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE reservations SET status='Cancelled' WHERE id=?";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int updated = stmt.executeUpdate();
            if (updated > 0) {
                showSuccess("Reservation cancelled successfully.");
                loadReservations();
                clearForm();
            } else {
                showError("Failed to cancel reservation.");
            }
        } catch (SQLException e) {
            showError("Error cancelling reservation: " + e.getMessage());
        }
    }

    private void clearForm() {
        hotelCombo.setSelectedIndex(0);
        guestField.setText("");
        roomField.setText("");
        contactField.setText("");
        emailField.setText("");
        roomTypeCombo.setSelectedIndex(0);
        nightsSpinner.setValue(1);
        adultsSpinner.setValue(1);
        childrenSpinner.setValue(0);
        breakfastCheckbox.setSelected(false);
        parkingCheckbox.setSelected(false);
        paymentMethodCombo.setSelectedIndex(0);
        billArea.setText("");
        totalAmountLabel.setText("Total: $0.00");
        currentTotal = 0.0;
        if (reservationsTable != null) {
            reservationsTable.clearSelection();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    // Record for storing reservation data
    private record ReservationData(
        String hotel, String guestName, int roomNumber, String roomType, String contact, String email,
        int adults, int children, int nights, boolean breakfast, boolean parking,
        String paymentMethod, double totalAmount, String status,
        String checkInDate, String checkOutDate
    ) {}

    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new HotelApp("Test User").setVisible(true);
        });
    }
}