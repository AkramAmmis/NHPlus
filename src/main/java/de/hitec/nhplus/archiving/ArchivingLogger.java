package de.hitec.nhplus.archiving;

import de.hitec.nhplus.model.RecordStatus;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logs archiving and deletion actions
 */
public class ArchivingLogger {
    private static final Logger LOGGER = Logger.getLogger(ArchivingLogger.class.getName());
    private static final String LOG_FILE = "archiving.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Logs a status change
     * @param entityType Type of entity (e.g., "Treatment")
     * @param id ID of the entity
     * @param oldStatus Old status
     * @param newStatus New status
     * @param user User who made the change
     */
    public static void logStatusChange(String entityType, long id, RecordStatus oldStatus,
                                       RecordStatus newStatus, String user) {
        String logMessage = String.format("%s | %s | ID: %d | Status: %s -> %s | User: %s",
                LocalDateTime.now().format(FORMATTER),
                entityType,
                id,
                oldStatus,
                newStatus,
                user);

        LOGGER.log(Level.INFO, logMessage);

        // Write to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(logMessage);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error writing to log", e);
        }
    }
}

