package pro.pantrypilot.endpoints.api.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.session.Session;
import pro.pantrypilot.db.classes.session.SessionsDatabase;
import pro.pantrypilot.db.classes.user.User;
import pro.pantrypilot.endpoints.api.recipes.bulk.BulkRecipeEditor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class LoadRecipeDatabasesJSON implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoadRecipeDatabasesJSON.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
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

        Session session = SessionsDatabase.getSession(sessionID);
        if (session == null || !session.isValid()) {
            logger.debug("Session not found or invalid, redirecting to login");
            exchange.getResponseHeaders().add("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        User user = session.getUser();
        if (user == null) {
            logger.error("User not found for sessionID: {}", sessionID);
            exchange.sendResponseHeaders(500, -1);
            return;
        }

        if (!user.isAdmin()) {
            logger.debug("User is not an admin, redirecting to index");
            exchange.getResponseHeaders().add("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        logger.debug("Processing JSON file upload");

        // Read request body (JSON data)
        String jsonRequestBody;
        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            jsonRequestBody = br.lines().collect(Collectors.joining());
        }

        if (jsonRequestBody.isEmpty()) {
            logger.error("Received empty JSON request body");
            exchange.sendResponseHeaders(400, -1); // Bad Request
            return;
        }

        logger.debug("Received JSON: {}", jsonRequestBody);

        // Process JSON data
        boolean success = BulkRecipeEditor.loadRecipeDatabasesFromJSON(jsonRequestBody);

        if (!success) {
            logger.error("Failed to process JSON data");
            exchange.sendResponseHeaders(500, -1);
            return;
        }

        logger.debug("Successfully processed JSON file");
        String response = "JSON data successfully processed";
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}