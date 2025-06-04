package de.hitec.nhplus.archiving;

import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.model.RecordStatus;
import de.hitec.nhplus.model.Caregiver;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of ArchivingService for Caregivers
 */
public class CaregiverArchivingService implements ArchivingService<Caregiver> {

    private static final Logger LOGGER = Logger.getLogger(CaregiverArchivingService.class.getName());
    private final CaregiverDao dao;

    public CaregiverArchivingService() {
        this.dao = DaoFactory.getDaoFactory().createCaregiverDAO();
    }

    @Override
    public boolean lockRecord(long id) {
        try {
            Caregiver caregiver = dao.read(id);
            if (caregiver == null) {
                LOGGER.log(Level.WARNING, "Caregiver with ID {0} not found", id);
                return false;
            }

            // Check if the record is already locked or deleted
            if (caregiver.getStatus() != RecordStatus.ACTIVE) {
                LOGGER.log(Level.WARNING, "Caregiver with ID {0} is already {1}",
                        new Object[]{id, caregiver.getStatus()});
                return false;
            }

            RecordStatus oldStatus = caregiver.getStatus();
            caregiver.setStatus(RecordStatus.LOCKED);
            dao.update(caregiver);

            // Log status change
            logStatusChange(id, oldStatus, RecordStatus.LOCKED, LocalDate.now(), "System");

            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error locking caregiver with ID " + id, e);
            return false;
        }
    }

    @Override
    public boolean deleteRecord(long id) {
        try {
            Caregiver caregiver = dao.read(id);
            if (caregiver == null) {
                LOGGER.log(Level.WARNING, "Caregiver with ID {0} not found", id);
                return false;
            }

            // Check if the record is locked - locked records cannot be deleted
            if (caregiver.getStatus() == RecordStatus.LOCKED) {
                LOGGER.log(Level.WARNING, "Caregiver with ID {0} is locked and cannot be deleted", id);
                return false;
            }

            // Check if the record is already deleted
            if (caregiver.getStatus() == RecordStatus.DELETED) {
                LOGGER.log(Level.WARNING, "Caregiver with ID {0} is already deleted", id);
                return false;
            }

            // Check if the record is older than 10 years
            // For caregivers, we might use a different criterion like creation date or last activity
            // For now, let's use the status change date as a proxy
            if (caregiver.getStatusChangeDate().plusYears(10).isAfter(LocalDate.now())) {
                LOGGER.log(Level.WARNING, "Caregiver with ID {0} is not yet 10 years old and cannot be deleted", id);
                return false;
            }

            RecordStatus oldStatus = caregiver.getStatus();
            caregiver.setStatus(RecordStatus.DELETED);
            dao.update(caregiver);

            // Log status change
            logStatusChange(id, oldStatus, RecordStatus.DELETED, LocalDate.now(), "System");

            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting caregiver with ID " + id, e);
            return false;
        }
    }

    @Override
    public List<Caregiver> findRecordsOlderThan(int years) {
        List<Caregiver> result = new ArrayList<>();
        try {
            List<Caregiver> allCaregivers = dao.readAll();
            LocalDate cutoffDate = LocalDate.now().minusYears(years);

            for (Caregiver caregiver : allCaregivers) {
                // For caregivers, we might need a different criterion than treatment date
                // For now, let's use the status change date as a proxy
                if (caregiver.getStatusChangeDate().isBefore(cutoffDate)) {
                    result.add(caregiver);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching for old caregivers", e);
        }
        return result;
    }

    @Override
    public List<Caregiver> findRecordsByStatus(RecordStatus status) {
        List<Caregiver> result = new ArrayList<>();
        try {
            List<Caregiver> allCaregivers = dao.readAll();

            for (Caregiver caregiver : allCaregivers) {
                if (caregiver.getStatus() == status) {
                    result.add(caregiver);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching for caregivers with status " + status, e);
        }
        return result;
    }

    @Override
    public void logStatusChange(long id, RecordStatus oldStatus, RecordStatus newStatus,
                                LocalDate date, String user) {
        // Use the ArchivingLogger to log the status change
        ArchivingLogger.logStatusChange("Caregiver", id, oldStatus, newStatus, user);
    }
}
