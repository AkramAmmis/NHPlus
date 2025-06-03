package de.hitec.nhplus.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class AccountLockService {
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    
    private Map<String, Integer> failedAttempts = new HashMap<>();
    private Map<String, LocalDateTime> lockoutTime = new HashMap<>();
    
    public boolean isAccountLocked(String username) {
        if (!lockoutTime.containsKey(username)) {
            return false;
        }
        
        LocalDateTime lockTime = lockoutTime.get(username);
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLock = ChronoUnit.MINUTES.between(lockTime, now);
        
        if (minutesSinceLock >= LOCKOUT_DURATION_MINUTES) {
      
            unlockAccount(username);
            System.out.println("DEBUG: Account " + username + " wurde automatisch entsperrt nach " + minutesSinceLock + " Minuten");
            return false;
        }
        
        System.out.println("DEBUG: Account " + username + " ist noch für " + (LOCKOUT_DURATION_MINUTES - minutesSinceLock) + " Minuten gesperrt");
        return true;
    }
    
    public void recordFailedAttempt(String username) {
        int attempts = failedAttempts.getOrDefault(username, 0) + 1;
        failedAttempts.put(username, attempts);
        
        System.out.println("DEBUG: Fehlgeschlagener Versuch #" + attempts + " für " + username);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(username);
        }
    }
    
    public void recordSuccessfulLogin(String username) {
        failedAttempts.remove(username);
        lockoutTime.remove(username);
        System.out.println("DEBUG: Erfolgreicher Login für " + username + " - Sperr-Zähler zurückgesetzt");
    }
    
    private void lockAccount(String username) {
        lockoutTime.put(username, LocalDateTime.now());
        System.out.println("DEBUG: Account " + username + " wurde gesperrt um " + LocalDateTime.now());
    }
    
    public void unlockAccount(String username) {
        failedAttempts.remove(username);
        lockoutTime.remove(username);
        System.out.println("DEBUG: Account " + username + " wurde manuell entsperrt");
    }
    
    public int getFailedAttempts(String username) {
        return failedAttempts.getOrDefault(username, 0);
    }
    
    public LocalDateTime getLockoutTime(String username) {
        return lockoutTime.get(username);
    }
}
