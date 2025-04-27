package pro.pantrypilot.endpoints.api.login;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.session.Session;
import pro.pantrypilot.db.classes.session.SessionsDatabase;
import pro.pantrypilot.db.classes.user.User;
import pro.pantrypilot.db.classes.user.UsersDatabase;
import pro.pantrypilot.helpers.PasswordHasher;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Login implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(Login.class);

    // POJO to represent the login request payload
    private static class LoginRequest {
        String username;
        String password;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Only accept POST requests
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            logger.debug("Invalid request method: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        // Read the JSON payload from the request body
        String requestBody;
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            requestBody = reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            logger.error("Error reading request body", e);
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        // Initialize Gson and attempt to parse the JSON payload into a LoginRequest object
        Gson gson = new Gson();
        LoginRequest loginRequest;
        try {
            loginRequest = gson.fromJson(requestBody, LoginRequest.class);
        } catch (JsonSyntaxException e) {
            logger.error("Error parsing JSON", e);
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON format\"}");
            return;
        }

        // Retrieve the user from the database
        User user = UsersDatabase.getUserByUsername(loginRequest.username);

        if (user == null) {
            logger.debug("User not found: {}", loginRequest.username);
            sendResponse(exchange, 401, "{\"message\": \"Invalid username or password\"}");
            return;
        }

        if (!user.isActive()) {
            sendResponse(exchange, 401, "{\"message\": \"Invalid username or password\"}");
            return;
        }

        // Verify the password using BCrypt
        if (!PasswordHasher.verifyPassword(loginRequest.password, user.getPasswordHash())) {
            logger.debug("Invalid password for user: {}", loginRequest.username);
            sendResponse(exchange, 401, "{\"message\": \"Invalid username or password\"}");
            return;
        }

        Session session = new Session(user.getUserID(), getClientIpAddress(exchange));
        session = SessionsDatabase.createSession(session);

        if(session == null || session.getSessionID() == null) {
            logger.error("Error creating session for user: {}", user.getUsername());
            sendResponse(exchange, 500, "{\"message\": \"Internal server error, null session ID\"}");
            return;
        }

        // Update session's last used time
        SessionsDatabase.updateLastUsed(session.getSessionID());
        UsersDatabase.updateUserLastLogin(user);

        // Set cookies securely using HTTP headers - this will be handled by the browser
        // Set sessionID cookie with HttpOnly flag
        exchange.getResponseHeaders().add("Set-Cookie", 
            String.format("sessionID=%s; Path=/; Secure; HttpOnly; SameSite=Strict", session.getSessionID()));
        
        // Set username cookie without HttpOnly to allow frontend access
        exchange.getResponseHeaders().add("Set-Cookie", 
            String.format("username=%s; Path=/; Secure; SameSite=Strict", loginRequest.username));

        // Login successful - still send a response for the client to process
        sendResponse(exchange, 200, "{\"message\": \"Login successful\",\"sessionID\": \"" + session.getSessionID() + "\"}");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private String getClientIpAddress(HttpExchange exchange) {
        // Try to get IP from X-Forwarded-For header first
        String ip = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            // X-Forwarded-For may contain multiple IPs; get the first one
            return ip.split(",")[0].trim();
        }
        // Fallback to remote address if X-Forwarded-For is not present
        return exchange.getRemoteAddress().getAddress().getHostAddress();
    }
}
