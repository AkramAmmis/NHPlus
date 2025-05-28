package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.UserDao;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.model.UserRole;
import de.hitec.nhplus.utils.AuthorizationManager;

import java.util.ArrayList;

/**
 * Controller für die Benutzerverwaltung
 */
public class UserManagementController {

    private UserDao userDao;

    /**
     * Konstruktor
     */
    public UserManagementController() {
        this.userDao = DaoFactory.getDaoFactory().createUserDAO();
    }

    /**
     * Gibt alle Benutzer zurück
     * @return Liste aller Benutzer
     */
    public ArrayList<User> getAllUsers() {
        return userDao.readAllUsers();
    }

    /**
     * Erstellt einen neuen Benutzer
     * @param username Benutzername
     * @param password Passwort
     * @param firstName Vorname
     * @param lastName Nachname
     * @param email E-Mail-Adresse
     * @param phoneNumber Telefonnummer
     * @param role Benutzerrolle
     * @return Der erstellte Benutzer oder null bei Fehler
     */
    public User createUser(String username, String password, String firstName, String lastName, 
                           String email, String phoneNumber, UserRole role) {
        // Prüfen, ob der aktuelle Benutzer ein Administrator ist
        if (!AuthorizationManager.getInstance().isAdmin()) {
            return null;
        }

        User user = new User(username, password, firstName, lastName, email, phoneNumber, role);
        return userDao.createUser(user);
    }

    /**
     * Aktualisiert einen Benutzer
     * @param user Der zu aktualisierende Benutzer
     * @return true bei Erfolg, sonst false
     */
    public boolean updateUser(User user) {
        // Prüfen, ob der aktuelle Benutzer ein Administrator ist
        if (!AuthorizationManager.getInstance().isAdmin()) {
            return false;
        }

        userDao.updateUser(user);
        return true;
    }

    /**
     * Löscht einen Benutzer
     * @param user Der zu löschende Benutzer
     * @return true bei Erfolg, sonst false
     */
    public boolean deleteUser(User user) {
        // Prüfen, ob der aktuelle Benutzer ein Administrator ist
        if (!AuthorizationManager.getInstance().isAdmin()) {
            return false;
        }

        // Admin-Benutzer kann nicht gelöscht werden
        if (user.isAdmin() && "admin".equals(user.getUsername())) {
            return false;
        }

        userDao.delete(user);
        return true;
    }

    /**
     * Prüft, ob ein Benutzername bereits vergeben ist
     * @param username Der zu prüfende Benutzername
     * @return true wenn bereits vergeben, sonst false
     */
    public boolean isUsernameAlreadyTaken(String username) {
        ArrayList<User> users = userDao.readAllUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gibt einen Benutzer anhand seiner ID zurück
     * @param uid Die Benutzer-ID
     * @return Der gefundene Benutzer oder null
     */
    public User getUserById(long uid) {
        return userDao.readUser(uid);
    }
}
