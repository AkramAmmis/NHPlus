package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.Treatment;
import de.hitec.nhplus.utils.DateConverter;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TreatmentDao extends DaoImp<Treatment> {

    public TreatmentDao(Connection connection) {
        super(connection);
    }

    @Override
    protected PreparedStatement getCreateStatement(Treatment treatment) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "INSERT INTO treatment (pid, cid, treatment_date, begin, end, description, remark) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, treatment.getPid());
            preparedStatement.setLong(2, treatment.getCid());
            preparedStatement.setString(3, treatment.getDate());
            preparedStatement.setString(4, treatment.getBegin());
            preparedStatement.setString(5, treatment.getEnd());
            preparedStatement.setString(6, treatment.getDescription());
            preparedStatement.setString(7, treatment.getRemarks());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    @Override
    protected PreparedStatement getReadByIDStatement(long tid) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "SELECT * FROM treatment WHERE tid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, tid);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    @Override
    protected Treatment getInstanceFromResultSet(ResultSet result) throws SQLException {
        LocalDate date = DateConverter.convertStringToLocalDate(result.getString(3));
        LocalTime begin = DateConverter.convertStringToLocalTime(result.getString(4));
        LocalTime end = DateConverter.convertStringToLocalTime(result.getString(5));
        return new Treatment(result.getLong(1), result.getLong(2),result.getLong(3),
                date, begin, end, result.getString(7), result.getString(8));
    }

    @Override
    protected PreparedStatement getReadAllStatement() {
        PreparedStatement statement = null;
        try {
            final String SQL = "SELECT * FROM treatment";
            statement = this.connection.prepareStatement(SQL);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return statement;
    }

    @Override
    protected ArrayList<Treatment> getListFromResultSet(ResultSet result) throws SQLException {
        ArrayList<Treatment> list = new ArrayList<Treatment>();
        while (result.next()) {
            LocalDate date = DateConverter.convertStringToLocalDate(result.getString(4));
            LocalTime begin = DateConverter.convertStringToLocalTime(result.getString(5));
            LocalTime end = DateConverter.convertStringToLocalTime(result.getString(6));
            Treatment treatment = new Treatment(result.getLong(1), result.getLong(2), result.getLong(3),
                    date, begin, end, result.getString(7), result.getString(8));
            list.add(treatment);
        }
        return list;
    }

    private PreparedStatement getReadAllTreatmentsOfOnePatientByPid(long pid) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "SELECT * FROM treatment WHERE pid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, pid);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    public List<Treatment> readTreatmentsByPid(long pid) throws SQLException {
        ResultSet result = getReadAllTreatmentsOfOnePatientByPid(pid).executeQuery();
        return getListFromResultSet(result);
    }



    /**
     * Generates a <code>PreparedStatement</code> to query all treatments of a caregiver with a given caregiver id (cid).
     *
     * @param cid Caregiver id to query all treatments referencing this id.
     * @return <code>PreparedStatement</code> to query all treatments of the given caregiver id (cid).
     */
    private PreparedStatement getReadAllTreatmentsOfOneCaregiverByCid(long cid) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "SELECT * FROM treatment WHERE cid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, cid);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Queries all treatments of a given caregiver id (cid) and maps the results to an <code>ArrayList</code> with
     * objects of class <code>Treatment</code>.
     *
     * @param cid Caregiver id to query all treatments referencing this id.
     * @return <code>ArrayList</code> with objects of class <code>Treatment</code> of all rows in the
     * <code>ResultSet</code>.
     */
    public List<Treatment> readTreatmentsByCid(long cid) throws SQLException {
        ResultSet result = getReadAllTreatmentsOfOneCaregiverByCid(cid).executeQuery();
        return getListFromResultSet(result);
    }

    /**
     * Generates a <code>PreparedStatement</code> to update the given treatment, identified
     * by the id of the treatment (tid).
     *
     * @param treatment Treatment object to update.
     * @return <code>PreparedStatement</code> to update the given treatment.
     */

    @Override
    protected PreparedStatement getUpdateStatement(Treatment treatment) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL =
                    "UPDATE treatment SET " +
                            "pid = ?, " +
                            "cid= ?," +
                            "treatment_date = ?, " +
                            "begin = ?, " +
                            "end = ?, " +
                            "description = ?, " +
                            "remark = ? " +
                            "WHERE tid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, treatment.getPid());
            preparedStatement.setLong(2, treatment.getCid());
            preparedStatement.setString(3, treatment.getDate());
            preparedStatement.setString(4, treatment.getBegin());
            preparedStatement.setString(5, treatment.getEnd());
            preparedStatement.setString(6, treatment.getDescription());
            preparedStatement.setString(7, treatment.getRemarks());
            preparedStatement.setLong(8, treatment.getTid());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    @Override
    protected PreparedStatement getDeleteStatement(long tid) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL =
                    "DELETE FROM treatment WHERE tid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, tid);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }
}