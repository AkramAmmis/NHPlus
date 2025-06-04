package de.hitec.nhplus.model;

import de.hitec.nhplus.utils.DateConverter;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Patients live in a NURSING home and are treated by nurses.
 */
public class Patient extends Person {
    private SimpleLongProperty pid;
    private final SimpleStringProperty dateOfBirth;
    private final SimpleStringProperty careLevel;
    private final SimpleStringProperty roomNumber;
    private final List<Treatment> allTreatments = new ArrayList<>();
    private RecordStatus status;
    private LocalDate statusChangeDate;


    /**
     * Constructor to initiate an object of class <code>Patient</code> with the given parameter. Use this constructor
     * to initiate objects, which are not persisted yet, because it will not have a patient id (pid).
     *
     * @param firstName First name of the patient.
     * @param surname Last name of the patient.
     * @param dateOfBirth Date of birth of the patient.
     * @param careLevel Care level of the patient.
     * @param roomNumber Room number of the patient.
     */
    public Patient(String firstName, String surname, LocalDate dateOfBirth, String careLevel, String roomNumber) {
        super(firstName, surname);
        this.dateOfBirth = new SimpleStringProperty(DateConverter.convertLocalDateToString(dateOfBirth));
        this.careLevel = new SimpleStringProperty(careLevel);
        this.roomNumber = new SimpleStringProperty(roomNumber);
        this.status = RecordStatus.ACTIVE;
        this.statusChangeDate = LocalDate.now();

    }

    /**
     * Constructor to initiate an object of class <code>Patient</code> with the given parameter. Use this constructor
     * to initiate objects, which are already persisted and have a patient id (pid).
     *
     * @param pid Patient id.
     * @param firstName First name of the patient.
     * @param surname Last name of the patient.
     * @param dateOfBirth Date of birth of the patient.
     * @param careLevel Care level of the patient.
     * @param roomNumber Room number of the patient.
     */
    public Patient(long pid, String firstName, String surname, LocalDate dateOfBirth, String careLevel, String roomNumber) {
        super(firstName, surname);
        this.pid = new SimpleLongProperty(pid);
        this.dateOfBirth = new SimpleStringProperty(DateConverter.convertLocalDateToString(dateOfBirth));
        this.careLevel = new SimpleStringProperty(careLevel);
        this.roomNumber = new SimpleStringProperty(roomNumber);
        this.status = RecordStatus.ACTIVE;
        this.statusChangeDate = LocalDate.now();
    }

    public Patient(long pid, String firstName, String surname, LocalDate dateOfBirth,
                   String careLevel, String roomNumber,
                   RecordStatus status, LocalDate statusChangeDate) {
        super(firstName, surname);
        this.pid = new SimpleLongProperty(pid);
        this.dateOfBirth = new SimpleStringProperty(DateConverter.convertLocalDateToString(dateOfBirth));
        this.careLevel = new SimpleStringProperty(careLevel);
        this.roomNumber = new SimpleStringProperty(roomNumber);
        this.status = status;
        this.statusChangeDate = statusChangeDate;
    }


    public long getPid() {
        return pid.get();
    }

    public SimpleLongProperty pidProperty() {
        return pid;
    }

    public String getDateOfBirth() {
        return dateOfBirth.get();
    }

    public SimpleStringProperty dateOfBirthProperty() {
        return dateOfBirth;
    }

    /**
     * Stores the given string as new <code>birthOfDate</code>.
     *
     * @param dateOfBirth as string in the following format: YYYY-MM-DD.
     */
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth.set(dateOfBirth);
    }

    public String getCareLevel() {
        return careLevel.get();
    }

    public SimpleStringProperty careLevelProperty() {
        return careLevel;
    }

    public void setCareLevel(String careLevel) {
        this.careLevel.set(careLevel);
    }

    public String getRoomNumber() {
        return roomNumber.get();
    }

    public SimpleStringProperty roomNumberProperty() {
        return roomNumber;
    }


    public void setRoomNumber(String roomNumber) {
        this.roomNumber.set(roomNumber);
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
        this.statusChangeDate = LocalDate.now();
    }

    public LocalDate getStatusChangeDate() {
        return statusChangeDate;
    }


    /**
     * Adds a treatment to the list of treatments, if the list does not already contain the treatment.
     *
     * @param treatment Treatment to add.
     * @return False, if the treatment was already part of the list, else true.
     */
    public boolean add(Treatment treatment) {
        if (this.allTreatments.contains(treatment)) {
            return false;
        }
        this.allTreatments.add(treatment);
        return true;
    }

    /**
     * @return The display name of the current status
     */
    public String getStatusDisplayName() {
        return status.getDisplayName();
    }

    @Override
    public String toString() {
        return "Patient{" +
                "pid=" + pid +
                ", firstName='" + getFirstName() + '\'' +
                ", surname='" + getSurname() + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", careLevel='" + careLevel + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", status=" + status +
                ", statusChangeDate=" + statusChangeDate +
                '}';
    }

}