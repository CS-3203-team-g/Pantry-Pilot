package pro.pantrypilot.db.classes.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionsDatabase {

    private static final Logger logger = LoggerFactory.getLogger(SessionsDatabase.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final long EXPIRATION_DAYS = 14;

    static {
        // Schedule session cleanup to run every hour
        scheduler.scheduleAtFixedRate(SessionsDatabase::deleteExpiredSessions, 1, 1, TimeUnit.HOURS);
    }

    /**
     * Initializes the sessions table in the database.
     *
     * The sessions table includes:
     * - sessionID: a unique identifier for the session (stored as a cookie in the browser)
     * - userID: a foreign key referencing the user that owns the session
     * - createdAt: timestamp when the session was created
     * - lastUsed: timestamp when the session was last used
     * - ipAddress: the IP address from which the session was initiated
     */
    public static void initializeSessionsDatabase() {
        String createSessionsTableSQL = "CREATE TABLE IF NOT EXISTS sessions (\n"
                + "    sessionID CHAR(36) PRIMARY KEY DEFAULT (UUID()),\n"   // Auto-generate UUID for session
                + "    userID CHAR(36) NOT NULL,\n"                           // Reference to the user owning the session
                + "    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"    // Session creation time
                + "    lastUsed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"     // Last time the session was active
                + "    ipAddress VARCHAR(45),\n"                              // Client IP address (supports IPv6)
                + "    FOREIGN KEY (userID) REFERENCES users(userID) ON DELETE CASCADE\n"
                + ");";
        try {
            Statement stmt = DatabaseConnectionManager.getConnection().createStatement();
            stmt.execute(createSessionsTableSQL);
        } catch (SQLException e) {
            logger.error("Error creating sessions table", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Inserts a new session record into the sessions table.
     *
     * The createdAt and lastUsed fields are automatically set via DEFAULT values.
     *
     * @param session The Session object containing data to be stored.
     * @return true if the session was successfully created; false otherwise.
     */

    public static Session createSession(Session session) {
        String sessionID = UUID.randomUUID().toString();
        String createSessionSQL = "INSERT INTO sessions (sessionID, userID, ipAddress) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(createSessionSQL)) {
            preparedStatement.setString(1, sessionID);
            preparedStatement.setString(2, session.getUserID());
            preparedStatement.setString(3, session.getIpAddress());
            
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                return getSession(sessionID);
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error creating session", e);
            return null;
        }
    }

    /**
     * Retrieves a session record from the database using the sessionID.
     *
     * @param sessionID The unique session ID.
     * @return A Session object if found; otherwise, null.
     */
    public static Session getSession(String sessionID) {
        String getSessionSQL = "SELECT * FROM sessions WHERE sessionID = ?";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(getSessionSQL)) {
            preparedStatement.setString(1, sessionID);
            
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {  // Move cursor to the first row
                    return new Session(resultSet);
                } else {
                    return null; // No session found with the given sessionID
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving session", e);
            return null;
        }
    }

    /**
     * Updates the 'lastUsed' timestamp for a given session to the current time.
     *
     * This method can be called each time the user performs an action to keep the session alive.
     *
     * @param sessionID The unique session ID.
     * @return true if the update was successful; false otherwise.
     */
    public static boolean updateLastUsed(String sessionID) {
        String updateSQL = "UPDATE sessions SET lastUsed = CURRENT_TIMESTAMP WHERE sessionID = ?";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(updateSQL)) {
            preparedStatement.setString(1, sessionID);
            
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error updating lastUsed timestamp", e);
            return false;
        }
    }

    /**
     * Deletes a session record from the sessions table.
     *
     * This can be used to log a user out or clear expired sessions.
     *
     * @param sessionID The unique session ID.
     * @return true if the deletion was successful; false otherwise.
     */
    public static boolean deleteSession(String sessionID) {
        String deleteSQL = "DELETE FROM sessions WHERE sessionID = ?";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(deleteSQL)) {
            preparedStatement.setString(1, sessionID);
            
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error deleting session", e);
            return false;
        }
    }

    /**
     * Deletes sessions that have been inactive for more than EXPIRATION_DAYS days
     */
    public static void deleteExpiredSessions() {
        String deleteSQL = "DELETE FROM sessions WHERE lastUsed < DATE_SUB(NOW(), INTERVAL " + EXPIRATION_DAYS + " DAY)";
        try (Statement stmt = DatabaseConnectionManager.getConnection().createStatement()) {
            int rowsAffected = stmt.executeUpdate(deleteSQL);
            if (rowsAffected > 0) {
                logger.info("Deleted {} expired sessions", rowsAffected);
            }
        } catch (SQLException e) {
            logger.error("Error deleting expired sessions", e);
        }
    }
}
