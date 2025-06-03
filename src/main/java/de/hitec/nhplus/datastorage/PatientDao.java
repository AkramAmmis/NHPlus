package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.Patient;
import de.hitec.nhplus.utils.DateConverter;
import java.sql.Statement;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class PatientDao extends DaoImp<Patient> {

    public PatientDao(Connection connection) {
        super(connection);
    }

    @Override
    protected PreparedStatement getCreateStatement(Patient patient) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "INSERT INTO patient (firstname, surname, dateOfBirth, carelevel, roomnumber) " +
                    "VALUES (?, ?, ?, ?, ?)";
            preparedStatement = this.connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getSurname());
            preparedStatement.setString(3, patient.getDateOfBirth());
            preparedStatement.setString(4, patient.getCareLevel());
            preparedStatement.setString(5, patient.getRoomNumber());
        } catch (SQLException exception) {
            exception.printStackTrace();
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
        return new Patient(
                result.getInt(1),
                result.getString(2),
                result.getString(3),
                DateConverter.convertStringToLocalDate(result.getString(4)),
                result.getString(5),
                result.getString(6));
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
            LocalDate date = DateConverter.convertStringToLocalDate(result.getString(4));
            Patient patient = new Patient(result.getInt(1), result.getString(2),
                    result.getString(3), date,
                    result.getString(5), result.getString(6));
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
                            "WHERE pid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getSurname());
            preparedStatement.setString(3, patient.getDateOfBirth());
            preparedStatement.setString(4, patient.getCareLevel());
            preparedStatement.setString(5, patient.getRoomNumber());
            preparedStatement.setLong(7, patient.getPid());
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
}