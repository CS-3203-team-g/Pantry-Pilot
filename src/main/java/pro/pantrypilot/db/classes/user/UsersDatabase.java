package pro.pantrypilot.db.classes.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsersDatabase {

    private static final Logger logger = LoggerFactory.getLogger(UsersDatabase.class);

    public static void initializeUserDatabase(){
        String createUsersTableSQL = "CREATE TABLE IF NOT EXISTS users (\n"
                + "    userID CHAR(36) PRIMARY KEY DEFAULT (UUID()),\n"  // Auto-generate UUID
                + "    username VARCHAR(50) NOT NULL UNIQUE,\n"          // Unique usernames
                + "    email VARCHAR(100) NOT NULL UNIQUE,\n"            // Unique emails
                + "    passwordHash VARCHAR(255) NOT NULL,\n"            // BCrypt hashed password (contains salt)
                + "    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n" // Auto timestamp
                + "    lastLogin TIMESTAMP NULL DEFAULT NULL,\n"         // Nullable, updated on login
                + "    isActive BOOLEAN DEFAULT TRUE,\n"                 // Active status flag
                + "    isAdmin BOOLEAN DEFAULT FALSE\n"                  // Admin status flag
                + ");";
        try {
            DatabaseConnectionManager.getConnection().createStatement().execute(createUsersTableSQL);
            
            // Check if the isAdmin column exists and add it if it doesn't
            try {
                String checkColumnSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                                       "WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'isAdmin'";
                ResultSet rs = DatabaseConnectionManager.getConnection().createStatement().executeQuery(checkColumnSQL);
                
                if (rs.next() && rs.getInt(1) == 0) {
                    logger.info("Migrating database: Adding 'isAdmin' column to users table");
                    String addColumnSQL = "ALTER TABLE users ADD COLUMN isAdmin BOOLEAN DEFAULT FALSE";
                    DatabaseConnectionManager.getConnection().createStatement().execute(addColumnSQL);
                }
            } catch (SQLException e) {
                // This might fail on some databases that don't support INFORMATION_SCHEMA
                // It's not critical, so we just log and continue
                logger.warn("Could not check for or add isAdmin column", e);
            }

            // Check if the 'salt' column exists and drop it if it does
            // This is a migration step for existing databases
            try {
                String checkColumnSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                                       "WHERE TABLE_NAME = 'users' AND COLUMN_NAME = 'salt'";
                ResultSet rs = DatabaseConnectionManager.getConnection().createStatement().executeQuery(checkColumnSQL);
                
                if (rs.next() && rs.getInt(1) > 0) {
                    logger.info("Migrating database: Removing 'salt' column from users table");
                    String dropSaltColumnSQL = "ALTER TABLE users DROP COLUMN salt";
                    DatabaseConnectionManager.getConnection().createStatement().execute(dropSaltColumnSQL);
                }
            } catch (SQLException e) {
                // This might fail on some databases that don't support INFORMATION_SCHEMA
                // It's not critical, so we just log and continue
                logger.warn("Could not check for or remove salt column", e);
            }
        } catch (SQLException e) {
            logger.error("Error creating users table", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean createUser(User user) {
        String createUserSQL = "INSERT INTO users (username, email, passwordHash) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(createUserSQL)) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPasswordHash());
            
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Returns true if a row was inserted
        } catch (SQLException e) {
            logger.error("Error creating user", e);
            return false; // Returns false if an exception occurred
        }
    }

    public static User getUserByUsername(String username) {
        String getUserSQL = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(getUserSQL)) {
            preparedStatement.setString(1, username);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {  // Move cursor to the first row
                    return new User(resultSet);
                } else {
                    return null; // No user found with the given username
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving user", e);
            return null;
        }
    }

    public static User getUserByUserId(String username) {
        String getUserSQL = "SELECT * FROM users WHERE userID = ?";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(getUserSQL)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {  // Move cursor to the first row
                    return new User(resultSet);
                } else {
                    return null; // No user found with the given username
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving user", e);
            return null;
        }
    }

    public static boolean updateUserAdminStatus(String userID, boolean isAdmin) {
        String updateAdminSQL = "UPDATE users SET isAdmin = ? WHERE userID = ?";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(updateAdminSQL)) {
            preparedStatement.setBoolean(1, isAdmin);
            preparedStatement.setString(2, userID);
    
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error updating admin status for userID: " + userID, e);
            return false;
        }
    }

    public static boolean updateUserPassword(String userID, String newPasswordHash) {
        String updatePasswordSQL = "UPDATE users SET passwordHash = ? WHERE userID = ?";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(updatePasswordSQL)) {
            preparedStatement.setString(1, newPasswordHash);
            preparedStatement.setString(2, userID);
    
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Returns true if a row was updated
        } catch (SQLException e) {
            logger.error("Error updating password for userID: " + userID, e);
            return false; // Returns false if an exception occurred
        }
    }

    public static void updateUserLastLogin(String userID) {
        String updateLastLoginSQL = "UPDATE users SET lastLogin = CURRENT_TIMESTAMP WHERE userID = ?";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(updateLastLoginSQL)) {
            preparedStatement.setString(1, userID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating last login for userID: " + userID, e);
        }
    }

    public static int getTotalUsers(){
        String countUsersSQL = "SELECT COUNT(*) FROM users";
        try (ResultSet resultSet = DatabaseConnectionManager.getConnection().createStatement().executeQuery(countUsersSQL)) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            logger.error("Error counting users", e);
            return 0;
        }
    }

    public static void updateUserLastLogin(User user) {
        updateUserLastLogin(user.getUserID());
    }
}
