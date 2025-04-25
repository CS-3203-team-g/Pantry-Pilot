package pro.pantrypilot.endpoints.api.settings;

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

public class ChangePassword implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChangePassword.class);

    // POJO to represent the change password request payload
    private static class ChangePasswordRequest {
        String currentPassword;
        String newPassword;
        String sessionID;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Only accept POST requests
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            logger.debug("Invalid request method: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        // Extract sessionID from cookies
        String sessionID = null;
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");

        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                cookie = cookie.trim();
                if (cookie.startsWith("sessionID=")) {
                    sessionID = cookie.substring("sessionID=".length());
                    break;
                }
            }
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

        // Initialize Gson and attempt to parse the JSON payload into a ChangePasswordRequest object
        Gson gson = new Gson();
        ChangePasswordRequest changePasswordRequest;
        try {
            changePasswordRequest = gson.fromJson(requestBody, ChangePasswordRequest.class);
            changePasswordRequest.sessionID = sessionID;
        } catch (JsonSyntaxException e) {
            logger.error("Error parsing JSON", e);
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON format\"}");
            return;
        }

        // verify sessionID
        Session session = SessionsDatabase.getSession(changePasswordRequest.sessionID);
        if (session == null) {
            logger.debug("Invalid sessionID: {}", changePasswordRequest.sessionID);
            sendResponse(exchange, 401, "{\"message\": \"Invalid sessionID\"}");
            return;
        }

        // Retrieve the user from the database and verify the current password
        User user = UsersDatabase.getUserByUserId(session.getUserID());
        if (user == null) {
            logger.debug("Username or password is incorrect for userID: {}", session.getUserID());
            sendResponse(exchange, 401, "{\"message\": \"Invalid username or password\"}");
            return;
        }

        // Update session activity
        SessionsDatabase.updateLastUsed(changePasswordRequest.sessionID);

        // Verify the current password using BCrypt
        if (!PasswordHasher.verifyPassword(changePasswordRequest.currentPassword, user.getPasswordHash())) {
            logger.debug("Invalid current password for user: {}", user.getUsername());
            sendResponse(exchange, 401, "{\"message\": \"Invalid username or password\"}");
            return;
        }

        // Hash the new password with BCrypt
        String newPassword = PasswordHasher.generatePassword(changePasswordRequest.newPassword);

        // Update the user's password in the database
        boolean updateSuccess = UsersDatabase.updateUserPassword(user.getUserID(), newPassword);

        if (!updateSuccess) {
            logger.error("Error updating password for user: {}", user.getUsername());
            sendResponse(exchange, 500, "{\"message\": \"Internal server error\"}");
            return;
        }

        // Password change successful
        sendResponse(exchange, 200, "{\"message\": \"Password changed successfully\"}");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}