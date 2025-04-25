package pro.pantrypilot.endpoints.api.shoppingLists;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.session.Session;
import pro.pantrypilot.db.classes.session.SessionsDatabase;
import pro.pantrypilot.db.classes.shoppingList.ShoppingListsDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class CreateShoppingList implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateShoppingList.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
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

        // Read the request body
        String requestBody;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            requestBody = reader.lines().collect(Collectors.joining("\n"));
        }

        // Parse the request body
        CreateListRequest createListRequest = new Gson().fromJson(requestBody, CreateListRequest.class);

        // If sessionID is not found in cookies, try to get it from request body (for backward compatibility)
        if (sessionID == null && createListRequest.sessionID != null) {
            sessionID = createListRequest.sessionID;
        }

        // Validate sessionID
        if (sessionID == null) {
            logger.debug("No sessionID provided");
            exchange.sendResponseHeaders(401, -1); // Unauthorized
            return;
        }

        Session session = SessionsDatabase.getSession(sessionID);
        if (session == null) {
            logger.debug("Invalid sessionID: {}", sessionID);
            exchange.sendResponseHeaders(401, -1); // Unauthorized
            return;
        }

        // Update session activity
        SessionsDatabase.updateLastUsed(sessionID);

        // Create the shopping list
        boolean success = ShoppingListsDatabase.createShoppingList(createListRequest.listName, session.getUserID());

        if (success) {
            String response = "{\"success\": true, \"message\": \"List created successfully\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } else {
            String response = "{\"success\": false, \"message\": \"Failed to create list\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }

    // Inner class to represent the request payload
    private static class CreateListRequest {
        String listName;
        String sessionID; // For backward compatibility
    }
}