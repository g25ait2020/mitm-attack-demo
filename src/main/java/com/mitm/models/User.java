package com.mitm.models;

/**
 * User class representing a client in the system
 * Contains credentials that can be intercepted in MITM attacks
 */
public class User {
    private String username;
    private String password;
    private String sessionToken;
    private boolean authenticated;
    private String ipAddress;

    public User() {
        this.authenticated = false;
    }

    public User(String username, String password) {
        this();
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='***'" +
                ", sessionToken='" + sessionToken + '\'' +
                ", authenticated=" + authenticated +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }

    /**
     * Sanitized version for logging (hides password)
     */
    public String toSafeString() {
        return "User{" +
                "username='" + username + '\'' +
                ", authenticated=" + authenticated +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
