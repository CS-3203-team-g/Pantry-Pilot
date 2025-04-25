package pro.pantrypilot.endpoints.api.shoppingLists;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.session.Session;
import pro.pantrypilot.db.classes.session.SessionsDatabase;
import pro.pantrypilot.db.classes.shoppingList.ShoppingList;
import pro.pantrypilot.db.classes.shoppingList.ShoppingListIngredientsDatabase;
import pro.pantrypilot.db.classes.shoppingList.ShoppingListsDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class RemoveIngredientFromShoppingList implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(RemoveIngredientFromShoppingList.class);

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
        RemoveIngredientRequest removeIngredientRequest = new Gson().fromJson(requestBody, RemoveIngredientRequest.class);

        // If sessionID is not found in cookies, try to get it from request body (for backward compatibility)
        if (sessionID == null && removeIngredientRequest.sessionID != null) {
            sessionID = removeIngredientRequest.sessionID;
        }

        // Validate sessionID
        if (sessionID == null) {
            logger.debug("No sessionID provided");
            sendErrorResponse(exchange, 401, "No session ID provided");
            return;
        }

        Session session = SessionsDatabase.getSession(sessionID);
        if (session == null) {
            logger.debug("Invalid sessionID: {}", sessionID);
            sendErrorResponse(exchange, 401, "Invalid session");
            return;
        }

        // Update session activity
        SessionsDatabase.updateLastUsed(sessionID);

        // Check if the user owns the shopping list
        ShoppingList shoppingList = ShoppingListsDatabase.getShoppingListWithIngredients(removeIngredientRequest.shoppingListID);
        if (shoppingList == null) {
            logger.debug("Shopping list not found for ID: {}", removeIngredientRequest.shoppingListID);
            sendErrorResponse(exchange, 404, "Shopping list not found");
            return;
        }

        if (!shoppingList.getUserID().equals(session.getUserID())) {
            logger.debug("User does not own the shopping list: {}", removeIngredientRequest.shoppingListID);
            sendErrorResponse(exchange, 403, "You don't have permission to modify this list");
            return;
        }

        // Remove the ingredient from the shopping list
        boolean success = ShoppingListIngredientsDatabase.removeIngredientFromShoppingList(
                removeIngredientRequest.shoppingListID,
                removeIngredientRequest.ingredientID,
                removeIngredientRequest.quantity,
                removeIngredientRequest.unit
        );

        if (success) {
            String response = "{\"success\": true, \"message\": \"Ingredient removed successfully\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } else {
            sendErrorResponse(exchange, 400, "Failed to remove ingredient");
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = String.format("{\"success\": false, \"message\": \"%s\"}", message);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // Inner class to represent the request payload
    private static class RemoveIngredientRequest {
        int shoppingListID;
        int ingredientID;
        int quantity;
        String unit;
        String sessionID; // For backward compatibility
    }
}