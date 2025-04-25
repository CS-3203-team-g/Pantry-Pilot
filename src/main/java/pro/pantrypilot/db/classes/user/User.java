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
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private boolean isActive;
    private boolean isAdmin;

    // Constructor for new users (auto-generates userID)
    public User(String username, String email, String plaintextPassword) {
        this.userID = UUID.randomUUID().toString();
        this.username = username;
        this.email = email;
        this.passwordHash = PasswordHasher.generatePassword(plaintextPassword);
        this.isActive = true;
        this.isAdmin = false;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.lastLogin = null;
    }

    // Constructor for loading existing users from the database
    public User(String userID, String username, String email, String passwordHash,
                Timestamp createdAt, Timestamp lastLogin, boolean isActive, boolean isAdmin) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.isActive = isActive;
        this.isAdmin = isAdmin;
    }

    public User(ResultSet resultSet) {
        try {
            this.userID = resultSet.getString("userID");
            this.username = resultSet.getString("username");
            this.email = resultSet.getString("email");
            this.passwordHash = resultSet.getString("passwordHash");
            this.createdAt = resultSet.getTimestamp("createdAt");
            this.lastLogin = resultSet.getTimestamp("lastLogin");
            this.isActive = resultSet.getBoolean("isActive");
            this.isAdmin = resultSet.getBoolean("isAdmin");
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

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
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
                ", isAdmin=" + isAdmin +
                '}';
    }
}
