package de.hitec.nhplus.utils;

import de.hitec.nhplus.model.User;

/**
 * Verwaltet Autorisierungen und Berechtigungen im System
 */
public class AuthorizationManager {

    private static AuthorizationManager instance;
    private User currentUser;

    private AuthorizationManager() {
        // Private Konstruktor für Singleton
    }

    /**
     * Gibt die Singleton-Instanz zurück
     * @return Die AuthorizationManager-Instanz
     */
    public static synchronized AuthorizationManager getInstance() {
        if (instance == null) {
            instance = new AuthorizationManager();
        }
        return instance;
    }

    /**
     * Setzt den aktuell angemeldeten Benutzer
     * @param user Der angemeldete Benutzer
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Gibt den aktuell angemeldeten Benutzer zurück
     * @return Der aktuelle Benutzer
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Prüft, ob der aktuelle Benutzer ein Administrator ist
     * @return true wenn der Benutzer ein Administrator ist, sonst false
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Prüft, ob der aktuelle Benutzer ein Pfleger ist
     * @return true wenn der Benutzer ein Pfleger ist, sonst false
     */
    public boolean isCaregiver() {
        return currentUser != null && currentUser.isCaregiver();
    }

    /**
     * Prüft, ob der aktuelle Benutzer berechtigt ist, Pflegerdaten zu bearbeiten
     * @return true wenn der Benutzer berechtigt ist, sonst false
     */
    public boolean canManageCaregivers() {
        return isAdmin();
    }

    /**
     * Prüft, ob der aktuelle Benutzer berechtigt ist, Patientendaten zu bearbeiten
     * @return true wenn der Benutzer berechtigt ist (Admin oder Pfleger), sonst false
     */
    public boolean canManagePatients() {
        return isAdmin() || isCaregiver();
    }

    /**
     * Prüft, ob ein Benutzer angelegt oder bearbeitet werden darf
     * @return true wenn der aktuelle Benutzer ein Administrator ist, sonst false
     */
    public boolean canCreateOrEditUsers() {
        return isAdmin();
    }

    /**
     * Prüft, ob der Benutzer abgemeldet ist
     * @return true wenn kein Benutzer angemeldet ist, sonst false
     */
    public boolean isLoggedOut() {
        return currentUser == null;
    }

    /**
     * Meldet den aktuellen Benutzer ab
     */
    public void logout() {
        this.currentUser = null;
    }
}
