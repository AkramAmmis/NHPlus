package de.hitec.nhplus.datastorage;

public class DaoFactory {

    private static DaoFactory instance;

    private DaoFactory() {
    }

    public static synchronized DaoFactory getDaoFactory() {
        if (DaoFactory.instance == null) {
            DaoFactory.instance = new DaoFactory();
        }
        return DaoFactory.instance;
    }

    public TreatmentDao createTreatmentDao() {
        return new TreatmentDao(ConnectionBuilder.getConnection());
    }

    public PatientDao createPatientDAO() {
        return new PatientDao(ConnectionBuilder.getConnection());
    }

    public CaregiverDao createCaregiverDAO() {
        return new CaregiverDaoImpl(ConnectionBuilder.getConnection());
    }

    public UserDao createUserDAO() {
        return new UserDaoImpl(ConnectionBuilder.getConnection());
    }
}