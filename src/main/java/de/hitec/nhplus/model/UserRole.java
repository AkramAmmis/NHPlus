package de.hitec.nhplus.model;

public enum UserRole {
    ADMIN,
    CAREGIVER,
    VISITOR;

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
