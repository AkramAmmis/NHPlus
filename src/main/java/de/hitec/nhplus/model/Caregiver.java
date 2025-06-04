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
    private RecordStatus status;
    private LocalDate statusChangeDate;


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
        this.status = RecordStatus.ACTIVE;
        this.statusChangeDate = LocalDate.now();

    }

    /**
     * Constructor for creating a caregiver with an ID (e.g., when retrieving from the database).
     * @param cid ID of the caregiver.
     * @param firstName First name of the caregiver.
     * @param surname Surname of the caregiver.
     * @param telephone Telephone number of the caregiver
     */
    public Caregiver(long cid, String firstName, String surname, String telephone) {
        this.cid = cid;
        this.firstName = firstName;
        this.surname = surname;
        this.telephone = telephone;
        this.status = RecordStatus.ACTIVE;
        this.statusChangeDate = LocalDate.now();
    }

    /**
     * Constructor for creating a caregiver with an ID and status information.
     * @param cid ID of the caregiver.
     * @param firstName First name of the caregiver.
     * @param surname Surname of the caregiver.
     * @param telephone Telephone number of the caregiver.
     * @param status Status of the record.
     * @param statusChangeDate Date when the status was last changed.
     */
    public Caregiver(long cid, String firstName, String surname, String telephone,
                     RecordStatus status, LocalDate statusChangeDate) {
        this.cid = cid;
        this.firstName = firstName;
        this.surname = surname;
        this.telephone = telephone;
        this.status = status;
        this.statusChangeDate = statusChangeDate;
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

    public RecordStatus getStatus() {
        return status;
    }

    /**
     * Sets a new status for the caregiver record and updates the status change date
     *
     * @param status The new status to set
     */
    public void setStatus(RecordStatus status) {
        this.status = status;
        this.statusChangeDate = LocalDate.now();
    }

    public LocalDate getStatusChangeDate() {
        return statusChangeDate;
    }

    /**
     * @return The display name of the current status
     */
    public String getStatusDisplayName() {
        return status.getDisplayName();
    }


    @Override
    public String toString() {
        return "Caregiver{" +
                "cid=" + cid +
                ", firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                ", telephone='" + telephone + '\'' +
                ", status=" + status +
                ", statusChangeDate=" + statusChangeDate +
                '}';
    }
}