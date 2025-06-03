package de.hitec.nhplus.archiving;

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
    private Timer timer;

    public ArchivingScheduler() {
        this.treatmentArchivingService = new TreatmentArchivingService();
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

        // 1. Find records that are older than 10 years
        List<Treatment> oldTreatments = treatmentArchivingService.findRecordsOlderThan(10);
        LOGGER.log(Level.INFO, "Found: {0} treatments that are older than 10 years", oldTreatments.size());

        // 2. For each old record, check if it is already locked
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

        LOGGER.log(Level.INFO, "Archiving process completed");
    }
}
