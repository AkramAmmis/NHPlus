package de.hitec.nhplus.datastorage;

import java.sql.SQLException;
import java.util.List;

/**
 * Generic Data Access Object (DAO) interface.
 * @param <T> The type of the entity.
 */
public interface Dao<T> {
    /**
     * Creates a new record for the entity in the database.
     * @param t The entity object to create.
     * @return The generated ID of the new entity.
     * @throws SQLException if a database access error occurs.
     */
    long create(T t) throws SQLException; // Geändert: Gibt jetzt long zurück

    /**
     * Reads an entity by its ID.
     * @param key The ID of the entity.
     * @return The entity object if found, otherwise null.
     * @throws SQLException if a database access error occurs.
     */
    T read(long key) throws SQLException;

    /**
     * Reads all entities from the database.
     * @return A list of all entities.
     * @throws SQLException if a database access error occurs.
     */
    List<T> readAll() throws SQLException;

    /**
     * Updates an existing entity record in the database.
     * @param t The entity object to update.
     * @throws SQLException if a database access error occurs.
     */
    void update(T t) throws SQLException;

    /**
     * Deletes an entity by its ID.
     * @param key The ID of the entity to delete.
     * @throws SQLException if a database access error occurs.
     */
    void deleteById(long key) throws SQLException;
}