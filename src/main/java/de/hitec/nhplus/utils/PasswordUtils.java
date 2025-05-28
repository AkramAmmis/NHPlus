package de.hitec.nhplus.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Hilfsklasse für Passwort-Operationen
 */
public class PasswordUtils {

    /**
     * Generiert ein zufälliges, sicheres Passwort
     * @param length Länge des zu generierenden Passworts
     * @return Ein zufälliges Passwort
     */
    public static String generateSecurePassword(int length) {
        final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        final String NUMBER = "0123456789";
        final String OTHER_CHAR = "!@#$%&*()_+-=[]|,./?><";
        final String ALL_ALLOWED_CHARS = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        // Sicherstellen, dass mindestens ein Zeichen aus jeder Kategorie enthalten ist
        password.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        password.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        password.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        password.append(OTHER_CHAR.charAt(random.nextInt(OTHER_CHAR.length())));

        // Auffüllen mit zufälligen Zeichen
        for (int i = 4; i < length; i++) {
            password.append(ALL_ALLOWED_CHARS.charAt(random.nextInt(ALL_ALLOWED_CHARS.length())));
        }

        // Durchmischen des Passworts
        char[] pwdChars = password.toString().toCharArray();
        for (int i = pwdChars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = pwdChars[i];
            pwdChars[i] = pwdChars[j];
            pwdChars[j] = temp;
        }

        return new String(pwdChars);
    }

    /**
     * Prüft die Stärke eines Passworts
     * @param password Das zu prüfende Passwort
     * @return true wenn das Passwort stark genug ist, sonst false
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasLower = false;
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        return hasLower && hasUpper && hasDigit && hasSpecial;
    }

    /**
     * Verschlüsselt ein Passwort mit SHA-256
     * @param password Das zu verschlüsselnde Passwort
     * @return Das verschlüsselte Passwort als Hex-String
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Fehler beim Hashen des Passworts", e);
        }
    }
}
