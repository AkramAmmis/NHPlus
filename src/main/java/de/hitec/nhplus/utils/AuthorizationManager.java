package de.hitec.nhplus.utils;

import de.hitec.nhplus.model.User;


public class AuthorizationManager {

    private static AuthorizationManager instance;
    private User currentUser;

    private AuthorizationManager() {
    }

    public static synchronized AuthorizationManager getInstance() {
        if (instance == null) {
            instance = new AuthorizationManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public boolean isCaregiver() {
        return currentUser != null && currentUser.isCaregiver();
    }

    public boolean canManageCaregivers() {
        return isAdmin();
    }

    public boolean canManagePatients() {
        return isAdmin() || isCaregiver();
    }

    public boolean canCreateOrEditUsers() {
        return isAdmin();
    }

    public boolean isLoggedOut() {
        return currentUser == null;
    }

    public void logout() {
        this.currentUser = null;
    }
}
