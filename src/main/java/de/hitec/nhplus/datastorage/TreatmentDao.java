package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.RecordStatus;
import de.hitec.nhplus.model.Treatment;
import de.hitec.nhplus.utils.DateConverter;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the Interface <code>DaoImp</code>. Overrides methods to generate specific <code>PreparedStatements</code>,
 * to execute the specific SQL Statements.
 */
public class TreatmentDao extends DaoImp<Treatment> {

    /**
     * The constructor initiates an object of <code>TreatmentDao</code> and passes the connection to its super class.
     *
     * @param connection Object of <code>Connection</code> to execute the SQL-statements.
     */
    public TreatmentDao(Connection connection) {
        super(connection);
    }

    /**
     * Generates a <code>PreparedStatement</code> to persist the given object of <code>Treatment</code>.
     *
     * @param treatment Object of <code>Treatment</code> to persist.
     * @return <code>PreparedStatement</code> to insert the given patient.
     */
    @Override
    protected PreparedStatement getCreateStatement(Treatment treatment) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(
                    "INSERT INTO treatment (pid, cid, treatment_date, begin, end, description, remark, status, status_change_date) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setLong(1, treatment.getPid());
            statement.setLong(2, treatment.getCid());
            statement.setString(3, treatment.getDate());
            statement.setString(4, treatment.getBegin());
            statement.setString(5, treatment.getEnd());
            statement.setString(6, treatment.getDescription());
            statement.setString(7, treatment.getRemarks());
            statement.setString(8, treatment.getStatus().name());
            statement.setString(9, treatment.getStatusChangeDate().toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }


    /**
     * Generates a <code>PreparedStatement</code> to query a treatment by a given treatment id (tid).
     *
     * @param tid Treatment id to query.
     * @return <code>PreparedStatement</code> to query the treatment.
     */
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

    /**
     * Maps a <code>ResultSet</code> of one treatment to an object of <code>Treatment</code>.
     *
     * @param result ResultSet with a single row. Columns will be mapped to an object of class <code>Treatment</code>.
     * @return Object of class <code>Treatment</code> with the data from the resultSet.
     */
    @Override
    protected Treatment getInstanceFromResultSet(ResultSet result) throws SQLException {
        LocalDate date = DateConverter.convertStringToLocalDate(result.getString(4));
        LocalTime begin = DateConverter.convertStringToLocalTime(result.getString(5));
        LocalTime end = DateConverter.convertStringToLocalTime(result.getString(6));

        RecordStatus status = RecordStatus.ACTIVE; // Default if not in DB
        if (result.getString(9) != null) {
            status = RecordStatus.valueOf(result.getString(9));
        }

        LocalDate statusChangeDate = LocalDate.now(); // Default if not in DB
        if (result.getString(10) != null) {
            statusChangeDate = DateConverter.convertStringToLocalDate(result.getString(10));
        }

        return new Treatment(
                result.getLong(1),
                result.getLong(2),
                result.getLong(3),
                date,
                begin,
                end,
                result.getString(7),
                result.getString(8),
                status,
                statusChangeDate
        );
    }

    /**
     * Generates a <code>PreparedStatement</code> to query all treatments.
     *
     * @return <code>PreparedStatement</code> to query all treatments.
     */
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

    /**
     * Maps a <code>ResultSet</code> of all treatments to an <code>ArrayList</code> with objects of class
     * <code>Treatment</code>.
     *
     * @param result ResultSet with all rows. The columns will be mapped to objects of class <code>Treatment</code>.
     * @return <code>ArrayList</code> with objects of class <code>Treatment</code> of all rows in the
     * <code>ResultSet</code>.
     */
    @Override
    protected ArrayList<Treatment> getListFromResultSet(ResultSet result) throws SQLException {
        ArrayList<Treatment> list = new ArrayList<Treatment>();
        while (result.next()) {
            LocalDate date = DateConverter.convertStringToLocalDate(result.getString(4));
            LocalTime begin = DateConverter.convertStringToLocalTime(result.getString(5));
            LocalTime end = DateConverter.convertStringToLocalTime(result.getString(6));

            RecordStatus status = RecordStatus.ACTIVE; // Default if not in DB
            if (result.getString(9) != null) {
                status = RecordStatus.valueOf(result.getString(9));
            }

            LocalDate statusChangeDate = LocalDate.now(); // Default if not in DB
            if (result.getString(10) != null) {
                statusChangeDate = DateConverter.convertStringToLocalDate(result.getString(10));
            }

            Treatment treatment = new Treatment(
                    result.getLong(1),
                    result.getLong(2),
                    result.getLong(3),
                    date,
                    begin,
                    end,
                    result.getString(7),
                    result.getString(8),
                    status,
                    statusChangeDate
            );
            list.add(treatment);
        }
        return list;
    }

    /**
     * Generates a <code>PreparedStatement</code> to query all treatments of a patient with a given patient id (pid).
     *
     * @param pid Patient id to query all treatments referencing this id.
     * @return <code>PreparedStatement</code> to query all treatments of the given patient id (pid).
     */
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

    /**
     * Queries all treatments of a given patient id (pid) and maps the results to an <code>ArrayList</code> with
     * objects of class <code>Treatment</code>.
     *
     * @param pid Patient id to query all treatments referencing this id.
     * @return <code>ArrayList</code> with objects of class <code>Treatment</code> of all rows in the
     * <code>ResultSet</code>.
     */
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
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(
                    "UPDATE treatment SET pid = ?, cid = ?, treatment_date = ?, begin = ?, end = ?, " +
                            "description = ?, remark = ?, status = ?, status_change_date = ? WHERE tid = ?");
            statement.setLong(1, treatment.getPid());
            statement.setLong(2, treatment.getCid());
            statement.setString(3, treatment.getDate());
            statement.setString(4, treatment.getBegin());
            statement.setString(5, treatment.getEnd());
            statement.setString(6, treatment.getDescription());
            statement.setString(7, treatment.getRemarks());
            statement.setString(8, treatment.getStatus().name());
            statement.setString(9, treatment.getStatusChangeDate().toString());
            statement.setLong(10, treatment.getTid());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }


    /**
     * Generates a <code>PreparedStatement</code> to delete a treatment with the given id.
     *
     * @param tid Id of the Treatment to delete.
     * @return <code>PreparedStatement</code> to delete treatment with the given id.
     */
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

    /**
     * Finds all treatments with a specific status
     * @param status The status to search for
     * @return List of found treatments
     * @throws SQLException on database problems
     */
    public List<Treatment> findByStatus(RecordStatus status) throws SQLException {
        List<Treatment> result = new ArrayList<>();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(
                    "SELECT * FROM treatment WHERE status = ?");
            statement.setString(1, status.name());
            ResultSet resultSet = statement.executeQuery();
            result = getListFromResultSet(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return result;
    }

    /**
     * Finds all treatments that are older than the specified date
     * @param date The comparison date
     * @return List of found treatments
     * @throws SQLException on database problems
     */
    public List<Treatment> findOlderThan(LocalDate date) throws SQLException {
        List<Treatment> result = new ArrayList<>();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(
                    "SELECT * FROM treatment WHERE treatment_date < ?");
            statement.setString(1, date.toString());
            ResultSet resultSet = statement.executeQuery();
            result = getListFromResultSet(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return result;
    }

}