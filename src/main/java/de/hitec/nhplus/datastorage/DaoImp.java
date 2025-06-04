package de.hitec.nhplus.datastorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class DaoImp<T> implements Dao<T> {
    protected Connection connection;

    public DaoImp(Connection connection) {
        this.connection = connection;
    }

    @Override
    public long create(T t) throws SQLException {
        PreparedStatement preparedStatement = getCreateStatement(t);


        try {
            preparedStatement.executeUpdate();


            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new SQLException("Creating " + t.getClass().getSimpleName() + " failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL-Fehler beim Ausführen des PreparedStatements: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unerwarteter Fehler beim Erstellen des Objekts: " + e.getMessage());
            throw new SQLException("Fehler beim Erstellen: " + e.getMessage(), e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                System.err.println("Fehler beim Schließen des PreparedStatements: " + e.getMessage());
            }
        }
    }

    @Override
    public T read(long key) throws SQLException {
        T object = null;
        ResultSet result = getReadByIDStatement(key).executeQuery();
        if (result.next()) {
            object = getInstanceFromResultSet(result);
        }
        return object;
    }

    @Override
    public List<T> readAll() throws SQLException {
        return getListFromResultSet(getReadAllStatement().executeQuery());
    }

    @Override
    public void update(T t) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = getUpdateStatement(t);
            if (preparedStatement == null) {
                throw new SQLException("Fehler beim Aktualisieren des Objekts " + t.getClass().getSimpleName() + ": PreparedStatement ist null.");
            }
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Update durchgeführt. Betroffene Zeilen: " + rowsAffected);
        } catch (SQLException e) {
            System.err.println("SQL-Fehler beim Ausführen des Update-PreparedStatements: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unerwarteter Fehler beim Aktualisieren des Objekts: " + e.getMessage());
            throw new SQLException("Fehler beim Aktualisieren: " + e.getMessage(), e);
        } finally {
            try {
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                System.err.println("Fehler beim Schließen des PreparedStatements: " + e.getMessage());
            }
        }
    }

    @Override
    public void deleteById(long key) throws SQLException {
        getDeleteStatement(key).executeUpdate();
    }

    protected abstract T getInstanceFromResultSet(ResultSet set) throws SQLException;

    protected abstract ArrayList<T> getListFromResultSet(ResultSet set) throws SQLException;


    protected abstract PreparedStatement getCreateStatement(T t);

    protected abstract PreparedStatement getReadByIDStatement(long key);

    protected abstract PreparedStatement getReadAllStatement();

    protected abstract PreparedStatement getUpdateStatement(T t);

    protected abstract PreparedStatement getDeleteStatement(long key);
}