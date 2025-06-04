package de.hitec.nhplus.archiving;

import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.PatientDao;
import de.hitec.nhplus.model.Patient;
import de.hitec.nhplus.model.RecordStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ArchivingService for Patients
 */
public class PatientArchivingService implements ArchivingService<Patient> {

    private static final Logger LOGGER = Logger.getLogger(PatientArchivingService.class.getName());
    private final PatientDao dao;

    public PatientArchivingService() {
        this.dao = DaoFactory.getDaoFactory().createPatientDAO();
    }

    @Override
    public boolean lockRecord(long id) {
        try {
            Patient patient = dao.read(id);
            if (patient == null) {
                LOGGER.log(Level.WARNING, "Patient with ID {0} not found", id);
                return false;
            }

            // Prüfen, ob der Datensatz bereits gesperrt oder gelöscht ist
            if (patient.getStatus() != RecordStatus.ACTIVE) {
                LOGGER.log(Level.WARNING, "Patient with ID {0} is already {1}",
                        new Object[]{id, patient.getStatus()});
                return false;
            }

            RecordStatus oldStatus = patient.getStatus();
            patient.setStatus(RecordStatus.LOCKED);
            dao.update(patient);

            // Status-Änderung protokollieren
            logStatusChange(id, oldStatus, RecordStatus.LOCKED, LocalDate.now(), "System");

            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error locking patient with ID " + id, e);
            return false;
        }
    }

    @Override
    public boolean deleteRecord(long id) {
        try {
            Patient patient = dao.read(id);
            if (patient == null) {
                LOGGER.log(Level.WARNING, "Patient with ID {0} not found", id);
                return false;
            }

            // Prüfen, ob der Datensatz gesperrt ist - gesperrte Datensätze können nicht gelöscht werden
            if (patient.getStatus() == RecordStatus.LOCKED) {
                LOGGER.log(Level.WARNING, "Patient with ID {0} is locked and cannot be deleted", id);
                return false;
            }

            // Prüfen, ob der Datensatz bereits gelöscht ist
            if (patient.getStatus() == RecordStatus.DELETED) {
                LOGGER.log(Level.WARNING, "Patient with ID {0} is already deleted", id);
                return false;
            }

            // Prüfen, ob der Datensatz älter als 10 Jahre ist
            // Für Patienten verwenden wir das Geburtsdatum als Ausgangsdatum
            LocalDate birthDate = LocalDate.parse(patient.getDateOfBirth());
            if (birthDate.plusYears(10).isAfter(LocalDate.now())) {
                LOGGER.log(Level.WARNING, "Patient with ID {0} is not yet 10 years old and cannot be deleted", id);
                return false;
            }

            RecordStatus oldStatus = patient.getStatus();
            patient.setStatus(RecordStatus.DELETED);
            dao.update(patient);

            // Status-Änderung protokollieren
            logStatusChange(id, oldStatus, RecordStatus.DELETED, LocalDate.now(), "System");

            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting patient with ID " + id, e);
            return false;
        }
    }

    @Override
    public List<Patient> findRecordsOlderThan(int years) {
        List<Patient> result = new ArrayList<>();
        try {
            List<Patient> allPatients = dao.readAll();
            LocalDate cutoffDate = LocalDate.now().minusYears(years);

            for (Patient patient : allPatients) {
                LocalDate birthDate = LocalDate.parse(patient.getDateOfBirth());
                if (birthDate.isBefore(cutoffDate)) {
                    result.add(patient);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching for old patients", e);
        }
        return result;
    }

    @Override
    public List<Patient> findRecordsByStatus(RecordStatus status) {
        List<Patient> result = new ArrayList<>();
        try {
            List<Patient> allPatients = dao.readAll();

            for (Patient patient : allPatients) {
                if (patient.getStatus() == status) {
                    result.add(patient);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching for patients with status " + status, e);
        }
        return result;
    }

    @Override
    public void logStatusChange(long id, RecordStatus oldStatus, RecordStatus newStatus,
                                LocalDate date, String user) {
        // Verwenden Sie den ArchivingLogger, um die Statusänderung zu protokollieren
        ArchivingLogger.logStatusChange("Patient", id, oldStatus, newStatus, user);
    }
}
