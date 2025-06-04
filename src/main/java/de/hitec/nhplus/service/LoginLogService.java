package de.hitec.nhplus.service;

import de.hitec.nhplus.model.LoginLog;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoginLogService {
   
    private static final String LOG_FILE = "login_logs.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void logLoginAttempt(String username, String ipAddress, boolean successful, String failureReason) {
        LoginLog log = new LoginLog(username, ipAddress, successful, failureReason);
        writeToFile(log);
    }

    private void writeToFile(LoginLog log) {
        File file = new File(LOG_FILE);
        try {
            // Ensure the file exists
            if (!file.exists()) {
                file.createNewFile();
            }
            try (FileWriter writer = new FileWriter(file, true)) {
                String logEntry = String.format("%s|%s|%s|%s|%s%n",
                    log.getTimestamp().format(FORMATTER),
                    log.getUsername(),
                    log.getIpAddress(),
                    log.isSuccessful() ? "SUCCESS" : "FAILED",
                    log.getFailureReason() != null ? log.getFailureReason() : ""
                );
                writer.write(logEntry);
            }
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    public List<LoginLog> getRecentLogs(int limit) {
        List<LoginLog> logs = new ArrayList<>();
        File file = new File(LOG_FILE);
        if (!file.exists()) {
            return logs;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null && logs.size() < limit) {
                LoginLog log = parseLogLine(line);
                if (log != null) {
                    logs.add(0, log); // Add to beginning for reverse chronological order
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading log file: " + e.getMessage());
        }
        return logs;
    }

    private LoginLog parseLogLine(String line) {
        String[] parts = line.split("\\|");
        if (parts.length >= 4) {
            try {
                LocalDateTime timestamp = LocalDateTime.parse(parts[0], FORMATTER);
                String username = parts[1];
                String ipAddress = parts[2];
                boolean successful = "SUCCESS".equals(parts[3]);
                String failureReason = parts.length > 4 ? parts[4] : null;
                
                LoginLog log = new LoginLog(username, ipAddress, successful, failureReason);
                log.setTimestamp(timestamp);
                return log;
            } catch (Exception e) {
                System.err.println("Error parsing log line: " + line);
            }
        }
        return null;
    }
}
