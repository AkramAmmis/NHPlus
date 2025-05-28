package de.hitec.nhplus.model;

/**
 * Represents a caregiver.
 */
public class Caregiver {
    private long cid;
    private String firstName;
    private String surname;
    private String telephone;
    private String username;
    private String password; // In einer echten Anwendung sollte das Passwort gehasht gespeichert werden

    /**
     * Constructor for creating a new caregiver without an ID (e.g., before saving to the database).
     * @param firstName First name of the caregiver.
     * @param surname Surname of the caregiver.
     * @param telephone Telephone number of the caregiver.
     */
    public Caregiver(String firstName, String surname, String telephone) {
        this.firstName = firstName;
        this.surname = surname;
        this.telephone = telephone;
    }

    /**
     * Constructor for creating a caregiver with an ID (e.g., when retrieving from the database).
     * @param cid ID of the caregiver.
     * @param firstName First name of the caregiver.
     * @param surname Surname of the caregiver.
     * @param telephone Telephone number of the caregiver.
     */
    public Caregiver(long cid, String firstName, String surname, String telephone) {
        this.cid = cid;
        this.firstName = firstName;
        this.surname = surname;
        this.telephone = telephone;
    }

    /**
     * Konstruktor für Caregiver mit Login-Daten
     * @param cid eindeutige ID
     * @param username Benutzername für Login
     * @param password Passwort für Login
     * @param firstName Vorname des Pflegers
     * @param surname Nachname des Pflegers
     * @param telephone Telefonnummer des Pflegers
     */
    public Caregiver(long cid, String username, String password, String firstName, String surname, String telephone) {
        this.cid = cid;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.surname = surname;
        this.telephone = telephone;
    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gibt vollständigen Namen zurück
     * @return Vor- und Nachname kombiniert
     */
    public String getFullName() {
        return firstName + " " + surname;
    }

    /**
     * Für Kompatibilität mit dem bestehenden Code
     * @return Nachname
     */
    public String getLastName() {
        return surname;
    }

    /**
     * Für Kompatibilität mit dem bestehenden Code
     * @param lastName Nachname
     */
    public void setLastName(String lastName) {
        this.surname = lastName;
    }

    /**
     * Für Kompatibilität mit dem bestehenden Code
     * @return Telefonnummer
     */
    public String getPhoneNumber() {
        return telephone;
    }

    /**
     * Für Kompatibilität mit dem bestehenden Code
     * @param phoneNumber Telefonnummer
     */
    public void setPhoneNumber(String phoneNumber) {
        this.telephone = phoneNumber;
    }

    @Override
    public String toString() {
        return "Caregiver{" +
               "cid=" + cid +
               ", firstName='" + firstName + '\'' +
               ", surname='" + surname + '\'' +
               ", telephone='" + telephone + '\'' +
               ", username='" + username + '\'' +
                ", username='" + this.username + '\'' +
               '}';
    }
}