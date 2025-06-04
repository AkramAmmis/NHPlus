package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.CaregiverDao;
 import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.utils.AuthorizationManager;



import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.UserDao;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.utils.AuthorizationManager;

public class LoginController {

    private UserDao userDao;
    private AuthorizationManager authManager;

    public LoginController() {
        try {
            this.userDao = DaoFactory.getDaoFactory().createUserDAO();
            this.authManager = AuthorizationManager.getInstance();

            System.out.println("Initialisiere Benutzer-Tabelle...");
            this.userDao.createTable();
            System.out.println("Benutzer-Tabelle erfolgreich initialisiert.");

            System.out.println("Starte Migration alter Daten...");
            migrateOldData();
            System.out.println("Migration abgeschlossen.");
        } catch (Exception e) {
            System.err.println("Fehler bei der Initialisierung des LoginControllers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void migrateOldData() {
        if (this.userDao instanceof de.hitec.nhplus.datastorage.UserDaoImpl) {
            ((de.hitec.nhplus.datastorage.UserDaoImpl)this.userDao).migrateCaregiverToUsers();
        }
    }

    public boolean login(String username, String password) {
        User user = userDao.authenticate(username, password);

        if (user != null) {
            authManager.setCurrentUser(user);
            return true;
        }

        return false;
    }

    public void logout() {
        authManager.logout();
    }

    public User getCurrentUser() {
        return authManager.getCurrentUser();
    }

    public boolean isAdmin() {
        return authManager.isAdmin();
    }

    public boolean isCaregiver() {
        return authManager.isCaregiver();
    }

    public boolean canManageCaregivers() {
        return authManager.canManageCaregivers();
    }

    public boolean canManagePatients() {
        return authManager.canManagePatients();
    }
}
