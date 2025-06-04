package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.Patient;
import de.hitec.nhplus.model.RecordStatus;
import de.hitec.nhplus.utils.DateConverter;
import java.sql.Statement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientDao extends DaoImp<Patient> {

    public PatientDao(Connection connection) {
        super(connection);
    }
  
    /**
     * Generates a <code>PreparedStatement</code> to persist the given object of <code>Patient</code>.
     *
     * @param patient Object of <code>Patient</code> to persist.
     * @return <code>PreparedStatement</code> to insert the given patient.
     */

    @Override
    protected PreparedStatement getCreateStatement(Patient patient) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(
                    "INSERT INTO patient (firstname, surname, dateOfBirth, carelevel, roomnumber, status, status_change_date) " + 
                    "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getSurname());
            preparedStatement.setString(3, patient.getDateOfBirth());
            preparedStatement.setString(4, patient.getCareLevel());
            preparedStatement.setString(5, patient.getRoomNumber());
            preparedStatement.setString(6, patient.getStatus().toString());
            preparedStatement.setString(7, patient.getStatusChangeDate().toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return preparedStatement;
    }

    @Override
    protected PreparedStatement getReadByIDStatement(long pid) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "SELECT * FROM patient WHERE pid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, pid);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    @Override
    protected Patient getInstanceFromResultSet(ResultSet result) throws SQLException {
        RecordStatus status = RecordStatus.ACTIVE;
        try {
            status = RecordStatus.valueOf(result.getString("status"));
        } catch (IllegalArgumentException | SQLException e) {
        }

        LocalDate statusChangeDate = LocalDate.now();
        try {
            String dateString = result.getString("status_change_date");
            if (dateString != null && !dateString.isEmpty()) {
                statusChangeDate = LocalDate.parse(dateString);
            }
        } catch (Exception e) {
        }

        Patient patient = new Patient(
                result.getLong("pid"),
                result.getString("firstname"),
                result.getString("surname"),
                DateConverter.convertStringToLocalDate(result.getString("dateOfBirth")),
                result.getString("carelevel"),
                result.getString("roomnumber"),
                status,
                statusChangeDate
        );
        return patient;
    }

    @Override
    protected PreparedStatement getReadAllStatement() {
        PreparedStatement statement = null;
        try {
            final String SQL = "SELECT * FROM patient";
            statement = this.connection.prepareStatement(SQL);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return statement;
    }

    @Override
    protected ArrayList<Patient> getListFromResultSet(ResultSet result) throws SQLException {
        ArrayList<Patient> list = new ArrayList<>();
        while (result.next()) {
            RecordStatus status = RecordStatus.ACTIVE;
            try {
                status = RecordStatus.valueOf(result.getString("status"));
            } catch (IllegalArgumentException | SQLException e) {}

            LocalDate statusChangeDate = LocalDate.now();
            try {
                String dateString = result.getString("status_change_date");
                if (dateString != null && !dateString.isEmpty()) {
                    statusChangeDate = LocalDate.parse(dateString);
                }
            } catch (Exception e) {}

            Patient patient = new Patient(
                    result.getLong("pid"),
                    result.getString("firstname"),
                    result.getString("surname"),
                    DateConverter.convertStringToLocalDate(result.getString("dateOfBirth")),
                    result.getString("carelevel"),
                    result.getString("roomnumber"),
                    status,
                    statusChangeDate
            );
            list.add(patient);
        }
        return list;
    }

    @Override
    protected PreparedStatement getUpdateStatement(Patient patient) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL =
                    "UPDATE patient SET " +
                            "firstname = ?, " +
                            "surname = ?, " +
                            "dateOfBirth = ?, " +
                            "carelevel = ?, " +
                            "roomnumber = ?, " +
                            "status = ?, " +
                            "status_change_date = ?" +
                            "WHERE pid = ?";

            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getSurname());
            preparedStatement.setString(3, patient.getDateOfBirth());
            preparedStatement.setString(4, patient.getCareLevel());
            preparedStatement.setString(5, patient.getRoomNumber());
            preparedStatement.setString(6, patient.getStatus().toString());
            preparedStatement.setString(7, patient.getStatusChangeDate().toString());
            preparedStatement.setLong(8, patient.getPid());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    @Override
    protected PreparedStatement getDeleteStatement(long pid) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "DELETE FROM patient WHERE pid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, pid);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Findet alle Patienten mit einem bestimmten Status
     *
     * @param status Status nach dem gesucht wird
     * @return Liste der gefundenen Patienten
     * @throws SQLException bei Datenbankproblemen
     */
    public List<Patient> findByStatus(RecordStatus status) throws SQLException {
        List<Patient> result = new ArrayList<>();
        final String SQL = "SELECT * FROM patient WHERE status = ?";

        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            statement.setString(1, status.toString());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Patient patient = getInstanceFromResultSet(resultSet);
                result.add(patient);
            }
        }

        return result;
    }

    /**
     * Findet alle Patienten, deren Status älter als das angegebene Datum ist
     *
     * @param date Das Vergleichsdatum
     * @return Liste der gefundenen Patienten
     * @throws SQLException bei Datenbankproblemen
     */
    public List<Patient> findOlderThan(LocalDate date) throws SQLException {
        List<Patient> result = new ArrayList<>();
        final String SQL = "SELECT * FROM patient WHERE status_change_date < ?";

        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            statement.setString(1, date.toString());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Patient patient = getInstanceFromResultSet(resultSet);
                result.add(patient);
            }
        }

        return result;
    }

    /**
     * Erstellt die Tabelle "patient" in der Datenbank, falls sie noch nicht existiert
     */
    public void createTable() {
        try {
            if (connection == null) {
                System.err.println("FEHLER: Connection ist null in createTable");
                connection = ConnectionBuilder.getConnection();
                if (connection == null) {
                    throw new SQLException("Konnte keine Datenbankverbindung herstellen");
                }
            }

            Statement st = connection.createStatement();
            System.out.println("Prüfe, ob Tabelle 'patient' existiert...");

            ResultSet tables = connection.getMetaData().getTables(null, null, "patient", null);
            boolean tableExists = tables.next();
            tables.close();

            if (!tableExists) {
                System.out.println("Tabelle 'patient' existiert nicht. Erstelle neu...");
                String createTableSQL = "CREATE TABLE IF NOT EXISTS patient (" +
                        "pid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "firstname TEXT NOT NULL, " +
                        "surname TEXT NOT NULL, " +
                        "dateOfBirth TEXT, " +
                        "carelevel TEXT, " +
                        "roomnumber TEXT, " +
                        "status TEXT, " +
                        "status_change_date TEXT)";

                System.out.println("Führe SQL aus: " + createTableSQL);
                st.executeUpdate(createTableSQL);
                System.out.println("Tabelle 'patient' erfolgreich erstellt.");
            } else {
                System.out.println("Tabelle 'patient' existiert bereits.");

                // Überprüfe und füge status und status_change_date Spalten hinzu, falls sie fehlen
                try {
                    ResultSet columns = connection.getMetaData().getColumns(null, null, "patient", "status");
                    boolean statusExists = columns.next();
                    columns.close();

                    if (!statusExists) {
                        System.out.println("Spalten 'status' und 'status_change_date' fehlen. Füge hinzu...");
                        st.executeUpdate("ALTER TABLE patient ADD COLUMN status TEXT");
                        st.executeUpdate("ALTER TABLE patient ADD COLUMN status_change_date TEXT");

                        st.executeUpdate("UPDATE patient SET status = 'ACTIVE', status_change_date = date('now') WHERE status IS NULL");
                        System.out.println("Statusspalten erfolgreich hinzugefügt und mit Standardwerten gefüllt.");
                    }
                } catch (SQLException e) {
                    System.out.println("Fehler beim Prüfen der Statusspalten: " + e.getMessage());
                    try {
                        System.out.println("Versuche, Spalten 'status' und 'status_change_date' hinzuzufügen...");
                        st.executeUpdate("ALTER TABLE patient ADD COLUMN status TEXT");
                        st.executeUpdate("ALTER TABLE patient ADD COLUMN status_change_date TEXT");

                        st.executeUpdate("UPDATE patient SET status = 'ACTIVE', status_change_date = date('now') WHERE status IS NULL");
                        System.out.println("Statusspalten erfolgreich hinzugefügt und mit Standardwerten gefüllt.");
                    } catch (SQLException ex) {
                        System.err.println("Fehler beim Hinzufügen der Statusspalten: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }

            st.close();
            System.out.println("Tabelle 'patient' ist bereit.");

        } catch (SQLException e) {
            System.err.println("SQL-Fehler in createTable: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unerwarteter Fehler in createTable: " + e.getMessage());
            e.printStackTrace();
        }
    }
}