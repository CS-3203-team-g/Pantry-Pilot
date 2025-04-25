package pro.pantrypilot.endpoints.api.recipes;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.recipe.Recipe;
import pro.pantrypilot.db.classes.recipe.RecipeDatabase;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GetRecipeWithIngredients implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetRecipeWithIngredients.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            logger.debug("Invalid request method: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        if (query == null || !query.contains("=")) {
            logger.error("Missing or invalid query parameter");
            sendErrorResponse(exchange, 400, "Missing recipe ID");
            return;
        }

        int recipeID;
        try {
            recipeID = Integer.parseInt(query.split("=")[1]);
        } catch (Exception e) {
            logger.error("Invalid recipe ID format: {}", query);
            sendErrorResponse(exchange, 400, "Invalid recipe ID format");
            return;
        }

        Recipe recipe = RecipeDatabase.getRecipeWithIngredients(recipeID);
        if (recipe == null) {
            logger.debug("Recipe not found for ID: {}", recipeID);
            sendErrorResponse(exchange, 404, "Recipe not found");
            return;
        }

        if (recipe.getIngredients() == null) {
            recipe.setIngredients(new ArrayList<>());
        }

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(recipe);

        sendSuccessResponse(exchange, jsonResponse);
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        String jsonResponse = String.format("{\"error\": \"%s\"}", message);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendSuccessResponse(HttpExchange exchange, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}