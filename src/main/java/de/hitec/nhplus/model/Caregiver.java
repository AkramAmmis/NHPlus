package de.hitec.nhplus.model;

import java.time.LocalDate;

/**
 * Represents a caregiver.
 */
public class Caregiver {
    private long cid;
    private String firstName;
    private String surname;
    private String telephone;


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

    public long getCid() {
        return cid;
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


    @Override
    public String toString() {
        return "Caregiver{" +
               "cid=" + cid +
               ", firstName='" + firstName + '\'' +
               ", surname='" + surname + '\'' +
               ", telephone='" + telephone + '\'' +
               '}';
    }
}