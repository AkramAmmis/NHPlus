package de.hitec.nhplus.model;

/**
 * Enum für verschiedene Benutzerrollen im System
 */
public enum UserRole {
    /**
     * Administrator mit vollen Rechten
     */
    ADMIN,

    /**
     * Pfleger mit eingeschränkten Rechten
     */
    CAREGIVER,

    /**
     * Besucher mit minimalen Rechten
     */
    VISITOR;

    /**
     * Konvertiert einen String in eine UserRole
     * @param role String-Repräsentation der Rolle
     * @return Die entsprechende UserRole oder VISITOR als Fallback
     */
    public static UserRole fromString(String role) {
        if (role == null) {
            return VISITOR;
        }

        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return VISITOR;
        }
    }
}
