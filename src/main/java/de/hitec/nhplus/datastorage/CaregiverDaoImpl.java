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
            createTable();

            String username = caregiver.getUsername();
            if (username == null || username.isEmpty()) {
                username = (caregiver.getFirstName().toLowerCase().charAt(0) +
                        caregiver.getSurname().toLowerCase().replace(" ", ""));
                caregiver.setUsername(username);
            }

            UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
            userDao.createTable();

            String password = caregiver.getPassword();
            if (password == null) {
                password = "";
                caregiver.setPassword(password);
            }

            if (this.connection == null) {
                System.err.println("FEHLER: Datenbankverbindung ist null!");
                this.connection = ConnectionBuilder.getConnection();
                if (this.connection == null) {
                    throw new SQLException("Konnte keine Datenbankverbindung herstellen");
                }
            }

            ResultSet tables = this.connection.getMetaData().getTables(null, null, "caregiver", null);
            boolean tableExists = tables.next();
            tables.close();

            if (!tableExists) {
                System.out.println("Tabelle 'caregiver' existiert nicht. Erstelle neu...");
                createTable();
            }

            final String SQL = "INSERT INTO caregiver (firstname, surname, telephone, username, password, status, status_change_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = this.connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, caregiver.getFirstName());
            preparedStatement.setString(2, caregiver.getSurname());
            preparedStatement.setString(3, caregiver.getTelephone());
            preparedStatement.setString(4, username);
            preparedStatement.setString(5, encryptPassword(password));
            preparedStatement.setString(6, caregiver.getStatus().toString());
            preparedStatement.setString(7, caregiver.getStatusChangeDate().toString());

            System.out.println("PreparedStatement erfolgreich erstellt: " + preparedStatement);
        } catch (SQLException exception) {
            System.err.println("Fehler beim Erstellen des PreparedStatements: " + exception.getMessage());
            exception.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unerwarteter Fehler im getCreateStatement: " + e.getMessage());
            e.printStackTrace();
        }

        if (preparedStatement == null) {
            System.err.println("WARNUNG: PreparedStatement ist null nach getCreateStatement!");
        }

        return preparedStatement;
    }

    private String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }

        return de.hitec.nhplus.utils.PasswordUtils.hashPassword(password);
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

        String username = "";
        try {
            username = result.getString("username");
        } catch (SQLException e) {
            // Falls username nicht existiert, verwende leeren String
        }

        String password = "";
        try {
            password = result.getString("password");
        } catch (SQLException e) {
            // Falls password nicht existiert, verwende leeren String
        }

        return new Caregiver(
                result.getLong("cid"),
                username,
                password,
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

            String username = "";
            try {
                username = result.getString("username");
            } catch (SQLException e) {
                // Falls username nicht existiert, verwende leeren String
            }

            String password = "";
            try {
                password = result.getString("password");
            } catch (SQLException e) {
                // Falls password nicht existiert, verwende leeren String
            }

            Caregiver caregiver = new Caregiver(
                    result.getLong("cid"),
                    username,
                    password,
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
            final String SQL = "UPDATE caregiver SET username = ?, password = ?, firstname = ?, surname = ?, telephone = ?, status = ?, status_change_date = ? WHERE cid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setString(1, caregiver.getUsername());

            String currentPasswordInDb = getPasswordFromDatabase(caregiver.getCid());
            System.out.println("Aktuelles Passwort in DB: " + (currentPasswordInDb != null ? "[verschlüsselt]" : "null"));
            System.out.println("Neues Passwort: " + (caregiver.getPassword() != null ? "[vorhanden]" : "null"));

            preparedStatement.setString(2, encryptPassword(caregiver.getPassword()));
            System.out.println("Passwort wurde verschlüsselt und wird aktualisiert.");

            preparedStatement.setString(3, caregiver.getFirstName());
            preparedStatement.setString(4, caregiver.getSurname());
            preparedStatement.setString(5, caregiver.getTelephone());
            preparedStatement.setString(6, caregiver.getStatus().toString());
            preparedStatement.setString(7, caregiver.getStatusChangeDate().toString());
            preparedStatement.setLong(8, caregiver.getCid());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    private String getPasswordFromDatabase(long cid) {
        try {
            PreparedStatement st = connection.prepareStatement("SELECT password FROM caregiver WHERE cid = ?");
            st.setLong(1, cid);

            ResultSet rs = st.executeQuery();
            String password = null;
            if (rs.next()) {
                password = rs.getString("password");
            }
            rs.close();
            st.close();
            return password;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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

    @Override
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
            System.out.println("Prüfe, ob Tabelle 'caregiver' existiert...");

            ResultSet tables = connection.getMetaData().getTables(null, null, "caregiver", null);
            boolean tableExists = tables.next();
            tables.close();

            if (!tableExists) {
                System.out.println("Tabelle 'caregiver' existiert nicht. Erstelle neu...");
                String createTableSQL = "CREATE TABLE IF NOT EXISTS caregiver (" +
                        "cid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT, " +
                        "password TEXT, " +
                        "firstname TEXT NOT NULL, " +
                        "surname TEXT NOT NULL, " +
                        "telephone TEXT, " +
                        "status TEXT, " +
                        "status_change_date TEXT)";

                System.out.println("Führe SQL aus: " + createTableSQL);
                st.executeUpdate(createTableSQL);
                System.out.println("Tabelle 'caregiver' erfolgreich erstellt.");
            } else {
                System.out.println("Tabelle 'caregiver' existiert bereits.");

                // Überprüfe und füge username und password Spalten hinzu, falls sie fehlen
                try {
                    ResultSet columns = connection.getMetaData().getColumns(null, null, "caregiver", "username");
                    boolean usernameExists = columns.next();
                    columns.close();

                    if (!usernameExists) {
                        System.out.println("Spalten 'username' und 'password' fehlen. Füge hinzu...");
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN username TEXT");
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN password TEXT");

                        st.executeUpdate("UPDATE caregiver SET username = 'caregiver_' || cid, password = '' WHERE username IS NULL");
                        System.out.println("Spalten erfolgreich hinzugefügt und mit Standardwerten gefüllt.");
                    }
                } catch (SQLException e) {
                    System.out.println("Fehler beim Prüfen der Spalten: " + e.getMessage());
                    try {
                        System.out.println("Versuche, Spalten 'username' und 'password' hinzuzufügen...");
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN username TEXT UNIQUE");
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN password TEXT");

                        st.executeUpdate("UPDATE caregiver SET username = 'caregiver_' || cid, password = '' WHERE username IS NULL");
                        System.out.println("Spalten erfolgreich hinzugefügt und mit Standardwerten gefüllt.");
                    } catch (SQLException ex) {
                        System.err.println("Fehler beim Hinzufügen der Spalten: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }

                // Überprüfe und füge status und status_change_date Spalten hinzu, falls sie fehlen
                try {
                    ResultSet columns = connection.getMetaData().getColumns(null, null, "caregiver", "status");
                    boolean statusExists = columns.next();
                    columns.close();

                    if (!statusExists) {
                        System.out.println("Spalten 'status' und 'status_change_date' fehlen. Füge hinzu...");
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN status TEXT");
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN status_change_date TEXT");

                        st.executeUpdate("UPDATE caregiver SET status = 'ACTIVE', status_change_date = date('now') WHERE status IS NULL");
                        System.out.println("Statusspalten erfolgreich hinzugefügt und mit Standardwerten gefüllt.");
                    }
                } catch (SQLException e) {
                    System.out.println("Fehler beim Prüfen der Statusspalten: " + e.getMessage());
                    try {
                        System.out.println("Versuche, Spalten 'status' und 'status_change_date' hinzuzufügen...");
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN status TEXT");
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN status_change_date TEXT");

                        st.executeUpdate("UPDATE caregiver SET status = 'ACTIVE', status_change_date = date('now') WHERE status IS NULL");
                        System.out.println("Statusspalten erfolgreich hinzugefügt und mit Standardwerten gefüllt.");
                    } catch (SQLException ex) {
                        System.err.println("Fehler beim Hinzufügen der Statusspalten: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }

            st.close();
            System.out.println("Tabelle 'caregiver' ist bereit.");

        } catch (SQLException e) {
            System.err.println("SQL-Fehler in createTable: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unerwarteter Fehler in createTable: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Caregiver createCaregiver(Caregiver caregiver) {
        try {
            long id = super.create(caregiver);
            caregiver.setCid(id);
            return caregiver;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ArrayList<Caregiver> readAllCaregivers() {
        try {
            return (ArrayList<Caregiver>) readAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Caregiver authenticate(String username, String password) {
        try {
            PreparedStatement st = connection.prepareStatement(
                    "SELECT * FROM caregiver WHERE username = ?"
            );
            st.setString(1, username);

            ResultSet rs = st.executeQuery();
            Caregiver caregiver = null;

            if (rs.next()) {
                caregiver = getInstanceFromResultSet(rs);

                String encryptedPassword = encryptPassword(password);
                if (!encryptedPassword.equals(caregiver.getPassword())) {
                    caregiver = null;
                }
            }

            rs.close();
            st.close();
            return caregiver;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Caregiver readCaregiver(long cid) {
        try {
            return read(cid);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateCaregiver(Caregiver caregiver) {
        try {
            update(caregiver);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Caregiver caregiver) {
        try {
            deleteById(caregiver.getCid());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}