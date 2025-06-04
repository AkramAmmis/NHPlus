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

    @Override
    protected PreparedStatement getCreateStatement(Patient patient) {
        PreparedStatement preparedStatement = null;
        try {
<<<<<< Datenarchivierungssystem
            preparedStatement = connection.prepareStatement(
                    "INSERT INTO patient (firstname, surname, dateOfBirth, carelevel, roomnumber, status, status_change_date) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
======
            final String SQL = "INSERT INTO patient (firstname, surname, dateOfBirth, carelevel, roomnumber) " +
                    "VALUES (?, ?, ?, ?, ?)";
            preparedStatement = this.connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
>>>>>> FixMerge
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


    /**
     * Generates a <code>PreparedStatement</code> to update the given patient, identified
     * by the id of the patient (pid).
     *
     * @param patient Patient object to update.
     * @return <code>PreparedStatement</code> to update the given patient.
     */

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


}