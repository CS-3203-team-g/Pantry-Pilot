package pro.pantrypilot.db.classes.session;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.pantrypilot.db.DatabaseConnectionManager;
import pro.pantrypilot.helpers.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionsDatabaseTest {
    
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize the test database
        SessionsDatabase.initializeSessionsDatabase();
        connection = DatabaseConnectionManager.getConnection();
        
        // Generate a proper hashed password using PasswordHasher
        String testPassword = PasswordHasher.generatePassword("testpassword");
        
        // Create a test user since sessions need a valid user reference
        // Using PreparedStatement to avoid SQL injection and properly handle the BCrypt hash
        String createTestUserSQL = "INSERT INTO users (userID, username, email, passwordHash) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(createTestUserSQL)) {
            pstmt.setString(1, "test-user-id");
            pstmt.setString(2, "testuser");
            pstmt.setString(3, "test@example.com");
            pstmt.setString(4, testPassword);
            pstmt.executeUpdate();
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up test data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM sessions WHERE userID = 'test-user-id'");
            stmt.execute("DELETE FROM users WHERE userID = 'test-user-id'");
        }
    }

    @Test
    void testCreateAndGetSession() {
        Session newSession = new Session("test-user-id", "127.0.0.1");
        Session createdSession = SessionsDatabase.createSession(newSession);
        
        assertNotNull(createdSession, "Created session should not be null");
        assertNotNull(createdSession.getSessionID(), "Session ID should be generated");
        assertEquals("test-user-id", createdSession.getUserID());
        assertEquals("127.0.0.1", createdSession.getIpAddress());
        assertNotNull(createdSession.getCreatedAt(), "Created timestamp should be set");
        assertNotNull(createdSession.getLastUsed(), "Last used timestamp should be set");
        
        // Test retrieving the session
        Session retrievedSession = SessionsDatabase.getSession(createdSession.getSessionID());
        assertNotNull(retrievedSession, "Retrieved session should not be null");
        assertEquals(createdSession.getSessionID(), retrievedSession.getSessionID());
        assertEquals(createdSession.getUserID(), retrievedSession.getUserID());
        assertEquals(createdSession.getIpAddress(), retrievedSession.getIpAddress());
    }

    @Test
    void testUpdateLastUsed() {
        Session newSession = new Session("test-user-id", "127.0.0.1");
        Session createdSession = SessionsDatabase.createSession(newSession);
        
        // Record the initial last used time
        long initialLastUsed = createdSession.getLastUsed().getTime();
        
        // Wait a brief moment to ensure timestamp will be different
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean updated = SessionsDatabase.updateLastUsed(createdSession.getSessionID());
        assertTrue(updated, "Session update should succeed");
        
        Session updatedSession = SessionsDatabase.getSession(createdSession.getSessionID());
        assertNotNull(updatedSession);
        assertTrue(
            updatedSession.getLastUsed().getTime() > initialLastUsed,
            "Last used timestamp should be updated"
        );
    }

    @Test
    void testDeleteSession() {
        Session newSession = new Session("test-user-id", "127.0.0.1");
        Session createdSession = SessionsDatabase.createSession(newSession);
        
        boolean deleted = SessionsDatabase.deleteSession(createdSession.getSessionID());
        assertTrue(deleted, "Session deletion should succeed");
        
        Session retrievedSession = SessionsDatabase.getSession(createdSession.getSessionID());
        assertNull(retrievedSession, "Session should not exist after deletion");
    }

    @Test
    void testGetNonExistentSession() {
        String nonExistentSessionId = UUID.randomUUID().toString();
        Session session = SessionsDatabase.getSession(nonExistentSessionId);
        assertNull(session, "Non-existent session should return null");
    }

    @Test
    void testUpdateNonExistentSession() {
        String nonExistentSessionId = UUID.randomUUID().toString();
        boolean updated = SessionsDatabase.updateLastUsed(nonExistentSessionId);
        assertFalse(updated, "Updating non-existent session should return false");
    }

    @Test
    void testDeleteNonExistentSession() {
        String nonExistentSessionId = UUID.randomUUID().toString();
        boolean deleted = SessionsDatabase.deleteSession(nonExistentSessionId);
        assertFalse(deleted, "Deleting non-existent session should return false");
    }

    @Test
    void testDeleteExpiredSessions() throws Exception {
        // Create a session
        Session newSession = new Session("test-user-id", "127.0.0.1");
        Session createdSession = SessionsDatabase.createSession(newSession);
        assertNotNull(createdSession);

        // Manually update the lastUsed timestamp to be older than expiration
        String updateSQL = "UPDATE sessions SET lastUsed = DATE_SUB(NOW(), INTERVAL 15 DAY) WHERE sessionID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateSQL)) {
            pstmt.setString(1, createdSession.getSessionID());
            pstmt.executeUpdate();
        }

        // Run expiration
        SessionsDatabase.deleteExpiredSessions();

        // Verify session was deleted
        Session retrievedSession = SessionsDatabase.getSession(createdSession.getSessionID());
        assertNull(retrievedSession, "Expired session should be deleted");
    }
}