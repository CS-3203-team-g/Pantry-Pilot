package pro.pantrypilot.endpoints.api.login;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.pantrypilot.db.classes.session.SessionsDatabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogoutTest {

    private Logout logoutHandler;

    @Mock
    private HttpExchange exchange;

    @Mock
    private Headers headers;

    @BeforeEach
    void setUp() {
        logoutHandler = new Logout();
        // No more header setup here - we'll do it in each test
    }

    @Test
    void testInvalidRequestMethod() throws IOException {
        when(exchange.getRequestMethod()).thenReturn("GET");
        logoutHandler.handle(exchange);
        verify(exchange, times(1)).sendResponseHeaders(405, -1);
    }

    @Test
    void testValidLogoutRequest() throws IOException {
        // Setup
        String sessionID = "test-session-id";
        ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();

        // Set up request and response
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
        when(exchange.getResponseBody()).thenReturn(responseOutputStream);
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst("Cookie")).thenReturn("sessionID=" + sessionID);

        // Mock SessionsDatabase.deleteSession to return true
        try (MockedStatic<SessionsDatabase> mockedStaticDB = Mockito.mockStatic(SessionsDatabase.class)) {
            mockedStaticDB.when(() -> SessionsDatabase.deleteSession(sessionID)).thenReturn(true);
            logoutHandler.handle(exchange);
        }

        // Verify headers and response
        verify(headers, times(1)).add(eq("Set-Cookie"),
                eq("sessionID=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; HttpOnly; SameSite=Strict"));
        verify(headers, times(1)).add(eq("Set-Cookie"),
                eq("username=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; SameSite=Strict"));
        verify(headers, times(1)).set(eq("Content-Type"), eq("application/json; charset=UTF-8"));
        verify(exchange, times(1)).sendResponseHeaders(200, 0);
    }

    @Test
    void testInvalidSessionID() throws IOException {
        // Setup
        String sessionID = "invalid-session-id";
        ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();

        // Set up request and response
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
        when(exchange.getResponseBody()).thenReturn(responseOutputStream);
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst("Cookie")).thenReturn("sessionID=" + sessionID);

        // Mock SessionsDatabase.deleteSession to return false
        try (MockedStatic<SessionsDatabase> mockedStaticDB = Mockito.mockStatic(SessionsDatabase.class)) {
            mockedStaticDB.when(() -> SessionsDatabase.deleteSession(sessionID)).thenReturn(false);
            logoutHandler.handle(exchange);
        }

        // Verify headers and response
        verify(headers, times(1)).add(eq("Set-Cookie"),
                eq("sessionID=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; HttpOnly; SameSite=Strict"));
        verify(headers, times(1)).add(eq("Set-Cookie"),
                eq("username=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; SameSite=Strict"));
        verify(headers, times(1)).set(eq("Content-Type"), eq("application/json; charset=UTF-8"));
        verify(exchange, times(1)).sendResponseHeaders(400, 0);
    }

    @Test
    void testMissingSessionID() throws IOException {
        // Setup
        ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();

        // Set up request and response
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8)));
        when(exchange.getResponseBody()).thenReturn(responseOutputStream);
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst("Cookie")).thenReturn(null); // No cookie header

        logoutHandler.handle(exchange);

        // Verify headers and response
        verify(headers, times(1)).add(eq("Set-Cookie"),
                eq("sessionID=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; HttpOnly; SameSite=Strict"));
        verify(headers, times(1)).add(eq("Set-Cookie"),
                eq("username=; Path=/; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; SameSite=Strict"));
        verify(headers, times(1)).set(eq("Content-Type"), eq("application/json; charset=UTF-8"));
        verify(exchange, times(1)).sendResponseHeaders(401, 0);
    }
}