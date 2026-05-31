package com.mitm.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message class representing communication between client and server
 * Used to demonstrate data interception in MITM attacks
 */
public class Message {
    private String id;
    private String sender;
    private String recipient;
    private String content;
    private String timestamp;
    private MessageType type;
    private boolean encrypted;
    private boolean intercepted;
    private boolean modified;

    public enum MessageType {
        LOGIN,
        DATA,
        RESPONSE,
        ERROR,
        HEARTBEAT
    }

    public Message() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.encrypted = false;
        this.intercepted = false;
        this.modified = false;
    }

    public Message(String sender, String recipient, String content, MessageType type) {
        this();
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.type = type;
        this.id = generateId();
    }

    private String generateId() {
        return System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isIntercepted() {
        return intercepted;
    }

    public void setIntercepted(boolean intercepted) {
        this.intercepted = intercepted;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", content='" + content + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", type=" + type +
                ", encrypted=" + encrypted +
                ", intercepted=" + intercepted +
                ", modified=" + modified +
                '}';
    }

    /**
     * Create a copy of this message (useful for MITM interception)
     */
    public Message copy() {
        Message copy = new Message();
        copy.setId(this.id);
        copy.setSender(this.sender);
        copy.setRecipient(this.recipient);
        copy.setContent(this.content);
        copy.setTimestamp(this.timestamp);
        copy.setType(this.type);
        copy.setEncrypted(this.encrypted);
        copy.setIntercepted(this.intercepted);
        copy.setModified(this.modified);
        return copy;
    }
}
