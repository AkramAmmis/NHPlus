package de.hitec.nhplus.datastorage;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    
    public static void initializeDatabase() {
        try (Connection connection = ConnectionBuilder.getConnection();
             Statement statement = connection.createStatement()) {
            
            System.out.println("Initialisiere Datenbank-Tabellen...");
            
            createCaregiverTable(statement);
            
            createUserTable(statement);
            
            createLoginLogTable(statement);
            
            System.out.println("Datenbank-Tabellen erfolgreich initialisiert.");
            
        } catch (SQLException e) {
            System.err.println("Fehler bei der Datenbankinitialisierung: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createCaregiverTable(Statement statement) throws SQLException {
        String createCaregiverTable = """
            CREATE TABLE IF NOT EXISTS caregiver (
                cid INTEGER PRIMARY KEY AUTOINCREMENT,
                firstname TEXT NOT NULL,
                surname TEXT NOT NULL,
                telephone TEXT,
                locked BOOLEAN DEFAULT 0,
                archived BOOLEAN DEFAULT 0,
                user_id INTEGER,
                FOREIGN KEY (user_id) REFERENCES users(uid)
            )
            """;
        statement.execute(createCaregiverTable);
        
        try {
            statement.execute("ALTER TABLE caregiver ADD COLUMN user_id INTEGER");
            System.out.println("user_id Spalte zur Caregiver-Tabelle hinzugefügt.");
        } catch (SQLException e) {
            if (!e.getMessage().contains("duplicate column")) {
                System.out.println("user_id Spalte existiert bereits in Caregiver-Tabelle.");
            }
        }
        
        System.out.println("Caregiver-Tabelle überprüft/erstellt.");
    }
    
    private static void createUserTable(Statement statement) throws SQLException {
        String createUserTable = """
            CREATE TABLE IF NOT EXISTS users (
                uid INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'USER',
                locked BOOLEAN DEFAULT 0,
                failed_attempts INTEGER DEFAULT 0,
                last_failed_attempt DATETIME,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;
        statement.execute(createUserTable);
        System.out.println("Users-Tabelle überprüft/erstellt.");
    }
    
    private static void createLoginLogTable(Statement statement) throws SQLException {
        String createLoginLogTable = """
            CREATE TABLE IF NOT EXISTS login_log (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                ip_address TEXT,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                successful BOOLEAN NOT NULL,
                failure_reason TEXT
            )
            """;
        statement.execute(createLoginLogTable);
        System.out.println("Login-Log-Tabelle überprüft/erstellt.");
    }
}
