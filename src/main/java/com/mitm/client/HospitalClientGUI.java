package com.mitm.client;

import com.mitm.models.HealthRecord;
import com.mitm.models.Message;
import com.mitm.models.User;
import com.mitm.utils.Logger;
import com.mitm.utils.NetworkUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.Socket;

/**
 * Hospital Client GUI
 * Provides a graphical interface for patients to login and submit health information
 * Demonstrates how sensitive medical data can be intercepted in MITM attacks
 */
public class HospitalClientGUI extends JFrame {
    private String serverHost;
    private int serverPort;
    private Socket socket;
    private User currentUser;
    private boolean connected;
    private boolean useEncryption;

    // GUI Components
    private JPanel loginPanel;
    private JPanel healthDataPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton connectButton;
    private JLabel statusLabel;
    private JCheckBox encryptionCheckBox;

    // Health Data Fields
    private JTextField patientNameField;
    private JTextField ageField;
    private JTextField genderField;
    private JTextField adharField;
    private JComboBox<String> bloodTypeCombo;
    private JTextArea medicalConditionArea;
    private JTextArea medicationsArea;
    private JTextArea allergiesArea;
    private JTextArea symptomsArea;
    private JButton submitHealthDataButton;
    private JTextArea responseArea;

    public HospitalClientGUI(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.connected = false;
        this.useEncryption = false;

        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("City Hospital - Patient Portal");
        setSize(700, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel with CardLayout
        JPanel mainPanel = new JPanel(new CardLayout());
        
        // Login Panel
        loginPanel = createLoginPanel();
        mainPanel.add(loginPanel, "LOGIN");

        // Health Data Panel
        healthDataPanel = createHealthDataPanel();
        mainPanel.add(healthDataPanel, "HEALTH_DATA");

        add(mainPanel, BorderLayout.CENTER);

        // Status Panel
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);

        Logger.logClientActivity("Hospital Client GUI initialized");
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(41, 128, 185));
        panel.setPreferredSize(new Dimension(700, 80));
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("CITY HOSPITAL", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Patient Health Information System", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(236, 240, 241));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(new Color(41, 128, 185));
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);

