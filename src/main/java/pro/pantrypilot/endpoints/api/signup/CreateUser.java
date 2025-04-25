package pro.pantrypilot.endpoints.api.signup;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class CreateUser implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateUser.class);

    // POJO to represent the sign-up request payload
    private static class SignupRequest {
        String username;
        String email;
        String password;

        // Optionally, add getters/setters if needed.
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
        }
        catch (Exception ignore){
            logger.debug("Error reading request body");
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        // Initialize Gson and attempt to parse the JSON payload into a SignupRequest object
        Gson gson = new Gson();
        SignupRequest signupRequest;
        try {
            signupRequest = gson.fromJson(requestBody, SignupRequest.class);
        } catch (JsonSyntaxException e) {
            // If parsing fails, return a 400 Bad Request response with an error message.
            String errorResponse = "{\"message\": \"Invalid JSON format\"}";
            logger.error("Invalid JSON format: {}", requestBody);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, errorBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorBytes);
            }
            return;
        }

        // Validate input (you can add more validation if needed)
        if (signupRequest.username == null || signupRequest.password == null || signupRequest.email == null) {
            sendResponse(exchange, 400, "{\"message\": \"Username, email and password are required\"}");
            return;
        }

        // Check if username already exists
        User existingUser = UsersDatabase.getUserByUsername(signupRequest.username);
        if (existingUser != null) {
            sendResponse(exchange, 409, "{\"message\": \"Username already exists\"}");
            return;
        }

        // Since there's no direct getUserByEmail method, we'll need to create a User object
        // and trust the database constraints to enforce unique emails
        
        // Create a new User object with the provided data
        User newUser = new User(signupRequest.username, signupRequest.email, signupRequest.password);
        
        // Store the user in the database
        boolean success = UsersDatabase.createUser(newUser);
        
        if (!success) {
            sendResponse(exchange, 500, "{\"message\": \"Error creating user\"}");
            return;
        }

        // Create a session for the new user
        User createdUser = UsersDatabase.getUserByUsername(signupRequest.username);
        Session session = new Session(createdUser.getUserID(), exchange.getRemoteAddress().getAddress().getHostAddress());
        session = SessionsDatabase.createSession(session);

        if(session == null || session.getSessionID() == null) {
            logger.error("Error creating session for user: {}", createdUser.getUsername());
            sendResponse(exchange, 500, "{\"message\": \"User created but error creating session\"}");
            return;
        }

        // Update session's last used time
        SessionsDatabase.updateLastUsed(session.getSessionID());
        
        // Set cookies securely using HTTP headers - this will be handled by the browser
        // Set sessionID cookie with HttpOnly flag
        exchange.getResponseHeaders().add("Set-Cookie", 
            String.format("sessionID=%s; Path=/; Secure; HttpOnly; SameSite=Strict", session.getSessionID()));
        
        // Set username cookie without HttpOnly to allow frontend access
        exchange.getResponseHeaders().add("Set-Cookie", 
            String.format("username=%s; Path=/; Secure; SameSite=Strict", createdUser.getUsername()));

        // Send success response
        sendResponse(exchange, 201, "{\"message\": \"User created successfully\", \"sessionID\": \"" + session.getSessionID() + "\"}");
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
