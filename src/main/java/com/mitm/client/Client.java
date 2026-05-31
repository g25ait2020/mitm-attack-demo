package com.mitm.client;

import com.mitm.models.Message;
import com.mitm.models.User;
import com.mitm.utils.Logger;
import com.mitm.utils.MessageEncryptor;
import com.mitm.utils.NetworkUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client component
 * Connects to server (or unknowingly to MITM attacker)
 * Sends login credentials and messages
 */
public class Client {
    private String serverHost;
    private int serverPort;
    private Socket socket;
    private User currentUser;
    private boolean connected;
    private boolean useEncryption;

    public Client(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.connected = false;
        this.useEncryption = false;
    }

    /**
     * Connect to server
     */
    public boolean connect() {
        try {
            socket = new Socket(serverHost, serverPort);
            connected = true;
            Logger.logClientActivity("Connected to server at " + serverHost + ":" + serverPort);
            System.out.println("=".repeat(60));
            System.out.println("CLIENT CONNECTED to " + serverHost + ":" + serverPort);
            System.out.println("=".repeat(60));
            return true;
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "CLIENT", 
                "Failed to connect to server: " + e.getMessage());
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }

    /**
     * Login to server
     */
    public boolean login(String username, String password) {
        if (!connected) {
            Logger.log(Logger.LogLevel.ERROR, "CLIENT", "Not connected to server");
            return false;
        }

        try {
            // Create login message
            String credentials = username + ":" + password;
            Message loginMessage = new Message("CLIENT", "SERVER", 
                credentials, Message.MessageType.LOGIN);
            
            // Encrypt if enabled
            if (useEncryption) {
                loginMessage.setEncrypted(true);
                String encrypted = MessageEncryptor.encrypt(credentials);
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
                Logger.log(Logger.LogLevel.ERROR, "CLIENT", "No response from server");
                return false;
            }

            // Decrypt response if encrypted
            String responseContent = response.getContent();
            if (response.isEncrypted()) {
                responseContent = MessageEncryptor.decrypt(responseContent);
            }

            // Check if login successful
            if (responseContent.startsWith("LOGIN_SUCCESS:")) {
                String sessionToken = responseContent.substring("LOGIN_SUCCESS:".length());
                currentUser = new User(username, password);
                currentUser.setSessionToken(sessionToken);
                currentUser.setAuthenticated(true);
                
                Logger.logClientActivity("Login successful! Session token: " + sessionToken);
                System.out.println("✓ Login successful!");
                return true;
            } else {
                Logger.log(Logger.LogLevel.WARNING, "CLIENT", "Login failed: " + responseContent);
                System.out.println("✗ Login failed: " + responseContent);
                return false;
            }
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "CLIENT", 
                "Login error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send a data message to server
     */
    public String sendData(String data) {
        if (!connected) {
            Logger.log(Logger.LogLevel.ERROR, "CLIENT", "Not connected to server");
            return null;
        }

        if (currentUser == null || !currentUser.isAuthenticated()) {
            Logger.log(Logger.LogLevel.ERROR, "CLIENT", "Not authenticated");
            return null;
        }

        try {
            // Create data message
            Message dataMessage = new Message("CLIENT", "SERVER", 
                data, Message.MessageType.DATA);
            
            // Encrypt if enabled
            if (useEncryption) {
                dataMessage.setEncrypted(true);
                String encrypted = MessageEncryptor.encrypt(data);
                dataMessage.setContent(encrypted);
                Logger.logClientActivity("Sending encrypted data");
            } else {
                Logger.logClientActivity("Sending UNENCRYPTED data (vulnerable to MITM!)");
            }

            // Send message
            NetworkUtils.sendMessage(socket, dataMessage);
            Logger.logClientActivity("Data sent: " + data);

            // Wait for response
            Message response = NetworkUtils.receiveMessage(socket);
            
            if (response == null) {
                Logger.log(Logger.LogLevel.ERROR, "CLIENT", "No response from server");
                return null;
            }

            // Decrypt response if encrypted
            String responseContent = response.getContent();
            if (response.isEncrypted()) {
                responseContent = MessageEncryptor.decrypt(responseContent);
            }

            Logger.logClientActivity("Received response: " + responseContent);
            return responseContent;
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "CLIENT", 
                "Error sending data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Disconnect from server
     */
    public void disconnect() {
        if (socket != null) {
            NetworkUtils.closeSocket(socket);
            connected = false;
            Logger.logClientActivity("Disconnected from server");
            System.out.println("Disconnected from server");
        }
    }

    /**
     * Enable or disable encryption
     */
    public void setEncryption(boolean enabled) {
        this.useEncryption = enabled;
        Logger.logClientActivity("Encryption " + (enabled ? "ENABLED" : "DISABLED"));
        System.out.println("Encryption " + (enabled ? "ENABLED" : "DISABLED"));
    }

    /**
     * Check if connected
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Check if authenticated
     */
    public boolean isAuthenticated() {
        return currentUser != null && currentUser.isAuthenticated();
    }

    /**
     * Get current user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    
    /**
     * Main method to run client standalone
     */
    public static void main(String[] args) {
        // Default: connect to server on port 8080
        // For MITM demo: connect to attacker on port 8081
        
        String host = "localhost";
        int port = 8080;

        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        System.out.println("Connecting to " + host + ":" + port);
        System.out.println("(Use port 8080 for direct server, 8081 for MITM attack demo)");

        Client client = new Client(host, port);
        //client.startInteractiveMode();
    }
}