        panel.add(textPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        panel.setBackground(Color.WHITE);

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome to Patient Portal");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(welcomeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel infoLabel = new JLabel("Please login to access your health records");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setForeground(Color.GRAY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(infoLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Connection section
        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        connectionPanel.setBackground(Color.WHITE);
        //JLabel serverLabel = new JLabel("Server: " + serverHost + ":" + serverPort);
        //serverLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        //connectionPanel.add(serverLabel);
        
        connectButton = new JButton("Connect to Hospital Server");
        connectButton.setBackground(new Color(46, 204, 113));
        connectButton.setForeground(Color.BLACK);
        connectButton.setFocusPainted(false);
        connectButton.addActionListener(e -> connectToServer());
        connectionPanel.add(connectButton);
        panel.add(connectionPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Username field
        JLabel usernameLabel = new JLabel("Patient ID / Username:");
        usernameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(usernameLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        usernameField = new JTextField(20);
        usernameField.setMaximumSize(new Dimension(300, 30));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(passwordLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        passwordField = new JPasswordField(20);
        passwordField.setMaximumSize(new Dimension(300, 30));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(passwordField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Encryption checkbox
        encryptionCheckBox = new JCheckBox("Enable Encryption (Secure Connection)");
        encryptionCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        encryptionCheckBox.setBackground(Color.WHITE);
        encryptionCheckBox.addActionListener(e -> {
            useEncryption = encryptionCheckBox.isSelected();
            Logger.logClientActivity("Encryption " + (useEncryption ? "ENABLED" : "DISABLED"));
        });
        panel.add(encryptionCheckBox);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Login button
        loginButton = new JButton("Login to Patient Portal");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setBackground(new Color(52, 152, 219));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.setEnabled(false);
        loginButton.addActionListener(e -> performLogin());
        panel.add(loginButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

 

        return panel;
    }

    private JPanel createHealthDataPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        panel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Patient Health Information Form");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Patient Name
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Patient Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        patientNameField = new JTextField(20);
        formPanel.add(patientNameField, gbc);
        row++;

        // Age
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        ageField = new JTextField(20);
        formPanel.add(ageField, gbc);
        row++;

        // Gender
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        genderField = new JTextField(20);
        formPanel.add(genderField, gbc);
        row++;

        // Adhar Card Number

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Adhaar Number:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        adharField = new JTextField(20);
        formPanel.add(adharField, gbc);
        row++;

        // Blood Type
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Blood Group:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        bloodTypeCombo = new JComboBox<>(bloodTypes);
        formPanel.add(bloodTypeCombo, gbc);
        row++;

        // Medical Condition
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Medical Condition:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        medicalConditionArea = new JTextArea(3, 20);
        medicalConditionArea.setLineWrap(true);
        medicalConditionArea.setWrapStyleWord(true);
        JScrollPane conditionScroll = new JScrollPane(medicalConditionArea);
        formPanel.add(conditionScroll, gbc);
        row++;

        // Current Medications
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Current Medications:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        medicationsArea = new JTextArea(3, 20);
        medicationsArea.setLineWrap(true);
        medicationsArea.setWrapStyleWord(true);
        JScrollPane medsScroll = new JScrollPane(medicationsArea);
        formPanel.add(medsScroll, gbc);
        row++;

        // Allergies
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Allergies:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        allergiesArea = new JTextArea(2, 20);
        allergiesArea.setLineWrap(true);
        allergiesArea.setWrapStyleWord(true);
        JScrollPane allergiesScroll = new JScrollPane(allergiesArea);
        formPanel.add(allergiesScroll, gbc);
        row++;

        // Current Symptoms
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Current Symptoms:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        symptomsArea = new JTextArea(3, 20);
        symptomsArea.setLineWrap(true);
        symptomsArea.setWrapStyleWord(true);
        JScrollPane symptomsScroll = new JScrollPane(symptomsArea);
        formPanel.add(symptomsScroll, gbc);
        row++;

        // Submit Button
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        submitHealthDataButton = new JButton("Submit Health Information");
        submitHealthDataButton.setBackground(new Color(231, 76, 60));
        submitHealthDataButton.setForeground(Color.BLACK);
        submitHealthDataButton.setFocusPainted(false);
        submitHealthDataButton.setPreferredSize(new Dimension(250, 40));
        submitHealthDataButton.addActionListener(e -> submitHealthData());
        formPanel.add(submitHealthDataButton, gbc);

        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        panel.add(formScrollPane, BorderLayout.CENTER);

        // Response Area
        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.setBorder(BorderFactory.createTitledBorder("Server Response"));
        responsePanel.setBackground(Color.WHITE);
        responseArea = new JTextArea(5, 40);
        responseArea.setEditable(false);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setBackground(new Color(245, 245, 245));
        JScrollPane responseScroll = new JScrollPane(responseArea);
        responsePanel.add(responseScroll, BorderLayout.CENTER);
        panel.add(responsePanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel("[!] Not connected to server");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(statusLabel);

        return panel;
    }

    private void connectToServer() {
        try {
            socket = new Socket(serverHost, serverPort);
            connected = true;
            Logger.logClientActivity("Connected to hospital server at " + serverHost + ":" + serverPort);
            
            statusLabel.setText("✓ Connected to hospital server");
            statusLabel.setForeground(new Color(46, 204, 113));
            connectButton.setEnabled(false);
            loginButton.setEnabled(true);
            
            JOptionPane.showMessageDialog(this, 
                "Successfully connected to hospital server!", 
                "Connection Success", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "CLIENT", 
                "Failed to connect to server: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Failed to connect to hospital server:\n" + e.getMessage(), 
                "Connection Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performLogin() {
        if (!connected) {
            JOptionPane.showMessageDialog(this, 
                "Please connect to the server first!", 
                "Not Connected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both username and password!", 
                "Invalid Input", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Create login message
            String credentials = username + ":" + password;
            Message loginMessage = new Message("CLIENT", "SERVER", 
                credentials, Message.MessageType.LOGIN);
            
            // Encrypt if enabled
            if (useEncryption) {
                loginMessage.setEncrypted(true);
                String encrypted = com.mitm.utils.MessageEncryptor.encrypt(credentials);
                loginMessage.setContent(encrypted);
                Logger.logClientActivity("Sending encrypted login request");
            } else {
                Logger.logClientActivity("Sending UNENCRYPTED login request (vulnerable to MITM!)");
            }

            // Send login request
            NetworkUtils.sendMessage(socket, loginMessage);
            Logger.logClientActivity("Login request sent for user: " + username);

            // Wait for response
            Message response = NetworkUtils.receiveMessage(socket);
            
            if (response == null) {
                JOptionPane.showMessageDialog(this, 
                    "No response from server!", 
                    "Login Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Decrypt response if encrypted
            String responseContent = response.getContent();
            if (response.isEncrypted()) {
                responseContent = com.mitm.utils.MessageEncryptor.decrypt(responseContent);
            }

            // Check if login successful
            if (responseContent.startsWith("LOGIN_SUCCESS:")) {
                String sessionToken = responseContent.substring("LOGIN_SUCCESS:".length());
                currentUser = new User(username, password);
                currentUser.setSessionToken(sessionToken);
                currentUser.setAuthenticated(true);
                
                Logger.logClientActivity("Login successful! Session token: " + sessionToken);
                
                statusLabel.setText("✓ Logged in as: " + username);
                statusLabel.setForeground(new Color(46, 204, 113));
                
                // Switch to health data panel
                CardLayout cl = (CardLayout) loginPanel.getParent().getLayout();
                cl.show(loginPanel.getParent(), "HEALTH_DATA");
                
                JOptionPane.showMessageDialog(this, 
                    "Login successful!\nWelcome, " + username, 
                    "Login Success", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                Logger.log(Logger.LogLevel.WARNING, "CLIENT", "Login failed: " + responseContent);
                JOptionPane.showMessageDialog(this, 
                    "Login failed:\n" + responseContent, 
                    "Login Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "CLIENT", 
                "Login error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Login error:\n" + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitHealthData() {
        if (!connected || currentUser == null || !currentUser.isAuthenticated()) {
            JOptionPane.showMessageDialog(this, 
                "You must be logged in to submit health data!", 
                "Not Authenticated", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate input
        String patientName = patientNameField.getText().trim();
        String ageStr = ageField.getText().trim();
        String gender = genderField.getText().trim();
        String adhar = adharField.getText().trim();
        
        if (patientName.isEmpty() || ageStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in at least Patient Name and Age!",
                "Invalid Input",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            
            // Create health record
            HealthRecord record = new HealthRecord(
                patientName,
                age,
                gender,
                adhar,
                (String) bloodTypeCombo.getSelectedItem(),
                medicalConditionArea.getText().trim(),
                medicationsArea.getText().trim(),
                allergiesArea.getText().trim(),
                symptomsArea.getText().trim()
            );

            // Create message with health data
            String healthData = record.toDataString();
            Message dataMessage = new Message("CLIENT", "SERVER", 
                healthData, Message.MessageType.DATA);
            
            // Encrypt if enabled
            if (useEncryption) {
                dataMessage.setEncrypted(true);
                String encrypted = com.mitm.utils.MessageEncryptor.encrypt(healthData);
                dataMessage.setContent(encrypted);
                Logger.logClientActivity("Sending encrypted health data");
            } else {
                Logger.logClientActivity("Sending UNENCRYPTED health data (vulnerable to MITM!)");
            }

            // Send message
            NetworkUtils.sendMessage(socket, dataMessage);
            Logger.logClientActivity("Health data sent for patient: " + patientName);

            // Wait for response
            Message response = NetworkUtils.receiveMessage(socket);
            
            if (response == null) {
                responseArea.setText("Error: No response from server");
                return;
            }

            // Decrypt response if encrypted
            String responseContent = response.getContent();
            if (response.isEncrypted()) {
                responseContent = com.mitm.utils.MessageEncryptor.decrypt(responseContent);
            }

            Logger.logClientActivity("Received response: " + responseContent);
            responseArea.setText(responseContent);
            
            JOptionPane.showMessageDialog(this, 
                "Health information submitted successfully!\n\n" + 
                "Your medical data has been recorded.", 
                "Submission Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid age (number)!", 
                "Invalid Age", 
                JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "CLIENT", 
                "Error sending health data: " + e.getMessage());
            responseArea.setText("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error submitting health data:\n" + e.getMessage(), 
                "Submission Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Default: connect to server on port 8080
        // For MITM demo: connect to attacker on port 8081
        
        String host = "localhost";
        int port = 8080;

        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String finalHost = host;
        final int finalPort = port;

        SwingUtilities.invokeLater(() -> {
            new HospitalClientGUI(finalHost, finalPort);
        });

        System.out.println("=".repeat(60));
        System.out.println("HOSPITAL CLIENT GUI STARTED");
        System.out.println("Server: " + host + ":" + port);
        System.out.println("(Use port 8080 for direct server, 8081 for MITM attack demo)");
        System.out.println("=".repeat(60));
    }
}