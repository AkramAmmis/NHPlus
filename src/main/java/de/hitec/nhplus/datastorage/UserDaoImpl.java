package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.User;
import de.hitec.nhplus.model.UserRole;
import de.hitec.nhplus.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementierung der UserDao-Schnittstelle
 */
public class UserDaoImpl extends DaoImp<User> implements UserDao {

    /**
     * Konstruktor mit Datenbankverbindung
     * @param connection Die zu verwendende Datenbankverbindung
     */
    public UserDaoImpl(Connection connection) {
        super(connection);
    }

    @Override
    protected PreparedStatement getCreateStatement(User user) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "INSERT INTO users (username, password, first_name, last_name, email, phone_number, role) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = this.connection.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, encryptPassword(user.getPassword()));
            preparedStatement.setString(3, user.getFirstName());
            preparedStatement.setString(4, user.getLastName());
            preparedStatement.setString(5, user.getEmail());
            preparedStatement.setString(6, user.getPhoneNumber());
            preparedStatement.setString(7, user.getRole().name());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Verschlüsselt das Passwort
     * @param password Das zu verschlüsselnde Passwort
     * @return Das verschlüsselte Passwort
     */
    private String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }
        return PasswordUtils.hashPassword(password);
    }

    @Override
    protected PreparedStatement getReadByIDStatement(long key) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "SELECT * FROM users WHERE uid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, key);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    @Override
    protected User getInstanceFromResultSet(ResultSet result) throws SQLException {
        User user = new User(
                result.getLong("uid"),
                result.getString("username"),
                result.getString("password"),
                result.getString("first_name"),
                result.getString("last_name"),
                result.getString("email"),
                result.getString("phone_number"),
                UserRole.valueOf(result.getString("role"))
        );

        // Versuche, die caregiver_id zu lesen, falls vorhanden
        try {
            user.setCaregiverId(result.getLong("caregiver_id"));
        } catch (SQLException e) {
            // Spalte existiert möglicherweise nicht in älteren Datenbanken
            user.setCaregiverId(0);
        }

        return user;
    }

    @Override
    protected PreparedStatement getReadAllStatement() {
        PreparedStatement statement = null;
        try {
            final String SQL = "SELECT * FROM users";
            statement = this.connection.prepareStatement(SQL);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return statement;
    }

    @Override
    protected ArrayList<User> getListFromResultSet(ResultSet result) throws SQLException {
        ArrayList<User> list = new ArrayList<>();
        while (result.next()) {
            list.add(getInstanceFromResultSet(result));
        }
        return list;
    }

    @Override
    protected PreparedStatement getUpdateStatement(User user) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "UPDATE users SET username = ?, password = ?, first_name = ?, " +
                              "last_name = ?, email = ?, phone_number = ?, role = ? WHERE uid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setString(1, user.getUsername());

            // Prüfen, ob das Passwort bereits verschlüsselt ist
            String currentPasswordInDb = getPasswordFromDatabase(user.getUid());
            if (currentPasswordInDb != null && currentPasswordInDb.equals(user.getPassword())) {
                // Das Passwort wurde nicht geändert - verwende es wie es ist
                preparedStatement.setString(2, user.getPassword());
            } else {
                // Das Passwort wurde geändert oder ist neu - verschlüsseln
                preparedStatement.setString(2, encryptPassword(user.getPassword()));
            }

            preparedStatement.setString(3, user.getFirstName());
            preparedStatement.setString(4, user.getLastName());
            preparedStatement.setString(5, user.getEmail());
            preparedStatement.setString(6, user.getPhoneNumber());
            preparedStatement.setString(7, user.getRole().name());
            preparedStatement.setLong(8, user.getUid());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Holt das aktuelle Passwort eines Benutzers aus der Datenbank
     * @param uid Die ID des Benutzers
     * @return Das verschlüsselte Passwort oder null
     */
    private String getPasswordFromDatabase(long uid) {
        try {
            PreparedStatement st = connection.prepareStatement("SELECT password FROM users WHERE uid = ?");
            st.setLong(1, uid);

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
            final String SQL = "DELETE FROM users WHERE uid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, key);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    @Override
    public void createTable() {
        try {
            Statement st = connection.createStatement();

            // Prüfen, ob die Tabelle bereits existiert
            ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users';");
            boolean tableExists = rs.next();
            rs.close();

            if (!tableExists) {
                // Tabelle neu erstellen
                st.executeUpdate("CREATE TABLE users (" +
                                "uid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "username TEXT UNIQUE NOT NULL, " +
                                "password TEXT NOT NULL, " +
                                "first_name TEXT NOT NULL, " +
                                "last_name TEXT NOT NULL, " +
                                "email TEXT, " +
                                "phone_number TEXT, " +
                                "role TEXT NOT NULL, " +
                                "caregiver_id BIGINT DEFAULT 0)");

                System.out.println("Benutzer-Tabelle 'users' wurde erstellt");

                // Admin-Benutzer erstellen
                createAdminUser();
            }

            st.close();
        } catch (SQLException e) {
            System.err.println("Fehler beim Erstellen der Benutzer-Tabelle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Erstellt einen Administrator-Benutzer in der Datenbank
     */
    private void createAdminUser() {
        try {
            System.out.println("Prüfe auf vorhandene Admin-Benutzer...");

            // Prüfen, ob bereits ein Admin existiert
            PreparedStatement checkAdmin = connection.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE role = ?");
            checkAdmin.setString(1, UserRole.ADMIN.name());
            ResultSet rs = checkAdmin.executeQuery();
            int adminCount = 0;
            if (rs.next()) {
                adminCount = rs.getInt(1);
            }
            rs.close();
            checkAdmin.close();

            if (adminCount == 0) {
                System.out.println("Kein Admin-Benutzer gefunden. Erstelle Standard-Admin...");

                // Admin-Benutzer erstellen
                User admin = new User(
                    "admin",
                    "admin123",
                    "Administrator",
                    "System",
                    "admin@nhplus.de",
                    "0",
                    UserRole.ADMIN
                );

                long adminId = create(admin);
                System.out.println("Admin-Benutzer erstellt mit ID: " + adminId);
            } else {
                System.out.println("Admin-Benutzer bereits vorhanden. Keine Aktion erforderlich.");
            }
        } catch (SQLException e) {
            System.err.println("Fehler beim Erstellen des Admin-Benutzers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public User authenticate(String username, String password) {
        try {
            // Prüfen, ob die users-Tabelle existiert
            Statement checkTable = connection.createStatement();
            ResultSet tables = checkTable.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users';");
            boolean usersTableExists = tables.next();
            tables.close();
            checkTable.close();

            if (!usersTableExists) {
                System.out.println("Tabelle 'users' existiert nicht. Wird erstellt...");
                createTable();
            }

            // Benutzer anhand des Benutzernamens suchen
            PreparedStatement st = connection.prepareStatement(
                "SELECT * FROM users WHERE username = ?");
            st.setString(1, username);

            ResultSet rs = st.executeQuery();
            User user = null;

            if (rs.next()) {
                user = getInstanceFromResultSet(rs);

                // Überprüfen, ob das verschlüsselte eingegebene Passwort mit dem gespeicherten übereinstimmt
                String encryptedPassword = encryptPassword(password);
                if (!encryptedPassword.equals(user.getPassword())) {
                    System.out.println("Passwort für Benutzer '" + username + "' stimmt nicht überein.");
                    // Passwort stimmt nicht überein
                    user = null;
                } else {
                    System.out.println("Benutzer '" + username + "' erfolgreich authentifiziert.");
                }
            } else {
                System.out.println("Benutzer '" + username + "' nicht gefunden.");
            }

            rs.close();
            st.close();
            return user;
        } catch (SQLException e) {
            System.err.println("SQL-Fehler bei der Authentifizierung: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("Unerwarteter Fehler bei der Authentifizierung: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public User createUser(User user) {
        try {
            long id = create(user);
            user.setUid(id);
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ArrayList<User> readAllUsers() {
        try {
            return (ArrayList<User>) readAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public User readUser(long uid) {
        try {
            return read(uid);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateUser(User user) {
        try {
            update(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(User user) {
        try {
            deleteById(user.getUid());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

        @Override
        public User findByUsername(String username) {
            try {
                // Prüfen, ob die users-Tabelle existiert
                Statement checkTable = connection.createStatement();
                ResultSet tables = checkTable.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users';");
                boolean usersTableExists = tables.next();
                tables.close();
                checkTable.close();

                if (!usersTableExists) {
                    System.out.println("Tabelle 'users' existiert nicht. Wird erstellt...");
                    createTable();
                }

                PreparedStatement st = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ?"
                );
                st.setString(1, username);

                ResultSet rs = st.executeQuery();
                User user = null;

                if (rs.next()) {
                    user = getInstanceFromResultSet(rs);
                    // Caregiver-ID direkt aus dem ResultSet holen
                    if (user != null) {
                        try {
                            user.setCaregiverId(rs.getLong("caregiver_id"));
                        } catch (SQLException e) {
                            System.out.println("Spalte 'caregiver_id' nicht gefunden: " + e.getMessage());
                            user.setCaregiverId(0); // Standardwert setzen
                        }
                    }
                } else {
                    System.out.println("Kein Benutzer mit Benutzernamen '" + username + "' gefunden.");
                }

                rs.close();
                st.close();
                return user;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

    /**
     * Migrationsmethode: Wandelt alte Caregiver-Daten in das neue User-Format um
     */
    public void migrateCaregiverToUsers() {
        try {
            // Prüfen, ob die alte Tabelle existiert
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='caregiver';");
            boolean caregiverTableExists = rs.next();
            rs.close();

            if (caregiverTableExists) {
                // Alle Caregiver auslesen
                rs = st.executeQuery("SELECT * FROM caregiver");
                List<User> usersToCreate = new ArrayList<>();

                while (rs.next()) {
                    String username;
                    String password;

                    try {
                        username = rs.getString("username");
                        password = rs.getString("password");
                    } catch (SQLException e) {
                        // Fallback für alte Daten ohne Login-Spalten
                        username = "caregiver_" + rs.getLong("cid");
                        password = "changeme";
                    }

                    // Leere Benutzernamen vermeiden
                    if (username == null || username.isEmpty()) {
                        username = "caregiver_" + rs.getLong("cid");
                    }

                    // Prüfen, ob bereits ein Benutzer mit diesem Namen existiert
                    PreparedStatement checkUser = connection.prepareStatement(
                        "SELECT COUNT(*) FROM users WHERE username = ?");
                    checkUser.setString(1, username);
                    ResultSet userCheck = checkUser.executeQuery();
                    int userCount = 0;
                    if (userCheck.next()) {
                        userCount = userCheck.getInt(1);
                    }
                    userCheck.close();
                    checkUser.close();

                    if (userCount == 0) {
                        // Neuen Benutzer erstellen
                        User user = new User(
                            username,
                            password, // Passwort ist bereits verschlüsselt
                            rs.getString("firstname"),
                            rs.getString("surname"),
                            "", // Keine Email in der alten Struktur
                            rs.getString("telephone"),
                            UserRole.CAREGIVER
                        );

                        // Verknüpfung zur Caregiver-ID herstellen
                        long caregiverId = rs.getLong("cid");
                        user.setCaregiverId(caregiverId);

                        usersToCreate.add(user);
                    }
                }
                rs.close();

                // Benutzer in die neue Tabelle einfügen
                for (User user : usersToCreate) {
                    PreparedStatement insertUser = connection.prepareStatement(
                        "INSERT INTO users (username, password, first_name, last_name, email, phone_number, role, caregiver_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    insertUser.setString(1, user.getUsername());
                    insertUser.setString(2, user.getPassword()); // Bereits verschlüsselt
                    insertUser.setString(3, user.getFirstName());
                    insertUser.setString(4, user.getLastName());
                    insertUser.setString(5, user.getEmail());
                    insertUser.setString(6, user.getPhoneNumber());
                    insertUser.setString(7, user.getRole().name());
                    insertUser.setLong(8, user.getCaregiverId());
                    insertUser.executeUpdate();
                    insertUser.close();
                }
            }

            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
