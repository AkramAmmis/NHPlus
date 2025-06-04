package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.model.RecordStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class CaregiverDaoImpl extends DaoImp<Caregiver> implements CaregiverDao {

    public CaregiverDaoImpl(Connection connection) {
        super(connection);
    }

    @Override
    protected PreparedStatement getCreateStatement(Caregiver caregiver) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "INSERT INTO caregiver (firstname, surname, telephone, status, status_change_date) VALUES (?, ?, ?, ?, ?)";
            preparedStatement = this.connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, caregiver.getFirstName());
            preparedStatement.setString(2, caregiver.getSurname());
            preparedStatement.setString(3, caregiver.getTelephone());
            preparedStatement.setString(4, caregiver.getStatus().toString());
            preparedStatement.setString(5, caregiver.getStatusChangeDate().toString());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    @Override
    protected PreparedStatement getReadByIDStatement(long key) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "SELECT * FROM caregiver WHERE cid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, key);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    @Override
    protected Caregiver getInstanceFromResultSet(ResultSet result) throws SQLException {
        RecordStatus status = RecordStatus.ACTIVE;
        try {
            status = RecordStatus.valueOf(result.getString("status"));
        } catch (IllegalArgumentException | SQLException e) {
            // Falls status nicht existiert oder ungültig ist, verwende ACTIVE als Standardwert
        }

        LocalDate statusChangeDate = LocalDate.now();
        try {
            String dateString = result.getString("status_change_date");
            if (dateString != null && !dateString.isEmpty()) {
                statusChangeDate = LocalDate.parse(dateString);
            }
        } catch (Exception e) {
            // Falls status_change_date nicht existiert oder ungültig ist, verwende heutiges Datum
        }

        return new Caregiver(
                result.getLong("cid"),
                result.getString("firstname"),
                result.getString("surname"),
                result.getString("telephone"),
                status,
                statusChangeDate
        );
    }

    @Override
    protected PreparedStatement getReadAllStatement() {
        PreparedStatement statement = null;
        try {
            final String SQL = "SELECT * FROM caregiver";
            statement = this.connection.prepareStatement(SQL);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return statement;
    }

    @Override
    protected ArrayList<Caregiver> getListFromResultSet(ResultSet result) throws SQLException {
        ArrayList<Caregiver> list = new ArrayList<>();
        while (result.next()) {
            RecordStatus status = RecordStatus.ACTIVE;
            try {
                status = RecordStatus.valueOf(result.getString("status"));
            } catch (IllegalArgumentException | SQLException e) {
                // Falls status nicht existiert oder ungültig ist, verwende ACTIVE als Standardwert
            }

            LocalDate statusChangeDate = LocalDate.now();
            try {
                String dateString = result.getString("status_change_date");
                if (dateString != null && !dateString.isEmpty()) {
                    statusChangeDate = LocalDate.parse(dateString);
                }
            } catch (Exception e) {
                // Falls status_change_date nicht existiert oder ungültig ist, verwende heutiges Datum
            }

            Caregiver caregiver = new Caregiver(
                    result.getLong("cid"),
                    result.getString("firstname"),
                    result.getString("surname"),
                    result.getString("telephone"),
                    status,
                    statusChangeDate
            );
            list.add(caregiver);
        }
        return list;
    }

    @Override
    protected PreparedStatement getUpdateStatement(Caregiver caregiver) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "UPDATE caregiver SET firstname = ?, surname = ?, telephone = ?, status = ?, status_change_date = ? WHERE cid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setString(1, caregiver.getFirstName());
            preparedStatement.setString(2, caregiver.getSurname());
            preparedStatement.setString(3, caregiver.getTelephone());
            preparedStatement.setString(4, caregiver.getStatus().toString());
            preparedStatement.setString(5, caregiver.getStatusChangeDate().toString());
            preparedStatement.setLong(6, caregiver.getCid());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    @Override
    protected PreparedStatement getDeleteStatement(long key) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "DELETE FROM caregiver WHERE cid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, key);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Findet alle Caregivers mit einem bestimmten Status
     * @param status Status nach dem gesucht wird
     * @return Liste der gefundenen Caregivers
     * @throws SQLException bei Datenbankproblemen
     */
    public List<Caregiver> findByStatus(RecordStatus status) throws SQLException {
        List<Caregiver> result = new ArrayList<>();
        final String SQL = "SELECT * FROM caregiver WHERE status = ?";

        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            statement.setString(1, status.toString());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Caregiver caregiver = getInstanceFromResultSet(resultSet);
                result.add(caregiver);
            }
        }

        return result;
    }

    /**
     * Findet alle Caregivers, deren Status älter als das angegebene Datum ist
     * @param date Das Vergleichsdatum
     * @return Liste der gefundenen Caregivers
     * @throws SQLException bei Datenbankproblemen
     */
    public List<Caregiver> findOlderThan(LocalDate date) throws SQLException {
        List<Caregiver> result = new ArrayList<>();
        final String SQL = "SELECT * FROM caregiver WHERE status_change_date < ?";

        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            statement.setString(1, date.toString());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Caregiver caregiver = getInstanceFromResultSet(resultSet);
                result.add(caregiver);
            }
        }

        return result;
    }


    // Die folgenden Methoden sind spezifisch für CaregiverDao und bereits durch DaoImp abgedeckt,
    // aber zur Klarheit hier explizit überschrieben, falls CaregiverDao spezifische Signaturen hätte.
    // Wenn CaregiverDao keine zusätzlichen Methoden über Dao<Caregiver> hinaus definiert,
    // sind diese Überschreibungen technisch nicht notwendig, da sie von DaoImp<Caregiver> geerbt werden.

    @Override
    public long create(Caregiver caregiver) throws SQLException {
        return super.create(caregiver);
    }

    @Override
    public Caregiver read(long key) throws SQLException {
        return super.read(key);
    }

    @Override
    public List<Caregiver> readAll() throws SQLException {
        return super.readAll();
    }

    @Override
    public void update(Caregiver caregiver) throws SQLException {
        super.update(caregiver);
    }

    @Override
    public void deleteById(long key) throws SQLException {
        super.deleteById(key);
    }
}