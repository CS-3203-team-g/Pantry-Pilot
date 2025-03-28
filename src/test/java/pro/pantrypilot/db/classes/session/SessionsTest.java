package pro.pantrypilot.db.classes.session;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test

@ExtendWith(MockitoExtension.class)
class SessionsTest {

    @Test
    void testConstructorWithUserIdAndIpAddress() {
        String userId = "test-user-id";
        String ipAddress = "127.0.0.1";

        Session session = new Session(userId, ipAddress);

        assertEquals(userId, session.getUserID());
        assertEquals(ipAddress, session.getIpAddress());
        assertNull(session.getSessionID());
        assertNull(session.getCreatedAt());
        assertNull(session.getLastUsed());
    }

    @Test
    void testConstructorWithResultSet() throws SQLException {
        ResultSet mockResultSet = mock(ResultSet.class);
        String sessionId = "test-session-id";
        String userId = "test-user-id";
        String ipAddress = "127.0.0.1";
        Timestamp now = new Timestamp(System.currentTimeMillis());

        when(mockResultSet.getString("sessionID")).thenReturn(sessionId);
        when(mockResultSet.getString("userID")).thenReturn(userId);
        when(mockResultSet.getString("ipAddress")).thenReturn(ipAddress);
        when(mockResultSet.getTimestamp("createdAt")).thenReturn(now);
        when(mockResultSet.getTimestamp("lastUsed")).thenReturn(now);

        Session session = new Session(mockResultSet);

        assertEquals(sessionId, session.getSessionID());
        assertEquals(userId, session.getUserID());
        assertEquals(ipAddress, session.getIpAddress());
        assertEquals(now, session.getCreatedAt());
        assertEquals(now, session.getLastUsed());
    }

    @Test
    void testSettersAndGetters() {
        Session session = new Session("user1", "192.168.1.1");

        session.setSessionID("new-session-id");
        assertEquals("new-session-id", session.getSessionID());

        session.setUserID("new-user-id");
        assertEquals("new-user-id", session.getUserID());

        session.setIpAddress("10.0.0.1");
        assertEquals("10.0.0.1", session.getIpAddress());

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        session.setCreatedAt(timestamp);
        assertEquals(timestamp, session.getCreatedAt());

        session.setLastUsed(timestamp);
        assertEquals(timestamp, session.getLastUsed());
    }
}