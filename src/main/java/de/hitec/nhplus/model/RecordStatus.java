package de.hitec.nhplus.model;

/**
 * Represents the status of a record in the system.
 */
public enum RecordStatus {
    ACTIVE("Active"),
    LOCKED("Locked"),
    DELETED("Deleted");

    private final String displayName;

    RecordStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
