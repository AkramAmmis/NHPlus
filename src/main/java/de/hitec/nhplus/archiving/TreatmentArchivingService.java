package de.hitec.nhplus.archiving;

import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.TreatmentDao;
import de.hitec.nhplus.model.RecordStatus;
import de.hitec.nhplus.model.Treatment;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ArchivingService for Treatments
 */
public class TreatmentArchivingService implements ArchivingService<Treatment> {

    private static final Logger LOGGER = Logger.getLogger(TreatmentArchivingService.class.getName());
    private final TreatmentDao dao;

    public TreatmentArchivingService() {
        this.dao = DaoFactory.getDaoFactory().createTreatmentDao();
    }

    @Override
    public boolean lockRecord(long id) {
        try {
            Treatment treatment = dao.read(id);
            if (treatment == null) {
                LOGGER.log(Level.WARNING, "Treatment with ID {0} not found", id);
                return false;
            }

            // Check if the record is already locked or deleted
            if (treatment.getStatus() != RecordStatus.ACTIVE) {
                LOGGER.log(Level.WARNING, "Treatment with ID {0} is already {1}",
                        new Object[]{id, treatment.getStatus()});
                return false;
            }

            RecordStatus oldStatus = treatment.getStatus();
            treatment.setStatus(RecordStatus.LOCKED);
            dao.update(treatment);

            // Log status change
            logStatusChange(id, oldStatus, RecordStatus.LOCKED, LocalDate.now(), "System");

            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error locking treatment with ID " + id, e);
            return false;
        }
    }

    @Override
    public boolean deleteRecord(long id) {
        try {
            Treatment treatment = dao.read(id);
            if (treatment == null) {
                LOGGER.log(Level.WARNING, "Treatment with ID {0} not found", id);
                return false;
            }

            // Check if the record is locked - locked records cannot be deleted
            if (treatment.getStatus() == RecordStatus.LOCKED) {
                LOGGER.log(Level.WARNING, "Treatment with ID {0} is locked and cannot be deleted", id);
                return false;
            }

            // Check if the record is already deleted
            if (treatment.getStatus() == RecordStatus.DELETED) {
                LOGGER.log(Level.WARNING, "Treatment with ID {0} is already deleted", id);
                return false;
            }

            // Check if the record is older than 10 years
            LocalDate treatmentDate = LocalDate.parse(treatment.getDate());
            if (treatmentDate.plusYears(10).isAfter(LocalDate.now())) {
                LOGGER.log(Level.WARNING, "Treatment with ID {0} is not yet 10 years old and cannot be deleted", id);
                return false;
            }

            RecordStatus oldStatus = treatment.getStatus();
            treatment.setStatus(RecordStatus.DELETED);
            dao.update(treatment);

            // Log status change
            logStatusChange(id, oldStatus, RecordStatus.DELETED, LocalDate.now(), "System");

            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting treatment with ID " + id, e);
            return false;
        }
    }

    @Override
    public List<Treatment> findRecordsOlderThan(int years) {
        List<Treatment> result = new ArrayList<>();
        try {
            List<Treatment> allTreatments = dao.readAll();
            LocalDate cutoffDate = LocalDate.now().minusYears(years);

            for (Treatment treatment : allTreatments) {
                LocalDate treatmentDate = LocalDate.parse(treatment.getDate());
                if (treatmentDate.isBefore(cutoffDate)) {
                    result.add(treatment);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching for old treatments", e);
        }
        return result;
    }

    @Override
    public List<Treatment> findRecordsByStatus(RecordStatus status) {
        List<Treatment> result = new ArrayList<>();
        try {
            List<Treatment> allTreatments = dao.readAll();

            for (Treatment treatment : allTreatments) {
                if (treatment.getStatus() == status) {
                    result.add(treatment);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching for treatments with status " + status, e);
        }
        return result;
    }

    @Override
    public void logStatusChange(long id, RecordStatus oldStatus, RecordStatus newStatus,
                                LocalDate date, String user) {
        // In a real application, logging would be done in a database
        LOGGER.log(Level.INFO, "Status for Treatment {0} changed from {1} to {2} by {3} on {4}",
                new Object[]{id, oldStatus, newStatus, user, date});
    }
}

