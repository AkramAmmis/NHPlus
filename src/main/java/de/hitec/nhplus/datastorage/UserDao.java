package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.User;

import java.util.ArrayList;

public interface UserDao extends Dao<User> {

    void createTable();

    User authenticate(String username, String password);

    User createUser(User user);

    ArrayList<User> readAllUsers();

    User readUser(long uid);

    void updateUser(User user);

    void delete(User user);

    User findByUsername(String username);
}
