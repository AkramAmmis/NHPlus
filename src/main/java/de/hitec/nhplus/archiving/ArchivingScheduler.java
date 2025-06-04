package de.hitec.nhplus.archiving;

import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.model.Patient;
import de.hitec.nhplus.model.RecordStatus;
import de.hitec.nhplus.model.Treatment;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduler for automatic archiving and deletion
 */
public class ArchivingScheduler {
    private static final Logger LOGGER = Logger.getLogger(ArchivingScheduler.class.getName());
    private final ArchivingService<Treatment> treatmentArchivingService;
    private final ArchivingService<Caregiver> caregiverArchivingService;
    private final ArchivingService<Patient> patientArchivingService;

    private Timer timer;

    public ArchivingScheduler() {
        this.treatmentArchivingService = new TreatmentArchivingService();
        this.caregiverArchivingService = new CaregiverArchivingService();
        this.patientArchivingService = new PatientArchivingService();

        this.timer = new Timer(true);
    }

    /**
     * Starts the scheduler for daily checks.
     */
    public void startScheduler() {
        // Run daily at midnight
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runArchivingTasks();
            }
        }, 0, 24 * 60 * 60 * 1000); // Once per day
    }

    /**
     * Stops the scheduler.
     */
    public void stopScheduler() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Executes the archiving tasks.
     */
    public void runArchivingTasks() {
        LOGGER.log(Level.INFO, "Starting archiving process...");

        // 1. Behandlungen archivieren
        processTreatments();

        // 2. Pfleger archivieren
        processCaregivers();

        processPatients();


        LOGGER.log(Level.INFO, "Archiving process completed");
    }

    private void processTreatments() {
        // Find records that are older than 10 years
        List<Treatment> oldTreatments = treatmentArchivingService.findRecordsOlderThan(10);
        LOGGER.log(Level.INFO, "Found: {0} treatments that are older than 10 years", oldTreatments.size());

        // For each old record, check if it is already locked
        for (Treatment treatment : oldTreatments) {
            if (treatment.getStatus() == RecordStatus.ACTIVE) {
                // If active, lock it
                boolean locked = treatmentArchivingService.lockRecord(treatment.getTid());
                if (locked) {
                    LOGGER.log(Level.INFO, "Treatment with ID {0} was automatically locked", treatment.getTid());
                }
            } else if (treatment.getStatus() == RecordStatus.LOCKED) {
                // If already locked, do nothing
                LOGGER.log(Level.FINE, "Treatment with ID {0} is already locked", treatment.getTid());
            }
        }
    }

    private void processCaregivers() {
        // Find caregivers that are older than 10 years
        List<Caregiver> oldCaregivers = caregiverArchivingService.findRecordsOlderThan(10);
        LOGGER.log(Level.INFO, "Found: {0} caregivers that are older than 10 years", oldCaregivers.size());

        // For each old caregiver, check if it is already locked
        for (Caregiver caregiver : oldCaregivers) {
            if (caregiver.getStatus() == RecordStatus.ACTIVE) {
                // If active, lock it
                boolean locked = caregiverArchivingService.lockRecord(caregiver.getCid());
                if (locked) {
                    LOGGER.log(Level.INFO, "Caregiver with ID {0} was automatically locked", caregiver.getCid());
                }
            } else if (caregiver.getStatus() == RecordStatus.LOCKED) {
                // If already locked, do nothing
                LOGGER.log(Level.FINE, "Caregiver with ID {0} is already locked", caregiver.getCid());
            }
        }
    }

    private void processPatients() {
        // Patienten finden, die älter als 10 Jahre sind
        List<Patient> oldPatients = patientArchivingService.findRecordsOlderThan(10);
        LOGGER.log(Level.INFO, "Gefunden: {0} Patienten, die älter als 10 Jahre sind", oldPatients.size());

        // Für jeden alten Patienten prüfen, ob er bereits gesperrt ist
        for (Patient patient : oldPatients) {
            if (patient.getStatus() == RecordStatus.ACTIVE) {
                // Falls aktiv, sperren
                boolean locked = patientArchivingService.lockRecord(patient.getPid());
                if (locked) {
                    LOGGER.log(Level.INFO, "Patient mit ID {0} wurde automatisch gesperrt", patient.getPid());
                }
            } else if (patient.getStatus() == RecordStatus.LOCKED) {
                // Falls bereits gesperrt, nichts tun
                LOGGER.log(Level.FINE, "Patient mit ID {0} ist bereits gesperrt", patient.getPid());
            }
        }

    }
}