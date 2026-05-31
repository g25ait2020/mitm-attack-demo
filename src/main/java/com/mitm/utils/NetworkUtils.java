package com.mitm.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mitm.models.Message;
import com.mitm.models.User;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Network utility class for handling socket communication
 * Provides methods for sending and receiving messages between components
 */
public class NetworkUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String MESSAGE_DELIMITER = "\n###END###\n";

    /**
     * Send a message through a socket
     */
    public static void sendMessage(Socket socket, Message message) throws IOException {
        String json = gson.toJson(message);
        sendString(socket, json);
        Logger.log(Logger.LogLevel.INFO, "NETWORK", 
            "Sent message from " + message.getSender() + " to " + message.getRecipient());
    }

    /**
     * Send a string through a socket
     */
    public static void sendString(Socket socket, String data) throws IOException {
        PrintWriter out = new PrintWriter(
            new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), 
            true
        );
        out.println(data + MESSAGE_DELIMITER);
        out.flush();
    }

    /**
     * Receive a message from a socket
     */
    public static Message receiveMessage(Socket socket) throws IOException {
        String json = receiveString(socket);
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, Message.class);
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "NETWORK", 
                "Failed to parse message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Receive a string from a socket
     */
    public static String receiveString(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
        );
        
        StringBuilder data = new StringBuilder();
        String line;
        
        while ((line = in.readLine()) != null) {
            if (line.equals("###END###")) {
                break;
            }
            data.append(line).append("\n");
        }
        
        return data.toString().trim();
    }

    /**
     * Send a user object through a socket
     */
    public static void sendUser(Socket socket, User user) throws IOException {
        String json = gson.toJson(user);
        sendString(socket, json);
    }

    /**
     * Receive a user object from a socket
     */
    public static User receiveUser(Socket socket) throws IOException {
        String json = receiveString(socket);
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, User.class);
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "NETWORK", 
                "Failed to parse user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convert Message to JSON string
     */
    public static String messageToJson(Message message) {
        return gson.toJson(message);
    }

    /**
     * Convert JSON string to Message
     */
    public static Message jsonToMessage(String json) {
        try {
            return gson.fromJson(json, Message.class);
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "NETWORK", 
                "Failed to parse JSON to message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Convert User to JSON string
     */
    public static String userToJson(User user) {
        return gson.toJson(user);
    }

    /**
     * Convert JSON string to User
     */
    public static User jsonToUser(String json) {
        try {
            return gson.fromJson(json, User.class);
        } catch (Exception e) {
            Logger.log(Logger.LogLevel.ERROR, "NETWORK", 
                "Failed to parse JSON to user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if a port is available
     */
    public static boolean isPortAvailable(int port) {
        try (Socket socket = new Socket("localhost", port)) {
            return false; // Port is in use
        } catch (IOException e) {
            return true; // Port is available
        }
    }

    /**
     * Get local IP address
     */
    public static String getLocalIPAddress() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    /**
     * Close socket safely
     */
    public static void closeSocket(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                Logger.log(Logger.LogLevel.ERROR, "NETWORK", 
                    "Failed to close socket: " + e.getMessage());
            }
        }
    }

    /**
     * Create a response message
     */
    public static Message createResponse(Message request, String content) {
        Message response = new Message();
        response.setSender(request.getRecipient());
        response.setRecipient(request.getSender());
        response.setContent(content);
        response.setType(Message.MessageType.RESPONSE);
        response.setEncrypted(request.isEncrypted());
        return response;
    }

    /**
     * Create an error message
     */
    public static Message createErrorMessage(String sender, String recipient, String error) {
        Message message = new Message();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent("ERROR: " + error);
        message.setType(Message.MessageType.ERROR);
        return message;
    }
}
