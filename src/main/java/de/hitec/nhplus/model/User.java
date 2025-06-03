package de.hitec.nhplus.model;

public class User {
    private long uid;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private UserRole role;
    private boolean locked;
    private int failedAttempts;
    private long caregiverId;
    private long lockUntil;

    public User() {}

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.locked = false;
        this.failedAttempts = 0;
        this.caregiverId = 0;
        this.lockUntil = 0;
    }

    public User(String username, String password, String firstName, String lastName, String email, String phoneNumber, UserRole role) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.locked = false;
        this.failedAttempts = 0;
        this.caregiverId = 0;
        this.lockUntil = 0;
    }

    public User(long uid, String username, String password, String firstName, String lastName, String email, String phoneNumber, UserRole role) {
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.locked = false;
        this.failedAttempts = 0;
        this.caregiverId = 0;
        this.lockUntil = 0;
    }

    public long getUid() { return uid; }
    public void setUid(long uid) { this.uid = uid; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public long getCaregiverId() { return caregiverId; }
    public void setCaregiverId(long caregiverId) { this.caregiverId = caregiverId; }

    public long getLockUntil() { return lockUntil; }
    public void setLockUntil(long lockUntil) { this.lockUntil = lockUntil; }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }

    public boolean isCaregiver() {
        return UserRole.CAREGIVER.equals(this.role);
    }

    public String getFullName() {
        if (firstName != null && !firstName.trim().isEmpty() && lastName != null && !lastName.trim().isEmpty()) {
            return firstName + " " + lastName;
        }
        return username;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role +
                '}';
    }
}
