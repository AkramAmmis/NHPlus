package de.hitec.nhplus.datastorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DefaultUserSetup {
    
    public static void createDefaultAdminUser() {
        try (Connection connection = ConnectionBuilder.getConnection()) {
            
            // Prüfen ob bereits ein Admin existiert
            if (adminUserExists(connection)) {
                System.out.println("Admin-Benutzer bereits vorhanden.");
                return;
            }
            
            // Standard-Admin erstellen
            createAdminUser(connection, "admin", "admin123", "ADMIN");
            System.out.println("Standard-Admin erstellt:");
            System.out.println("Benutzername: admin");
            System.out.println("Passwort: admin123");
            System.out.println("WICHTIG: Bitte ändern Sie das Passwort nach dem ersten Login!");
            
        } catch (SQLException e) {
            System.err.println("Fehler beim Erstellen des Admin-Benutzers: " + e.getMessage());
        }
    }
    
    private static boolean adminUserExists(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user WHERE role = 'ADMIN'";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    private static void createAdminUser(Connection connection, String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO user (username, password, role, locked, failed_attempts) VALUES (?, ?, ?, 0, 0)";
        
        String hashedPassword = hashPassword(password);
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            stmt.executeUpdate();
        }
    }
    
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Fehler beim Hashen des Passworts: " + e.getMessage());
            return password; // Fallback (unsicher!)
        }
    }
    
    public static void listAllUsers() {
        try (Connection connection = ConnectionBuilder.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT username, role, locked FROM user");
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("\n=== Alle Benutzer ===");
            while (rs.next()) {
                System.out.printf("Benutzer: %s | Rolle: %s | Gesperrt: %s%n",
                    rs.getString("username"),
                    rs.getString("role"),
                    rs.getBoolean("locked") ? "Ja" : "Nein"
                );
            }
            System.out.println("==================\n");
            
        } catch (SQLException e) {
            System.err.println("Fehler beim Auflisten der Benutzer: " + e.getMessage());
        }
    }
}
