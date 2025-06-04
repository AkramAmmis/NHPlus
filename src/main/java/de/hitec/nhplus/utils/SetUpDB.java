package de.hitec.nhplus.utils;

import de.hitec.nhplus.datastorage.*;
import de.hitec.nhplus.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static de.hitec.nhplus.utils.DateConverter.convertStringToLocalDate;
import static de.hitec.nhplus.utils.DateConverter.convertStringToLocalTime;

public class SetUpDB {

    public static void setUpDb() {
        Connection connection = ConnectionBuilder.getConnection();
        SetUpDB.wipeDb(connection);
        SetUpDB.setUpTablePatient(connection);
        SetUpDB.setUpTableCaregiver(connection);
        SetUpDB.setUpTableTreatment(connection);
        SetUpDB.setUpPatients();
        SetUpDB.setUpCaregivers();
        SetUpDB.setUpTreatments();
    }

    public static void wipeDb(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS treatment");
            statement.execute("DROP TABLE IF EXISTS patient");
            statement.execute("DROP TABLE IF EXISTS caregiver");
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void setUpTablePatient(Connection connection) {
        final String SQL = "CREATE TABLE IF NOT EXISTS patient (" +
                "   pid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "   firstname TEXT NOT NULL, " +
                "   surname TEXT NOT NULL, " +
                "   dateOfBirth TEXT NOT NULL, " +
                "   carelevel TEXT NOT NULL, " +
                "   roomnumber TEXT NOT NULL, " +
                "   status TEXT DEFAULT 'ACTIVE', " +
                "   status_change_date TEXT " +
                ");";
        try (Statement statement = connection.createStatement()) {
            statement.execute(SQL);
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void setUpTableCaregiver(Connection connection) {
        final String SQL = "CREATE TABLE IF NOT EXISTS caregiver (" +
                "   cid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "   firstname TEXT NOT NULL, " +
                "   surname TEXT NOT NULL, " +
                "   telephone TEXT NOT NULL, " +
                "   username TEXT UNIQUE NOT NULL, " +
                "   password TEXT NOT NULL, " +
                "   role TEXT NOT NULL DEFAULT 'USER', " +
                "   locked BOOLEAN DEFAULT 0, " +
                "   failed_attempts INTEGER DEFAULT 0, " +
                "   last_failed_attempt DATETIME, " +
                "   status TEXT DEFAULT 'ACTIVE', " +
                "   status_change_date TEXT " +
                ");";
        try (Statement statement = connection.createStatement()) {
            statement.execute(SQL);
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void setUpTableTreatment(Connection connection) {
        final String SQL = "CREATE TABLE IF NOT EXISTS treatment (" +
                "   tid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "   pid INTEGER NOT NULL, " +
                "   cid INTEGER NOT NULL, " +
                "   treatment_date TEXT NOT NULL, " +
                "   begin TEXT NOT NULL, " +
                "   end TEXT NOT NULL, " +
                "   description TEXT NOT NULL, " +
                "   remark TEXT NOT NULL," +
                "   status TEXT DEFAULT 'ACTIVE', " +
                "   status_change_date TEXT, " +
                "   FOREIGN KEY (pid) REFERENCES patient (pid) ON DELETE CASCADE, " +
                "   FOREIGN KEY (cid) REFERENCES caregiver (cid) ON DELETE CASCADE " +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(SQL);
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void setUpPatients() {
        try {
            PatientDao dao = DaoFactory.getDaoFactory().createPatientDAO();
            dao.create(new Patient("Seppl", "Herberger", convertStringToLocalDate("1945-12-01"), "4", "202"));
            dao.create(new Patient("Martina", "Gerdsen", convertStringToLocalDate("1954-08-12"), "5", "010"));
            dao.create(new Patient("Gertrud", "Franzen", convertStringToLocalDate("1949-04-16"), "3", "002"));
            dao.create(new Patient("Ahmet", "Yilmaz", convertStringToLocalDate("1941-02-22"), "3", "013"));
            dao.create(new Patient("Hans", "Neumann", convertStringToLocalDate("1955-12-12"), "2", "001"));
            dao.create(new Patient("Elisabeth", "Müller", convertStringToLocalDate("1958-03-07"), "5", "110"));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static void setUpCaregivers() {
        try {
            CaregiverDao dao = DaoFactory.getDaoFactory().createCaregiverDAO();
            UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();

            dao.create(new Caregiver(0, "hneumann", "password123", "Hans", "Neumann", "0123456789"));
            dao.create(new Caregiver(0, "lneubauer", "password123", "Luise", "Neubauer", "02314457893"));
            dao.create(new Caregiver(0, "jmeyer", "password123", "Jonas", "Meyer", "0173228845"));

            userDao.createUser(new User("hneumann", "password123", "Hans", "Neumann", "", "0123456789", UserRole.CAREGIVER));
            userDao.createUser(new User("lneubauer", "password123", "Luise", "Neubauer", "", "02314457893", UserRole.CAREGIVER));
            userDao.createUser(new User("jmeyer", "password123", "Jonas", "Meyer", "", "0173228845", UserRole.CAREGIVER));

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static void setUpTreatments() {
        try {
            TreatmentDao dao = DaoFactory.getDaoFactory().createTreatmentDao();

            // Korrekte Verwendung des Konstruktors für bereits persistierte Behandlungen
            dao.create(new Treatment(1, 1, convertStringToLocalDate("2023-06-03"),
                    convertStringToLocalTime("11:00"), convertStringToLocalTime("15:00"),
                    "Gespräch", "Der Patient hat enorme Angstgefühle und glaubt, er sei überfallen worden. Ihm seien alle Wertsachen gestohlen worden.\nPatient beruhigt sich erst, als alle Wertsachen im Zimmer gefunden worden sind."));

            dao.create(new Treatment(1, 1, convertStringToLocalDate("2023-06-05"),
                    convertStringToLocalTime("11:00"), convertStringToLocalTime("12:30"),
                    "Gespräch", "Patient irrt auf der Suche nach gestohlenen Wertsachen durch die Etage und bezichtigt andere Bewohner des Diebstahls.\nPatient wird in seinen Raum zurückbegleitet und erhält Beruhigungsmittel."));

            dao.create(new Treatment(2, 2, convertStringToLocalDate("2023-06-04"),
                    convertStringToLocalTime("07:30"), convertStringToLocalTime("08:00"),
                    "Waschen", "Patient mit Waschlappen gewaschen und frisch angezogen. Patient gewendet."));

            dao.create(new Treatment(3, 2, convertStringToLocalDate("2023-06-06"),
                    convertStringToLocalTime("15:10"), convertStringToLocalTime("16:00"),
                    "Spaziergang", "Spaziergang im Park, Patient döst im Rollstuhl ein"));

            dao.create(new Treatment(4, 3, convertStringToLocalDate("2023-06-08"),
                    convertStringToLocalTime("15:00"), convertStringToLocalTime("16:00"),
                    "Spaziergang", "Parkspaziergang; Patient ist heute lebhafter und hat klare Momente; erzählt von seiner Tochter"));

            dao.create(new Treatment(4, 3, convertStringToLocalDate("2023-06-07"),
                    convertStringToLocalTime("11:00"), convertStringToLocalTime("11:30"),
                    "Waschen", "Waschen per Dusche auf einem Stuhl; Patientin gewendet;"));

            dao.create(new Treatment(5, 2, convertStringToLocalDate("2023-06-08"),
                    convertStringToLocalTime("15:00"), convertStringToLocalTime("15:30"),
                    "Physiotherapie", "Übungen zur Stabilisation und Mobilisierung der Rückenmuskulatur"));

            dao.create(new Treatment(3, 2, convertStringToLocalDate("2023-08-24"),
                    convertStringToLocalTime("09:30"), convertStringToLocalTime("10:15"),
                    "KG", "Lympfdrainage"));

            dao.create(new Treatment(6, 1, convertStringToLocalDate("2023-08-31"),
                    convertStringToLocalTime("13:30"), convertStringToLocalTime("13:45"),
                    "Toilettengang", "Hilfe beim Toilettengang; Patientin klagt über Schmerzen beim Stuhlgang. Gabe von Iberogast"));

            dao.create(new Treatment(6, 1, convertStringToLocalDate("2023-09-01"),
                    convertStringToLocalTime("16:00"), convertStringToLocalTime("17:00"),
                    "KG", "Massage der Extremitäten zur Verbesserung der Durchblutung"));

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SetUpDB.setUpDb();
    }

}

