package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.CaregiverDao;
 import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.utils.AuthorizationManager;



import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.UserDao;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.utils.AuthorizationManager;

/**
 * Controller für die Anmeldung und Autorisierung
 */
public class LoginController {

    private UserDao userDao;
    private AuthorizationManager authManager;

    /**
     * Konstruktor
     */
    public LoginController() {
        try {
            this.userDao = DaoFactory.getDaoFactory().createUserDAO();
            this.authManager = AuthorizationManager.getInstance();

            // Datenbanktabelle initialisieren
            System.out.println("Initialisiere Benutzer-Tabelle...");
            this.userDao.createTable();
            System.out.println("Benutzer-Tabelle erfolgreich initialisiert.");

            // Migration von Caregiver-Daten falls nötig
            System.out.println("Starte Migration alter Daten...");
            migrateOldData();
            System.out.println("Migration abgeschlossen.");
        } catch (Exception e) {
            System.err.println("Fehler bei der Initialisierung des LoginControllers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Migriert alte Daten ins neue Format
     */
    private void migrateOldData() {
        if (this.userDao instanceof de.hitec.nhplus.datastorage.UserDaoImpl) {
            ((de.hitec.nhplus.datastorage.UserDaoImpl)this.userDao).migrateCaregiverToUsers();
        }
    }

    /**
     * Authentifiziert einen Benutzer mit Benutzername und Passwort
     * @param username Benutzername
     * @param password Passwort
     * @return true wenn die Anmeldung erfolgreich war, sonst false
     */
    public boolean login(String username, String password) {
        User user = userDao.authenticate(username, password);

        if (user != null) {
            // Benutzer erfolgreich authentifiziert
            authManager.setCurrentUser(user);
            return true;
        }

        return false;
    }

    /**
     * Meldet den aktuellen Benutzer ab
     */
    public void logout() {
        authManager.logout();
    }

    /**
     * Gibt den aktuell angemeldeten Benutzer zurück
     * @return Der angemeldete Benutzer oder null
     */
    public User getCurrentUser() {
        return authManager.getCurrentUser();
    }

    /**
     * Prüft, ob der aktuelle Benutzer ein Administrator ist
     * @return true wenn der Benutzer ein Administrator ist, sonst false
     */
    public boolean isAdmin() {
        return authManager.isAdmin();
    }

    /**
     * Prüft, ob der aktuelle Benutzer ein Pfleger ist
     * @return true wenn der Benutzer ein Pfleger ist, sonst false
     */
    public boolean isCaregiver() {
        return authManager.isCaregiver();
    }

    /**
     * Prüft, ob der aktuelle Benutzer Pflegerdaten bearbeiten darf
     * @return true wenn der Benutzer Pflegerdaten bearbeiten darf, sonst false
     */
    public boolean canManageCaregivers() {
        return authManager.canManageCaregivers();
    }

    /**
     * Prüft, ob der aktuelle Benutzer Patientendaten bearbeiten darf
     * @return true wenn der Benutzer Patientendaten bearbeiten darf, sonst false
     */
    public boolean canManagePatients() {
        return authManager.canManagePatients();
    }
}
