package de.hitec.nhplus.model;

import java.time.LocalDateTime;

public class LoginLog {
    private int id;
    private String username;
    private String ipAddress;
    private LocalDateTime timestamp;
    private boolean successful;
    private String failureReason;

    public LoginLog(String username, String ipAddress, boolean successful, String failureReason) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
        this.successful = successful;
        this.failureReason = failureReason;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public boolean isSuccessful() { return successful; }
    public void setSuccessful(boolean successful) { this.successful = successful; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
