package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.Caregiver;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface for data access operations for caregivers.
 */
public interface CaregiverDao extends Dao<Caregiver> {
    /**
     * Creates a new caregiver record in the database.
     * @param caregiver The caregiver object to create.
     * @return The generated ID of the new caregiver.
     * @throws SQLException if a database access error occurs.
     */
    long create(Caregiver caregiver) throws SQLException;

    /**
     * Reads a caregiver by its ID.
     * @param key The ID of the caregiver.
     * @return The caregiver object if found, otherwise null.
     * @throws SQLException if a database access error occurs.
     */
    Caregiver read(long key) throws SQLException;

    /**
     * Reads all caregivers from the database.
     * @return A list of all caregivers.
     * @throws SQLException if a database access error occurs.
     */
    List<Caregiver> readAll() throws SQLException;

    /**
     * Updates an existing caregiver record in the database.
     * @param caregiver The caregiver object to update.
     * @throws SQLException if a database access error occurs.
     */
    void update(Caregiver caregiver) throws SQLException;

    /**
     * Deletes a caregiver by its ID.
     * @param key The ID of the caregiver to delete.
     * @throws SQLException if a database access error occurs.
     */
    void deleteById(long key) throws SQLException;
}