package com.mitm.server;

import com.mitm.models.HealthRecord;
import com.mitm.models.Message;
import com.mitm.models.User;
import com.mitm.utils.Logger;
import com.mitm.utils.MessageEncryptor;
import com.mitm.utils.NetworkUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Legitimate Server component
 * Handles client authentication and message processing
 * In MITM attack, this server is unaware of the attacker's presence
 */
public class Server {
    private static final int PORT = 8080;
    private ServerSocket serverSocket;
    private boolean running;
    private Map<String, User> authenticatedUsers;
    private Map<String, String> userDatabase; // username -> password
    private List<HealthRecord> healthRecords; // Store health records

    public Server() {
        this.authenticatedUsers = new ConcurrentHashMap<>();
        this.userDatabase = new HashMap<>();
        this.healthRecords = new ArrayList<>();
        initializeUserDatabase();
    }

    /**
     * Initialize with some demo users
     */
    private void initializeUserDatabase() {
        userDatabase.put("alice", "password123");
        userDatabase.put("bob", "secret456");
        userDatabase.put("admin", "admin789");
        Logger.logServerActivity("User database initialized with demo accounts");
    }

    /**
     * Start the server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            Logger.logServerActivity("Server started on port " + PORT);
            System.out.println("=".repeat(60));
            System.out.println("SERVER STARTED - Listening on port " + PORT);
            System.out.println("=".repeat(60));

            // Accept client connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Logger.logServerActivity("New client connection from " + 
                        clientSocket.getInetAddress().getHostAddress());
                    
                    // Handle each client in a separate thread
                    new Thread(() -> handleClient(clientSocket)).start();
                } catch (IOException e) {
                    if (running) {
                        Logger.log(Logger.LogLevel.ERROR, "SERVER", 
                            "Error accepting client: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "SERVER", 
                "Failed to start server: " + e.getMessage());
        }
    }

    /**
     * Handle individual client connection
     */
    private void handleClient(Socket clientSocket) {
        String clientId = clientSocket.getInetAddress().getHostAddress() + ":" + 
                         clientSocket.getPort();
        Logger.logServerActivity("Handling client: " + clientId);

        try {
            while (!clientSocket.isClosed()) {
                Message message = NetworkUtils.receiveMessage(clientSocket);
                
                if (message == null) {
                    break;
                }

                Logger.logServerActivity("Received message from " + message.getSender() + 
                    " - Type: " + message.getType());

                // Decrypt if encrypted
                if (message.isEncrypted()) {
                    String decrypted = MessageEncryptor.decrypt(message.getContent());
                    message.setContent(decrypted);
                    Logger.logServerActivity("Decrypted message content");
                }

                // Process message based on type
                Message response = processMessage(message, clientId);
                
                // Encrypt response if original was encrypted
                if (message.isEncrypted() && response != null) {
                    response.setEncrypted(true);
                    String encrypted = MessageEncryptor.encrypt(response.getContent());
                    response.setContent(encrypted);
                }

                // Send response
                if (response != null) {
                    NetworkUtils.sendMessage(clientSocket, response);
                    Logger.logServerActivity("Sent response to " + response.getRecipient());
                }
            }
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.WARNING, "SERVER", 
                "Client disconnected: " + clientId);
        } finally {
            NetworkUtils.closeSocket(clientSocket);
            Logger.logServerActivity("Client connection closed: " + clientId);
        }
    }

    /**
     * Process incoming message and generate response
     */
    private Message processMessage(Message message, String clientId) {
        switch (message.getType()) {
            case LOGIN:
                return handleLogin(message, clientId);
            
            case DATA:
                return handleDataRequest(message);
            
            case HEARTBEAT:
                return handleHeartbeat(message);
            
            default:
                return NetworkUtils.createErrorMessage("SERVER", message.getSender(), 
                    "Unknown message type");
        }
    }

    /**
     * Handle login request
     */
    private Message handleLogin(Message message, String clientId) {
        try {
            // Parse username and password from message content
            String[] credentials = message.getContent().split(":");
            if (credentials.length != 2) {
                Logger.log(Logger.LogLevel.WARNING, "SERVER", 
                    "Invalid login format from " + message.getSender());
                return NetworkUtils.createErrorMessage("SERVER", message.getSender(), 
                    "Invalid login format");
            }

            String username = credentials[0];
            String password = credentials[1];

            Logger.logServerActivity("Login attempt - Username: " + username);

            // Verify credentials
            if (userDatabase.containsKey(username) && 
                userDatabase.get(username).equals(password)) {
                
                // Generate session token
                String sessionToken = UUID.randomUUID().toString();
                
                // Create user object
                User user = new User(username, password);
                user.setSessionToken(sessionToken);
                user.setAuthenticated(true);
                user.setIpAddress(clientId);
                
                // Store authenticated user
                authenticatedUsers.put(sessionToken, user);
                
                Logger.logServerActivity("Login successful for user: " + username);
                
                // Create success response
                Message response = new Message("SERVER", message.getSender(), 
                    "LOGIN_SUCCESS:" + sessionToken, Message.MessageType.RESPONSE);
                return response;
            } else {
                Logger.log(Logger.LogLevel.WARNING, "SERVER", 
                    "Login failed for user: " + username);
                return NetworkUtils.createErrorMessage("SERVER", message.getSender(), 
                    "Invalid credentials");
            }
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "SERVER", 
                "Login processing error: " + e.getMessage());
            return NetworkUtils.createErrorMessage("SERVER", message.getSender(), 
                "Login processing failed");
        }
    }

    /**
     * Handle data request (including health data)
     */
    private Message handleDataRequest(Message message) {
        Logger.logServerActivity("Processing data request from " + message.getSender());
        
        String requestData = message.getContent();
        
        // Check if this is health data
        if (requestData.contains("PATIENT_NAME:") && requestData.contains("BLOOD_TYPE:")) {
            return handleHealthDataSubmission(message, requestData);
        }
        
        // Regular data processing
        String responseData = "Processed: " + requestData + " [Server Response]";
        
        Message response = new Message("SERVER", message.getSender(),
            responseData, Message.MessageType.RESPONSE);
        
        Logger.logServerActivity("Data request processed successfully");
        return response;
    }

    /**
     * Handle health data submission
     */
    private Message handleHealthDataSubmission(Message message, String healthData) {
        try {
            // Parse health record
            HealthRecord record = HealthRecord.fromDataString(healthData);
            
            // Generate patient ID
            String patientId = "P" + System.currentTimeMillis();
            record.setPatientId(patientId);
            
            // Store health record
            healthRecords.add(record);
            
            Logger.logServerActivity("Health record stored for patient: " + record.getPatientName());
            Logger.logServerActivity("Patient ID: " + patientId);
            Logger.logServerActivity("Medical Condition: " + record.getMedicalCondition());
            Logger.logServerActivity("Medications: " + record.getMedications());
            
            // Create success response
            String responseData = String.format(
                "✓ Health Information Recorded Successfully\n\n" +
                "Patient ID: %s\n" +
                "Patient Name: %s\n" +
                "Age: %d\n" +
                "Gender: %s\n" +
                "Adhar Number: %s\n" +
                "Blood Type: %s\n" +
                "Medical Condition: %s\n" +
                "Medications: %s\n" +
                "Allergies: %s\n" +
                "Symptoms: %s\n\n" +
                "Your health information has been securely stored in our system.\n" +
                "Total records in database: %d",
                patientId,
                record.getPatientName(),
                record.getAge(),
                record.getGender(),
                record.getAdhar(),
                record.getBloodType(),
                record.getMedicalCondition(),
                record.getMedications(),
                record.getAllergies(),
                record.getSymptoms(),
                healthRecords.size()
            );
            
            Message response = new Message("SERVER", message.getSender(),
                responseData, Message.MessageType.RESPONSE);
            
            Logger.logServerActivity("Health data submission processed successfully");
            
            // Print to console for demo
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📋 NEW HEALTH RECORD STORED");
            System.out.println("=".repeat(60));
            System.out.println("Patient: " + record.getPatientName() + " (ID: " + patientId + ")");
            System.out.println("Age: " + record.getAge() + " | Blood Type: " + record.getBloodType());
            System.out.println("Condition: " + record.getMedicalCondition());
            System.out.println("=".repeat(60));
            
            return response;
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "SERVER",
                "Error processing health data: " + e.getMessage());
            return NetworkUtils.createErrorMessage("SERVER", message.getSender(),
                "Failed to process health data: " + e.getMessage());
        }
    }

    /**
     * Handle heartbeat
     */
    private Message handleHeartbeat(Message message) {
        return new Message("SERVER", message.getSender(), 
            "HEARTBEAT_ACK", Message.MessageType.RESPONSE);
    }

    /**
     * Stop the server
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            Logger.logServerActivity("Server stopped");
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "SERVER", 
                "Error stopping server: " + e.getMessage());
        }
    }

    /**
     * Get server status
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get number of authenticated users
     */
    public int getAuthenticatedUserCount() {
        return authenticatedUsers.size();
    }

    /**
     * Main method to run server standalone
     */
    public static void main(String[] args) {
        Server server = new Server();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down server...");
            server.stop();
        }));
        
        server.start();
    }
}
