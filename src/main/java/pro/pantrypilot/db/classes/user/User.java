package pro.pantrypilot.db.classes.user;

import pro.pantrypilot.helpers.PasswordHasher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.sql.Timestamp;

public class User {
    private String userID;
    private String username;
    private String email;
    private String passwordHash;
    private String salt;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private boolean isActive;
    private String mfaSecret;
    private boolean mfaEnabled;

    // Constructor for new users (auto-generates userID)
    public User(String username, String email, String plaintextPassword) {
        this.userID = UUID.randomUUID().toString(); // Auto-generate UUID in Java
        this.username = username;
        this.email = email;

        PasswordHasher.Password password = PasswordHasher.generatePassword(username, plaintextPassword);
        this.passwordHash = password.getHash();
        this.salt = password.getSalt();

        this.isActive = true; // Default value
        this.createdAt = new Timestamp(System.currentTimeMillis()); // Set current timestamp
        this.lastLogin = null; // Not logged in yet
        this.mfaEnabled = false; // MFA disabled by default
        this.mfaSecret = null; // No secret initially
    }

    // Constructor for loading existing users from the database
    public User(String userID, String username, String email, String passwordHash,
                Timestamp createdAt, Timestamp lastLogin, boolean isActive) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
    }

    public User(ResultSet resultSet) {
        try {
            this.userID = resultSet.getString("userID");
            this.username = resultSet.getString("username");
            this.email = resultSet.getString("email");
            this.passwordHash = resultSet.getString("passwordHash");
            this.salt = resultSet.getString("salt");
            this.createdAt = resultSet.getTimestamp("createdAt");
            this.lastLogin = resultSet.getTimestamp("lastLogin");
            this.isActive = resultSet.getBoolean("isActive");
            this.mfaSecret = resultSet.getString("mfaSecret");
            this.mfaEnabled = resultSet.getBoolean("mfaEnabled");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error constructing User from ResultSet", e);
        }
    }


    // Getters and Setters
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    // Getters and Setters for MFA fields
    public String getMfaSecret() {
        return mfaSecret;
    }

    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }

    // Method to update last login timestamp
    public void updateLastLogin() {
        this.lastLogin = new Timestamp(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "User{" +
                "userID='" + userID + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", lastLogin=" + lastLogin +
                ", isActive=" + isActive +
                ", mfaEnabled=" + mfaEnabled +
                '}';
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
