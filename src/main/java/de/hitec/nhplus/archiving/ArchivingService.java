package de.hitec.nhplus.archiving;

import de.hitec.nhplus.model.RecordStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Generic interface for archiving management
 * @param <T> Type of entity to be archived
 */
public interface ArchivingService<T> {

    /**
     * Locks a record.
     * @param id ID of the record to be locked
     * @return true if successful, false otherwise
     */
    boolean lockRecord(long id);

    /**
     * Deletes a record if it is older than the retention period.
     * @param id ID of the record to be deleted
     * @return true if successful, false otherwise
     */
    boolean deleteRecord(long id);

    /**
     * Finds all records that are older than the specified number of years.
     * @param years Number of years
     * @return List of found records
     */
    List<T> findRecordsOlderThan(int years);

    /**
     * Finds all records with a specific status.
     * @param status Status to search for
     * @return List of found records
     */
    List<T> findRecordsByStatus(RecordStatus status);

    /**
     * Logs a status change.
     * @param id ID of the affected record
     * @param oldStatus Old status
     * @param newStatus New status
     * @param date Date of change
     * @param user User who made the change
     */
    void logStatusChange(long id, RecordStatus oldStatus, RecordStatus newStatus,
                         LocalDate date, String user);
}

