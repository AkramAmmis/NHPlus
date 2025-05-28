package de.hitec.nhplus.datastorage;

// Ggf. Import für ConnectionBuilder hinzufügen, falls nicht im selben Paket
// import de.hitec.nhplus.utils.ConnectionBuilder; // Beispielhafter Pfad

public class DaoFactory {

    private static DaoFactory instance;

    // Privater Konstruktor, um direkte Instanziierung zu verhindern
    private DaoFactory() {
    }

    /**
     * Gibt die Singleton-Instanz der DaoFactory zurück.
     * @return Die Singleton DaoFactory Instanz.
     */
    public static synchronized DaoFactory getDaoFactory() {
        if (DaoFactory.instance == null) {
            DaoFactory.instance = new DaoFactory();
        }
        return DaoFactory.instance;
    }

    /**
     * Erstellt und gibt eine TreatmentDao Instanz zurück.
     * @return Eine TreatmentDao Instanz.
     */
    public TreatmentDao createTreatmentDao() {
        return new TreatmentDao(ConnectionBuilder.getConnection());
    }

    /**
     * Erstellt und gibt eine PatientDao Instanz zurück.
     * @return Eine PatientDao Instanz.
     */
    public PatientDao createPatientDAO() {
        return new PatientDao(ConnectionBuilder.getConnection());
    }

    /**
     * Erstellt und gibt eine CaregiverDao Instanz zurück.
     * @return Eine CaregiverDao Instanz.
     */
    public CaregiverDao createCaregiverDAO() {
        // Annahme: CaregiverDaoImpl existiert und akzeptiert eine Connection
        // Sie müssen CaregiverDaoImpl.java erstellen, ähnlich wie PatientDaoImpl.java
        return new CaregiverDaoImpl(ConnectionBuilder.getConnection());
    }

    /**
     * Erstellt und gibt eine UserDao Instanz zurück.
     * @return Eine UserDao Instanz.
     */
    public UserDao createUserDAO() {
        return new UserDaoImpl(ConnectionBuilder.getConnection());
    }

    // Wenn Sie eine UserDao haben, fügen Sie hier auch eine create Methode dafür hinzu:
    // public UserDao createUserDao() {
    //     return new UserDaoImpl(ConnectionBuilder.getConnection());
    // }

    // Es ist ratsam, die Connection-Logik zentralisiert zu halten.
    // Wenn ConnectionBuilder nicht existiert oder anders gehandhabt wird,
    // passen Sie die Connection-Beschaffung hier entsprechend an.
}