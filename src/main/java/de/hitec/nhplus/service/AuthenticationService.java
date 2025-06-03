package de.hitec.nhplus.service;

public class AuthenticationService {
    private AccountLockService lockService = new AccountLockService();
    private LoginLogService loginLogService = new LoginLogService();
    
    public AuthenticationResult authenticate(String username, String password, String ipAddress) {
        System.out.println("DEBUG: Authentifizierungsversuch für: " + username);
        
        // Erst prüfen ob Account gesperrt ist
        if (lockService.isAccountLocked(username)) {
            String reason = "Account temporär gesperrt";
            loginLogService.logLoginAttempt(username, ipAddress, false, reason);
            return new AuthenticationResult(false, reason);
        }
        
        // Dann Anmeldedaten prüfen
        boolean credentialsValid = validateCredentials(username, password);
        System.out.println("DEBUG: Anmeldedaten gültig: " + credentialsValid);
        
        if (credentialsValid) {
            lockService.recordSuccessfulLogin(username);
            loginLogService.logLoginAttempt(username, ipAddress, true, null);
            return new AuthenticationResult(true, "Login erfolgreich");
        } else {
            lockService.recordFailedAttempt(username);
            String reason = "Ungültige Anmeldedaten";
            loginLogService.logLoginAttempt(username, ipAddress, false, reason);
            return new AuthenticationResult(false, reason);
        }
    }
    
    private boolean validateCredentials(String username, String password) {
        // Hier würde die eigentliche Passwort-Validierung stattfinden
        // Placeholder für die bestehende Logik
        System.out.println("DEBUG: Validiere Anmeldedaten für " + username);
        
        // TODO: Implementieren Sie hier Ihre bestehende Passwort-Validierung
        // return userDao.validatePassword(username, password);
        return false; // Placeholder
    }
    
    public void unlockAccount(String username) {
        lockService.unlockAccount(username);
    }
    
    public static class AuthenticationResult {
        private final boolean successful;
        private final String message;
        
        public AuthenticationResult(boolean successful, String message) {
            this.successful = successful;
            this.message = message;
        }
        
        public boolean isSuccessful() { return successful; }
        public String getMessage() { return message; }
    }
}
