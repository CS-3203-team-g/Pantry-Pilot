package pro.pantrypilot.endpoints.api.recipes;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;
import pro.pantrypilot.db.classes.recipe.Recipe;
import pro.pantrypilot.db.classes.recipe.RecipeDatabase;
import pro.pantrypilot.db.classes.ingredient.NutritionFacts;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetRecipeNutritionFacts implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetRecipeNutritionFacts.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            logger.debug("Invalid request method: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // Parse recipeID from URL query
        String query = exchange.getRequestURI().getQuery();
        int recipeID;
        try {
            recipeID = Integer.parseInt(query.split("=")[1]);
        } catch (Exception e) {
            logger.error("Invalid recipe ID format: {}", query);
            sendErrorResponse(exchange, 400, "Invalid recipe ID");
            return;
        }

        // Get recipe with ingredients (to validate it exists)
        Recipe recipe = RecipeDatabase.getRecipeWithIngredients(recipeID);
        if (recipe == null || recipe.getIngredients() == null) {
            logger.debug("Recipe not found for ID: {}", recipeID);
            sendErrorResponse(exchange, 404, "Recipe not found");
            return;
        }

        // Get nutrition facts with efficient SQL query
        Map<String, Object> nutritionData = getNutritionDataForRecipe(recipeID);
        
        // Format response
        String jsonResponse = new Gson().toJson(nutritionData);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
    
    /**
     * Gets nutrition data for the recipe with a single optimized SQL query
     * @param recipeID The ID of the recipe to calculate nutrition for
     * @return Map containing total nutrition and per-ingredient nutrition
     */
    private Map<String, Object> getNutritionDataForRecipe(int recipeID) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> totalNutrition = new HashMap<>();
        
        // Get recipe with ingredients
        Recipe recipe = RecipeDatabase.getRecipeWithIngredients(recipeID);
        if (recipe == null || recipe.getIngredients() == null) {
            logger.error("Recipe or ingredients not found for ID: {}", recipeID);
            return result;
        }
        
        // Calculate total nutrition using the Recipe class method
        NutritionFacts total = recipe.calculateTotalNutrition();
        
        // Store total nutrition (round to 1 decimal place for cleaner display)
        totalNutrition.put("calories", round(total.getCalories()));
        totalNutrition.put("fat", round(total.getFat()));
        totalNutrition.put("carbohydrates", round(total.getCarbohydrates()));
        totalNutrition.put("protein", round(total.getProtein()));
        
        // Build result with just the total
        result.put("total", totalNutrition);
        
        return result;
    }
    
    private float round(Float value) {
        if (value == null) return 0.0f;
        return Math.round(value * 10) / 10.0f;
    }
    
    /**
     * Send an error response in JSON format
     */
    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(errorResponse);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}