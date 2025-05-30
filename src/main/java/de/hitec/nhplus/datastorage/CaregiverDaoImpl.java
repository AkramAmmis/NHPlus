package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            // Prüfen, ob die Tabelle existiert
            createTable();

            // Anmeldedaten werden in der User-Tabelle gespeichert
            String username = caregiver.getUsername();
            if (username == null || username.isEmpty()) {
                // Generiere einen Benutzernamen aus dem Namen
                username = (caregiver.getFirstName().toLowerCase().charAt(0) + 
                          caregiver.getSurname().toLowerCase().replace(" ", ""));
                caregiver.setUsername(username);
            }

            // Erstelle auch einen entsprechenden User für die Anmeldedaten
            UserDao userDao = DaoFactory.getDaoFactory().createUserDAO();
            userDao.createTable(); // Stelle sicher, dass die User-Tabelle existiert

            String password = caregiver.getPassword();
            if (password == null) {
                password = ""; // Leeres Passwort als Standard
                caregiver.setPassword(password);
            }

            // Stellen Sie sicher, dass die Connection nicht null ist
            if (this.connection == null) {
                System.err.println("FEHLER: Datenbankverbindung ist null!");
                // Versuchen, eine neue Verbindung zu bekommen
                this.connection = ConnectionBuilder.getConnection();
                if (this.connection == null) {
                    throw new SQLException("Konnte keine Datenbankverbindung herstellen");
                }
            }

            // Tabelle in der Datenbank überprüfen
            Statement checkStatement = this.connection.createStatement();
            ResultSet tables = this.connection.getMetaData().getTables(null, null, "caregiver", null);
            boolean tableExists = tables.next();
            tables.close();

            if (!tableExists) {
                System.out.println("Tabelle 'caregiver' existiert nicht. Erstelle neu...");
                createTable();
            }

            final String SQL = "INSERT INTO caregiver (firstname, surname, telephone, username, password) VALUES (?, ?, ?, ?, ?)";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setString(1, caregiver.getFirstName());
            preparedStatement.setString(2, caregiver.getSurname());
            preparedStatement.setString(3, caregiver.getTelephone());
            preparedStatement.setString(4, username);
            preparedStatement.setString(5, encryptPassword(password));

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

    /**
     * Verschlüsselt das Passwort mit einem sicheren Hashing-Algorithmus
     * @param password Das zu verschlüsselnde Passwort
     * @return Das verschlüsselte Passwort
     */
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
        return new Caregiver(
                result.getLong("cid"),
                result.getString("username"),
                result.getString("password"),
                result.getString("firstname"),
                result.getString("surname"),
                result.getString("telephone")
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
            Caregiver caregiver = new Caregiver(
                    result.getLong("cid"),
                    result.getString("firstname"),
                    result.getString("surname"),
                    result.getString("telephone")
            );
            list.add(caregiver);
        }
        return list;
    }

    @Override
    protected PreparedStatement getUpdateStatement(Caregiver caregiver) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "UPDATE caregiver SET username = ?, password = ?, firstname = ?, surname = ?, telephone = ? WHERE cid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setString(1, caregiver.getUsername());

            // Prüfen, ob das Passwort bereits verschlüsselt ist
            // Wenn das Passwort bereits in der DB existiert und nicht geändert wurde, ist es bereits verschlüsselt
            // Andernfalls müssen wir es neu verschlüsseln
            String currentPasswordInDb = getPasswordFromDatabase(caregiver.getCid());
            System.out.println("Aktuelles Passwort in DB: " + (currentPasswordInDb != null ? "[verschlüsselt]" : "null"));
            System.out.println("Neues Passwort: " + (caregiver.getPassword() != null ? "[vorhanden]" : "null"));

            // Immer verschlüsseln, wenn ein neues Passwort gesetzt wird
            preparedStatement.setString(2, encryptPassword(caregiver.getPassword()));
            System.out.println("Passwort wurde verschlüsselt und wird aktualisiert.");

            preparedStatement.setString(3, caregiver.getFirstName());
            preparedStatement.setString(4, caregiver.getSurname());
            preparedStatement.setString(5, caregiver.getTelephone());
            preparedStatement.setLong(6, caregiver.getCid());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Holt das aktuelle Passwort eines Caregivers aus der Datenbank
     * @param cid Die ID des Caregivers
     * @return Das verschlüsselte Passwort oder null
     */
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

    @Override
    public void createTable() {
        try {
            // Sicherstellen, dass die Connection nicht null ist
            if (connection == null) {
                System.err.println("FEHLER: Connection ist null in createTable");
                connection = ConnectionBuilder.getConnection();
                if (connection == null) {
                    throw new SQLException("Konnte keine Datenbankverbindung herstellen");
                }
            }

            Statement st = connection.createStatement();
            System.out.println("Prüfe, ob Tabelle 'caregiver' existiert...");

            // Prüfen, ob die Tabelle bereits existiert
            ResultSet tables = connection.getMetaData().getTables(null, null, "caregiver", null);
            boolean tableExists = tables.next();
            tables.close();

            if (!tableExists) {
                System.out.println("Tabelle 'caregiver' existiert nicht. Erstelle neu...");
                // Tabelle erstellen, wenn sie nicht existiert
                String createTableSQL = "CREATE TABLE IF NOT EXISTS caregiver (" +
                        "cid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT, " +
                        "password TEXT, " +
                        "firstname TEXT NOT NULL, " +
                        "surname TEXT NOT NULL, " +
                        "telephone TEXT)";

                System.out.println("Führe SQL aus: " + createTableSQL);
                st.executeUpdate(createTableSQL);
                System.out.println("Tabelle 'caregiver' erfolgreich erstellt.");
            } else {
                System.out.println("Tabelle 'caregiver' existiert bereits.");
                // Prüfen, ob die Spalten username und password existieren
                try {
                    ResultSet columns = connection.getMetaData().getColumns(null, null, "caregiver", "username");
                    boolean usernameExists = columns.next();
                    columns.close();

                    if (!usernameExists) {
                        System.out.println("Spalten 'username' und 'password' fehlen. Füge hinzu...");
                        // Spalten hinzufügen, wenn sie nicht existieren
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN username TEXT");
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN password TEXT");

                        // Bestehende Datensätze mit Standardwerten füllen
                        st.executeUpdate("UPDATE caregiver SET username = 'caregiver_' || cid, password = '' WHERE username IS NULL");
                        System.out.println("Spalten erfolgreich hinzugefügt und mit Standardwerten gefüllt.");
                    }
                } catch (SQLException e) {
                    System.out.println("Fehler beim Prüfen der Spalten: " + e.getMessage());
                    // Falls die Spalte nicht existiert oder ein anderer Fehler auftritt, versuchen wir die Spalten hinzuzufügen
                    try {
                        System.out.println("Versuche, Spalten 'username' und 'password' hinzuzufügen...");
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN username TEXT UNIQUE");
                        st.executeUpdate("ALTER TABLE caregiver ADD COLUMN password TEXT");

                        // Bestehende Datensätze mit Standardwerten füllen
                        st.executeUpdate("UPDATE caregiver SET username = 'caregiver_' || cid, password = '' WHERE username IS NULL");
                        System.out.println("Spalten erfolgreich hinzugefügt und mit Standardwerten gefüllt.");
                    } catch (SQLException ex) {
                        System.err.println("Fehler beim Hinzufügen der Spalten: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }

            st.close();
            System.out.println("Tabelle 'caregiver' ist bereit.");

                // Anmerkung: Anmeldedaten werden in der User-Tabelle gespeichert
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
            // Zuerst holen wir den Caregiver basierend auf dem Benutzernamen
            PreparedStatement st = connection.prepareStatement(
                "SELECT * FROM caregiver WHERE username = ?"
            );
            st.setString(1, username);

            ResultSet rs = st.executeQuery();
            Caregiver caregiver = null;

            if (rs.next()) {
                caregiver = getInstanceFromResultSet(rs);

                // Überprüfe, ob das verschlüsselte eingegebene Passwort mit dem gespeicherten übereinstimmt
                String encryptedPassword = encryptPassword(password);
                if (!encryptedPassword.equals(caregiver.getPassword())) {
                    // Passwort stimmt nicht überein
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