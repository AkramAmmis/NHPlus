package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.UserDao;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.model.UserRole;
import de.hitec.nhplus.utils.AuthorizationManager;

import java.util.ArrayList;

public class UserManagementController {
    private UserDao userDao;

    public UserManagementController() {
        this.userDao = DaoFactory.getDaoFactory().createUserDAO();
    }

    public ArrayList<User> getAllUsers() {
        return userDao.readAllUsers();
    }

    public User createUser(String username, String password, String firstName, String lastName,
                           String email, String phoneNumber, UserRole role) {
        if (!AuthorizationManager.getInstance().isAdmin()) {
            return null;
        }
        User user = new User(username, password, firstName, lastName, email, phoneNumber, role);
        return userDao.createUser(user);
    }

    public boolean updateUser(User user) {
        if (!AuthorizationManager.getInstance().isAdmin()) {
            return false;
        }
        userDao.updateUser(user);
        return true;
    }

    public boolean deleteUser(User user) {
        if (!AuthorizationManager.getInstance().isAdmin()) {
            return false;
        }
        if (user.isAdmin() && "admin".equals(user.getUsername())) {
            return false;
        }
        userDao.delete(user);
        return true;
    }

    public boolean isUsernameAlreadyTaken(String username) {
        ArrayList<User> users = userDao.readAllUsers();
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public User getUserById(long uid) {
        return userDao.readUser(uid);
    }
}
