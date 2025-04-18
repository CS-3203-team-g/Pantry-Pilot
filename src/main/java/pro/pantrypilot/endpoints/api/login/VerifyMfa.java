package pro.pantrypilot.endpoints.api.login;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.session.Session;
import pro.pantrypilot.db.classes.session.SessionsDatabase;
import pro.pantrypilot.db.classes.user.User;
import pro.pantrypilot.db.classes.user.UsersDatabase;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class VerifyMfa implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(VerifyMfa.class);
    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    // POJO for request body
    private static class VerifyMfaRequest {
        String userId;
        String otpCode;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            logger.debug("Invalid request method: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String requestBody;
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            requestBody = reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            logger.error("Error reading request body", e);
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        Gson gson = new Gson();
        VerifyMfaRequest mfaRequest;
        try {
            mfaRequest = gson.fromJson(requestBody, VerifyMfaRequest.class);
            if (mfaRequest.userId == null || mfaRequest.otpCode == null) {
                throw new JsonSyntaxException("Missing userId or otpCode");
            }
        } catch (JsonSyntaxException e) {
            logger.error("Error parsing JSON or missing fields", e);
            sendResponse(exchange, 400, "{\"message\": \"Invalid JSON format or missing fields\"}");
            return;
        }

        User user = UsersDatabase.getUserById(mfaRequest.userId);

        if (user == null || !user.isActive()) {
            logger.warn("MFA verification attempt for invalid or inactive user ID: {}", mfaRequest.userId);
            sendResponse(exchange, 401, "{\"message\": \"Invalid user or OTP code\"}");
            return;
        }

        if (!user.isMfaEnabled() || user.getMfaSecret() == null) {
            logger.error("MFA verification attempt for user ID: {} but MFA not enabled or no secret found.", mfaRequest.userId);
            sendResponse(exchange, 400, "{\"message\": \"MFA not enabled for this user\"}");
            return;
        }

        // Verify the OTP code
        try {
            int otp = Integer.parseInt(mfaRequest.otpCode);
            boolean isCodeValid = gAuth.authorize(user.getMfaSecret(), otp);

            if (isCodeValid) {
                logger.info("MFA verification successful for user: {}", user.getUsername());
                // Create session upon successful MFA verification
                Session session = new Session(user.getUserID(), exchange.getRemoteAddress().getAddress().getHostAddress());
                session = SessionsDatabase.createSession(session);

                if (session == null || session.getSessionID() == null) {
                    logger.error("Error creating session after MFA verification for user: {}", user.getUsername());
                    sendResponse(exchange, 500, "{\"message\": \"Internal server error, could not create session\"}");
                    return;
                }

                // Send success response with session ID
                String successResponse = String.format("{\"message\": \"Login successful\", \"sessionID\": \"%s\"}", session.getSessionID());
                sendResponse(exchange, 200, successResponse);

            } else {
                logger.warn("Invalid MFA code provided for user: {}", user.getUsername());
                sendResponse(exchange, 401, "{\"message\": \"Invalid user or OTP code\"}");
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid OTP format received: {}", mfaRequest.otpCode);
            sendResponse(exchange, 400, "{\"message\": \"Invalid OTP format\"}");
        } catch (Exception e) {
            logger.error("Error during MFA verification for user: {}", user.getUsername(), e);
            sendResponse(exchange, 500, "{\"message\": \"Internal server error during MFA verification\"}");
        }
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