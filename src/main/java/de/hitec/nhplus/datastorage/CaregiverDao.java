package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.Caregiver;
import java.sql.SQLException;
import java.util.ArrayList;

public interface CaregiverDao extends Dao<Caregiver> {

    void createTable();

    Caregiver createCaregiver(Caregiver caregiver);

    ArrayList<Caregiver> readAllCaregivers();

    Caregiver readCaregiver(long cid);

    void updateCaregiver(Caregiver caregiver);

    void delete(Caregiver caregiver);

    Caregiver authenticate(String username, String password);
}