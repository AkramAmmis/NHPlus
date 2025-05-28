package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.Caregiver;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Interface für den Datenbankzugriff auf Caregiver-Objekte
 */
public interface CaregiverDao extends Dao<Caregiver> {

    /**
     * Erstellt die benötigte Tabelle in der Datenbank
     */
    void createTable();

    /**
     * Erstellt einen neuen Pfleger in der Datenbank
     * @param caregiver Der zu erstellende Pfleger
     * @return Der erstellte Pfleger mit gesetzter ID
     */
    Caregiver createCaregiver(Caregiver caregiver);

    /**
     * Gibt alle Pfleger zurück
     * @return Eine Liste aller Pfleger
     */
    ArrayList<Caregiver> readAllCaregivers();

    /**
     * Liest einen Pfleger anhand seiner ID aus der Datenbank, fängt Exceptions ab
     * @param cid ID des Pflegers
     * @return Caregiver-Objekt oder null
     */
    Caregiver readCaregiver(long cid);

    /**
     * Aktualisiert einen Pfleger in der Datenbank, fängt Exceptions ab
     * @param caregiver zu aktualisierender Pfleger
     */
    void updateCaregiver(Caregiver caregiver);

    /**
     * Löscht einen Pfleger aus der Datenbank
     * @param caregiver zu löschender Pfleger
     */
    void delete(Caregiver caregiver);

    /**
     * Authentifiziert einen Pfleger mit Benutzername und Passwort
     * @param username Benutzername
     * @param password Passwort
     * @return Caregiver-Objekt wenn Login erfolgreich, sonst null
     */
    Caregiver authenticate(String username, String password);
}