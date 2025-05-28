package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.User;

import java.util.ArrayList;

/**
 * Datenzugriffsschnittstelle für User-Objekte
 */
public interface UserDao extends Dao<User> {

    /**
     * Erstellt die User-Tabelle, falls sie noch nicht existiert
     */
    void createTable();

    /**
     * Authentifiziert einen Benutzer anhand von Benutzername und Passwort
     * @param username Benutzername
     * @param password Passwort
     * @return User-Objekt bei erfolgreicher Authentifizierung, sonst null
     */
    User authenticate(String username, String password);

    /**
     * Erstellt einen neuen Benutzer
     * @param user Das zu erstellende User-Objekt
     * @return Das erstellte User-Objekt mit gesetzter ID
     */
    User createUser(User user);

    /**
     * Gibt alle Benutzer zurück
     * @return Liste aller Benutzer
     */
    ArrayList<User> readAllUsers();

    /**
     * Liest einen Benutzer anhand seiner ID
     * @param uid ID des Benutzers
     * @return Das gefundene User-Objekt oder null
     */
    User readUser(long uid);

    /**
     * Aktualisiert einen Benutzer
     * @param user Das zu aktualisierende User-Objekt
     */
    void updateUser(User user);

    /**
     * Löscht einen Benutzer
     * @param user Das zu löschende User-Objekt
     */
    void delete(User user);

    /**
     * Sucht einen Benutzer anhand des Benutzernamens
     * @param username Der gesuchte Benutzername
     * @return Das gefundene User-Objekt oder null
     */
    User findByUsername(String username);
}
