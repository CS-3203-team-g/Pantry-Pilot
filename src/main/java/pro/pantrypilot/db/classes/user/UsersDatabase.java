package pro.pantrypilot.db.classes.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UsersDatabase {

    private static final Logger logger = LoggerFactory.getLogger(UsersDatabase.class);

    public static void initializeUserDatabase(){

        String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users (\n"
                + "    userID CHAR(36) PRIMARY KEY DEFAULT (UUID()),\n"  // Auto-generate UUID
                + "    username VARCHAR(50) NOT NULL UNIQUE,\n"          // Unique usernames
                + "    email VARCHAR(100) NOT NULL UNIQUE,\n"            // Unique emails
                + "    passwordHash VARCHAR(255) NOT NULL,\n"            // Hashed password
                + "    salt VARCHAR(255) NOT NULL UNIQUE,\n"                    // Salt for password hashing
                + "    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" // Auto timestamp
                + "    lastLogin TIMESTAMP NULL DEFAULT NULL,\n"         // Nullable, updated on login
                + "    isActive BOOLEAN DEFAULT TRUE,\n"                  // Active status flag
                + "    mfaSecret VARCHAR(255) NULL DEFAULT NULL,\n"     // Added for MFA
                + "    mfaEnabled BOOLEAN DEFAULT FALSE\n"         // Added for MFA
                + ");";
        try {
            DatabaseConnectionManager.getConnection().createStatement().execute(createUsersTableSQL);
        } catch (SQLException e) {
            logger.error("Error creating users table", e);
            throw new RuntimeException(e);
        }

    }

    public static boolean createUser(User user) {
        String createUserSQL = "INSERT INTO users (username, email, passwordHash, salt) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(createUserSQL)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPasswordHash());
            preparedStatement.setString(4, user.getSalt());
            // mfaSecret defaults to NULL and mfaEnabled defaults to FALSE in the DB schema

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Returns true if a row was inserted
        } catch (SQLException e) {
            logger.error("Error creating user: " + user.getUsername(), e);
            return false; // Returns false if an exception occurred
        }
    }


    public static User getUser(String username) {
        String getUserSQL = "SELECT * FROM users WHERE username = ?"; // Use PreparedStatement
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(getUserSQL)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {  // Move cursor to the first row
                    return new User(resultSet); // Constructor now handles all fields including MFA
                } else {
                    return null; // No user found with the given username
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving user: " + username, e);
            return null;
        }
    }

    public static User getUserById(String userId) {
        String getUserSQL = "SELECT * FROM users WHERE userID = ?";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(getUserSQL)) {
            preparedStatement.setString(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(resultSet); // Assumes User constructor handles ResultSet
                } else {
                    return null; // No user found with the given userID
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving user by ID: " + userId, e);
            return null;
        }
    }

    public static boolean updateUserPassword(String userID, String newPasswordHash, String newSalt) {
        String updatePasswordSQL = "UPDATE users SET passwordHash = ?, salt = ? WHERE userID = ?";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(updatePasswordSQL)) {
            preparedStatement.setString(1, newPasswordHash);
            preparedStatement.setString(2, newSalt);
            preparedStatement.setString(3, userID);
    

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Returns true if a row was updated
        } catch (SQLException e) {
            logger.error("Error updating password for userID: " + userID, e);
            return false; // Returns false if an exception occurred
        }
    }


}
