package com.mitm.utils;

import com.mitm.models.Message;
import com.mitm.models.User;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom logger for tracking all system activities
 * Especially useful for demonstrating MITM attack interceptions
 */
public class Logger {
    private static final String LOG_DIR = "logs/";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static List<String> recentLogs = new ArrayList<>();
    private static final int MAX_RECENT_LOGS = 100;

    public enum LogLevel {
        INFO, WARNING, ERROR, ATTACK, INTERCEPT
    }

    /**
     * Log a general message
     */
    public static void log(LogLevel level, String component, String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format("[%s] [%s] [%s] %s", timestamp, level, component, message);
        
        // Add to recent logs for web display
        addToRecentLogs(logEntry);
        
        // Print to console
        System.out.println(logEntry);
        
        // Write to file
        writeToFile(component.toLowerCase() + ".log", logEntry);
    }

    /**
     * Log message interception (MITM specific)
     */
    public static void logInterception(Message original, Message modified, String attackerAction) {
        String timestamp = LocalDateTime.now().format(formatter);
        StringBuilder logEntry = new StringBuilder();
        logEntry.append(String.format("[%s] [INTERCEPT] [ATTACKER]\n", timestamp));
        logEntry.append("  Action: ").append(attackerAction).append("\n");
        logEntry.append("  Original Message:\n");
        logEntry.append("    From: ").append(original.getSender()).append("\n");
        logEntry.append("    To: ").append(original.getRecipient()).append("\n");
        logEntry.append("    Content: ").append(original.getContent()).append("\n");
        
        if (modified != null && !original.getContent().equals(modified.getContent())) {
            logEntry.append("  Modified Message:\n");
            logEntry.append("    Content: ").append(modified.getContent()).append("\n");
        }
        
        String logStr = logEntry.toString();
        addToRecentLogs(logStr);
        System.out.println(logStr);
        writeToFile("attacker.log", logStr);
    }

    /**
     * Log credential theft (MITM specific)
     */
    public static void logCredentialTheft(User user) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = String.format(
            "[%s] [ATTACK] [ATTACKER] CREDENTIALS STOLEN - Username: %s, Password: %s",
            timestamp, user.getUsername(), user.getPassword()
        );
        
        addToRecentLogs(logEntry);
        System.out.println(logEntry);
        writeToFile("attacker.log", logEntry);
    }

    /**
     * Log server activity
     */
    public static void logServerActivity(String activity) {
        log(LogLevel.INFO, "SERVER", activity);
    }

    /**
     * Log client activity
     */
    public static void logClientActivity(String activity) {
        log(LogLevel.INFO, "CLIENT", activity);
    }

    /**
     * Log attacker activity
     */
    public static void logAttackerActivity(String activity) {
        log(LogLevel.ATTACK, "ATTACKER", activity);
    }

    /**
     * Write log entry to file
     */
    private static void writeToFile(String filename, String content) {
        try (FileWriter fw = new FileWriter(LOG_DIR + filename, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(content);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    /**
     * Add to recent logs for web display
     */
    private static synchronized void addToRecentLogs(String logEntry) {
        recentLogs.add(logEntry);
        if (recentLogs.size() > MAX_RECENT_LOGS) {
            recentLogs.remove(0);
        }
    }

    /**
     * Get recent logs for web display
     */
    public static synchronized List<String> getRecentLogs() {
        return new ArrayList<>(recentLogs);
    }

    /**
     * Get recent logs as JSON string
     */
    public static synchronized String getRecentLogsAsJson() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < recentLogs.size(); i++) {
            json.append("\"").append(escapeJson(recentLogs.get(i))).append("\"");
            if (i < recentLogs.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    /**
     * Escape special characters for JSON
     */
    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * Clear all logs
     */
    public static synchronized void clearLogs() {
        recentLogs.clear();
    }
}
