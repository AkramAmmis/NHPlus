package de.hitec.nhplus.model;

import de.hitec.nhplus.datastorage.PatientDao;
import de.hitec.nhplus.utils.DateConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import de.hitec.nhplus.datastorage.DaoFactory;

/**
 * Represents a treatment of a patient by a caregiver.
 */
public class Treatment {
    private long tid;
    private final long pid;
    private long cid;
    private LocalDate date;
    private LocalTime begin;
    private LocalTime end;
    private String description;
    private String remarks;
    private RecordStatus status;           // Status des Datensatzes
    private LocalDate statusChangeDate;    // Datum der letzten Statusänderung


    /**
     * Constructor for new treatments that are not yet persisted.
     *
     * @param pid         ID of the treated patient
     * @param cid         ID of the caregiver
     * @param date        Date of the treatment
     * @param begin       Start time of the treatment
     * @param end         End time of the treatment
     * @param description Description of the treatment
     * @param remarks     Additional remarks about the treatment
     */
    public Treatment(long pid, long cid, LocalDate date, LocalTime begin, LocalTime end, String description, String remarks) {
        this.pid = pid;
        this.cid = cid;
        this.date = date;
        this.begin = begin;
        this.end = end;
        this.description = description;
        this.remarks = remarks;
        this.status = RecordStatus.ACTIVE;  // Standardstatus ist ACTIVE
        this.statusChangeDate = LocalDate.now();
    }


    /**
     * Constructor for already persisted treatments (basic version).
     *
     * @param tid         ID of the treatment
     * @param pid         ID of the treated patient
     * @param cid         ID of the caregiver
     * @param date        Date of the treatment
     * @param begin       Start time of the treatment
     * @param end         End time of the treatment
     * @param description Description of the treatment
     * @param remarks     Additional remarks about the treatment
     */
    public Treatment(long tid, long pid, long cid, LocalDate date, LocalTime begin, LocalTime end, String description, String remarks) {
        this.tid = tid;
        this.pid = pid;
        this.cid = cid;
        this.date = date;
        this.begin = begin;
        this.end = end;
        this.description = description;
        this.remarks = remarks;
        this.status = RecordStatus.ACTIVE;  // Standardstatus ist ACTIVE
        this.statusChangeDate = LocalDate.now();
    }

    /**
     * Constructor for already persisted treatments with status information.
     *
     * @param tid              ID of the treatment
     * @param pid              ID of the treated patient
     * @param cid              ID of the caregiver
     * @param date             Date of the treatment
     * @param begin            Start time of the treatment
     * @param end              End time of the treatment
     * @param description      Description of the treatment
     * @param remarks          Additional remarks about the treatment
     * @param status           Status of the treatment record
     * @param statusChangeDate Date when the status was last changed
     */
    public Treatment(long tid, long pid, long cid, LocalDate date, LocalTime begin,
                     LocalTime end, String description, String remarks,
                     RecordStatus status, LocalDate statusChangeDate) {
        this.tid = tid;
        this.pid = pid;
        this.cid = cid;
        this.date = date;
        this.begin = begin;
        this.end = end;
        this.description = description;
        this.remarks = remarks;
        this.status = status;
        this.statusChangeDate = statusChangeDate;
    }

    public long getTid() {
        return tid;
    }

    public long getPid() {
        return this.pid;
    }

    public long getCid() {
        return cid;
    }

    public String getDate() {
        return date.toString();
    }

    public String getBegin() {
        return begin.toString();
    }

    public String getCareLevel() {
        PatientDao pDao = DaoFactory.getDaoFactory().createPatientDAO();
        try {
            return pDao.read(this.pid).getCareLevel();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public String getEnd() {
        return end.toString();
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public void setDate(String date) {
        this.date = DateConverter.convertStringToLocalDate(date);
    }

    public void setBegin(String begin) {
        this.begin = DateConverter.convertStringToLocalTime(begin);
    }

    public void setEnd(String end) {
        this.end = DateConverter.convertStringToLocalTime(end);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * @return The current status of the treatment record
     */
    public RecordStatus getStatus() {
        return status;
    }

    /**
     * Sets a new status for the treatment record and updates the status change date
     *
     * @param status The new status to set
     */
    public void setStatus(RecordStatus status) {
        this.status = status;
        this.statusChangeDate = LocalDate.now();
    }

    /**
     * @return The date when the status was last changed
     */
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
        return "\nTreatment" + "\nTID: " + this.tid +
                "\nPID: " + this.pid +
                "\nCID: " + this.cid +
                "\nDate: " + this.date +
                "\nBegin: " + this.begin +
                "\nEnd: " + this.end +
                "\nDescription: " + this.description +
                "\nRemarks: " + this.remarks +
                "\nStatus: " + this.status +
                "\nStatus Change Date: " + this.statusChangeDate + "\n";
    }
}