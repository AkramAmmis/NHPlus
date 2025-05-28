package de.hitec.nhplus.model;

/**
 * Klasse zur Repräsentation eines Benutzers im System
 */
public class User {
    private long uid;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private long caregiverId;

    /**
     * Konstruktor für einen neuen Benutzer ohne ID
     * @param username Benutzername
     * @param password Passwort (unverschlüsselt)
     * @param firstName Vorname
     * @param lastName Nachname
     * @param email E-Mail-Adresse
     * @param phoneNumber Telefonnummer
     * @param role Benutzerrolle
     */
    public User(String username, String password, String firstName, String lastName, 
                String email, String phoneNumber, UserRole role) {
        this.uid = -1; // Negative ID für noch nicht gespeicherte Objekte
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.caregiverId = 0; // Standardwert: keine Verknüpfung
    }

    /**
     * Konstruktor für einen Benutzer mit ID (z.B. aus der Datenbank geladen)
     * @param uid Benutzer-ID
     * @param username Benutzername
     * @param password Passwort (verschlüsselt)
     * @param firstName Vorname
     * @param lastName Nachname
     * @param email E-Mail-Adresse
     * @param phoneNumber Telefonnummer
     * @param role Benutzerrolle
     */
    public User(long uid, String username, String password, String firstName, String lastName, 
                String email, String phoneNumber, UserRole role) {
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.caregiverId = 0; // Standardwert: keine Verknüpfung
    }

    /**
     * Gibt die Benutzer-ID zurück
     * @return Benutzer-ID
     */
    public long getUid() {
        return uid;
    }

    /**
     * Konstruktor mit allen Parametern inklusive caregiverId
     */
    public User(long uid, String username, String password, String firstName, String lastName, String email, String phoneNumber, UserRole role, long caregiverId) {
        this(uid, username, password, firstName, lastName, email, phoneNumber, role);
        this.caregiverId = caregiverId;
    }

    /**
     * Gibt die ID des verknüpften Caregivers zurück
     * @return ID des Caregivers oder 0, wenn nicht verknüpft
     */
    public long getCaregiverId() {
        return caregiverId;
    }

    /**
     * Setzt die ID des verknüpften Caregivers
     * @param caregiverId Die ID des Caregivers
     */
    public void setCaregiverId(long caregiverId) {
        if (caregiverId < 0) {
            System.out.println("Warnung: Negative Caregiver-ID (" + caregiverId + "). Setze auf 0.");
            this.caregiverId = 0;
        } else {
            this.caregiverId = caregiverId;
        }
    }

    /**
     * Setzt die Benutzer-ID
     * @param uid Neue Benutzer-ID
     */
    public void setUid(long uid) {
        this.uid = uid;
    }

    /**
     * Gibt den Benutzernamen zurück
     * @return Benutzername
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setzt den Benutzernamen
     * @param username Neuer Benutzername
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gibt das Passwort zurück
     * @return Passwort (verschlüsselt)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setzt das Passwort
     * @param password Neues Passwort
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gibt den Vornamen zurück
     * @return Vorname
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Setzt den Vornamen
     * @param firstName Neuer Vorname
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gibt den Nachnamen zurück
     * @return Nachname
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Setzt den Nachnamen
     * @param lastName Neuer Nachname
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gibt die E-Mail-Adresse zurück
     * @return E-Mail-Adresse
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setzt die E-Mail-Adresse
     * @param email Neue E-Mail-Adresse
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gibt die Telefonnummer zurück
     * @return Telefonnummer
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Setzt die Telefonnummer
     * @param phoneNumber Neue Telefonnummer
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Gibt die Benutzerrolle zurück
     * @return Benutzerrolle
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Setzt die Benutzerrolle
     * @param role Neue Benutzerrolle
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Gibt den vollständigen Namen zurück
     * @return Vollständiger Name (Vorname + Nachname)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Prüft, ob der Benutzer Administrator ist
     * @return true wenn Administrator, sonst false
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * Prüft, ob der Benutzer ein Pfleger ist
     * @return true wenn Pfleger, sonst false
     */
    public boolean isCaregiver() {
        return role == UserRole.CAREGIVER;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role +
                ", caregiverId=" + caregiverId +
                '}';
    }
}
