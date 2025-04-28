package pro.pantrypilot.endpoints.pages.recipes;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.recipe.Recipe;
import pro.pantrypilot.db.classes.recipe.RecipeDatabase;
import pro.pantrypilot.helpers.FileHelper;
import pro.pantrypilot.helpers.SystemHelper;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Recipes implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(Recipes.class);
    private static final Gson gson = new Gson(); // Gson instance for JSON parsing

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            handlePostRequest(exchange);
        } else if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleGetRequest(exchange);
        } else {
            // Method Not Allowed
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleGetRequest(HttpExchange exchange) throws IOException {
        byte[] responseBytes;
        int statusCode = 200;
        try {
            byte[] fileBytes = FileHelper.readFile("static/recipes/recipes.html");
            responseBytes = fileBytes; // Already bytes
        } catch (IOException e) {
            logger.error("Error reading recipes.html", e);
            responseBytes = "Error: Unable to load recipes.html".getBytes(StandardCharsets.UTF_8);
            statusCode = 500;
        }

        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void handlePostRequest(HttpExchange exchange) throws IOException {
        // 1. Parse form data from request body
        Map<String, String> formData = parseFormData(exchange.getRequestBody());

        // Re-declare errorMessage here
        String errorMessage = null;

        // Extract parameters (adjust names based on your HTML form)
        List<String> ingredients = parseListParameter(formData.get("ingredients"));
        String cuisine = formData.get("cuisine");
        List<String> dietaryRestrictions = parseListParameter(formData.get("dietaryRestrictions"));
        List<String> excludedIngredients = parseListParameter(formData.get("excludedIngredients"));
        String mealType = formData.get("mealType");
        String customPrompt = formData.get("customPrompt");

        // 2. Call Python script using ProcessBuilder
        String scriptPath = "ai_recipe_generator.py"; // Assuming it's in the project root
        List<String> command = new ArrayList<>();
        command.add(SystemHelper.getPythonCommand()); // Use helper to find python/python3
        command.add(scriptPath);

        // Add arguments only if they are provided
        if (ingredients != null && !ingredients.isEmpty()) {
            command.add("--ingredients");
            command.addAll(ingredients);
        }
        if (cuisine != null && !cuisine.isEmpty()) {
            command.add("--cuisine"); command.add(cuisine);
        }
        if (dietaryRestrictions != null && !dietaryRestrictions.isEmpty()) {
            command.add("--dietary-restrictions");
            command.addAll(dietaryRestrictions);
        }
        if (excludedIngredients != null && !excludedIngredients.isEmpty()) {
            command.add("--excluded-ingredients");
            command.addAll(excludedIngredients);
        }
        if (mealType != null && !mealType.isEmpty()) {
            command.add("--meal-type"); command.add(mealType);
        }
        if (customPrompt != null && !customPrompt.isEmpty()) {
            command.add("--custom-prompt"); command.add(customPrompt);
        }

        logger.info("Executing Python script: {}", String.join(" ", command));
        ProcessBuilder pb = new ProcessBuilder(command);
        // pb.redirectErrorStream(true); // DO NOT combine streams

        String jsonOutput = null;
        StringBuilder errorOutput = new StringBuilder(); // Capture stderr separately
        int exitCode = -1;

        try {
            Process process = pb.start();

            // Read stdout (expected JSON)
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            // Read stderr (errors and debug messages)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorOutput.append(line).append(System.lineSeparator());
                }
            }

            exitCode = process.waitFor();
            String stdOutResult = output.toString().trim();
            String stdErrResult = errorOutput.toString().trim();

            if (!stdErrResult.isEmpty()) {
                logger.warn("Python script printed to stderr: {}\n{}", (exitCode != 0 ? "(Exit Code: " + exitCode + ")" : ""), stdErrResult);
            }

            if (exitCode == 0) {
                // Try to parse the stdout as JSON
                if (!stdOutResult.isEmpty() && stdOutResult.startsWith("{") && stdOutResult.endsWith("}")) { // Basic check for JSON
                     jsonOutput = stdOutResult;
                     logger.info("Python script executed successfully. Received JSON from stdout.");
                     logger.debug("Received JSON: {}", jsonOutput);
                 } else {
                     errorMessage = "Python script finished successfully (exit code 0) but did not return valid JSON on stdout. Stdout: [" + stdOutResult + "] Stderr: [" + stdErrResult + "]";
                     logger.error(errorMessage);
                 }
            } else {
                errorMessage = "Python script execution failed with exit code " + exitCode + ". Stderr: [" + stdErrResult + "] Stdout: [" + stdOutResult + "]";
                logger.error(errorMessage);
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error executing or reading output from Python script", e);
            errorMessage = "Failed to execute AI generation script: " + e.getMessage();
        }

        // 3. Process Result
        if (jsonOutput != null) {
            try {
                // 4. Parse JSON and create Recipe object
                Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
                Map<String, Object> recipeData = gson.fromJson(jsonOutput, mapType);

                // Extract data, handling potential nulls and type conversions
                String title = (String) recipeData.get("title");
                List<String> instructionsList = (List<String>) recipeData.get("instructions");
                List<String> ingredientsList = (List<String>) recipeData.get("ingredients"); // This is the list for JSON column
                List<String> tagsList = (List<String>) recipeData.get("tags");

                String instructionsStr = (instructionsList != null) ? String.join("\n", instructionsList) : null;
                String ingredientsJsonStr = (ingredientsList != null) ? gson.toJson(ingredientsList) : null; // Serialize ingredients list to JSON
                String tagsStr = (tagsList != null) ? String.join(",", tagsList) : null;

                String thumbnailUrl = (String) recipeData.get("thumbnail_url"); // Often null
                Integer prepTime = recipeData.containsKey("prep_time_minutes") && recipeData.get("prep_time_minutes") != null ? ((Double) recipeData.get("prep_time_minutes")).intValue() : null;
                Integer cookTime = recipeData.containsKey("cook_time_minutes") && recipeData.get("cook_time_minutes") != null ? ((Double) recipeData.get("cook_time_minutes")).intValue() : null;
                Integer servings = recipeData.containsKey("servings") && recipeData.get("servings") != null ? ((Double) recipeData.get("servings")).intValue() : null;

                // Create Java Recipe object - Use the constructor added for this purpose
                // Assuming rating is not provided directly by AI in this flow, defaulting to 0.0f
                Recipe newRecipe = new Recipe(title, thumbnailUrl, instructionsStr, ingredientsJsonStr, 0.0f, tagsStr, prepTime, cookTime, servings);

                // 5. Insert into database
                int recipeId = RecipeDatabase.insertGeneratedRecipe(newRecipe);

                if (recipeId != -1) {
                    // Success - Redirect to the new recipe page (or back to recipes list)
                    // Redirect requires setting Location header and 302/303 status code
                    logger.info("Successfully generated and saved recipe with ID: {}", recipeId);
                    exchange.getResponseHeaders().add("Location", "/recipe?id=" + recipeId); // Redirect to new recipe detail page
                    exchange.sendResponseHeaders(303, -1); // 303 See Other is appropriate after POST
                    return;
                } else {
                    errorMessage = "AI generation successful, but failed to save the recipe to the database.";
                    logger.error(errorMessage);
                }

            } catch (Exception e) {
                logger.error("Error parsing JSON from Python script or saving recipe", e);
                errorMessage = "Failed to process or save generated recipe: " + e.getMessage();
            }
        }

        // If we reach here, something went wrong (script execution, JSON parsing, DB insert)
        // Send an error response back (could redirect with error query param, or show error page)
        String errorResponse = "<html><body><h1>Error Generating Recipe</h1><p>" + (errorMessage != null ? errorMessage : "An unknown error occurred.") + "</p><a href=\"/recipes\">Back to Recipes</a></body></html>";
        byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(500, errorBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(errorBytes);
        }
    }

    // Helper to parse standard URL-encoded form data
    private Map<String, String> parseFormData(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        String query = br.lines().collect(Collectors.joining());
        Map<String, String> map = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                if (idx > 0 && idx < pair.length() - 1) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name());
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name());
                    map.put(key, value);
                }
            }
        }
        return map;
    }

    // Helper to parse comma-separated string or similar into a list (adjust if form sends multi-value params)
    private List<String> parseListParameter(String paramValue) {
        if (paramValue == null || paramValue.trim().isEmpty()) {
            return null; // Or return empty list: new ArrayList<>();
        }
        // Assuming comma-separated input for list-like fields in the form for now
        return Arrays.stream(paramValue.split(","))
                     .map(String::trim)
                     .filter(s -> !s.isEmpty())
                     .collect(Collectors.toList());
    }

}