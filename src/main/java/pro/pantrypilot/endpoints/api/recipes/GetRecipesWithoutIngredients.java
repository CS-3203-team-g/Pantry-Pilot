package pro.pantrypilot.endpoints.api.recipes;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.recipe.Recipe;
import pro.pantrypilot.db.classes.recipe.RecipeDatabase;
import pro.pantrypilot.validation.RecipeValidator;
import pro.pantrypilot.validation.ValidationResult;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GetRecipesWithoutIngredients implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetRecipesWithoutIngredients.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            logger.debug("Invalid request method: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        ArrayList<Recipe> recipes = RecipeDatabase.getRecipesNoIngredients();
        
//        // Validate each recipe using our validator
//        for (Recipe recipe : recipes) {
//            ValidationResult result = RecipeValidator.validateRecipe(recipe.getTitle());
//            if (result.isValid()) {
//                // logger.info("Recipe validated successfully: {}", recipe.getTitle());
//            } else {
//               // logger.warn("Recipe validation failed: {} - {}", recipe.getTitle(), result.getErrorMessage());
//            }
//        }

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(recipes);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}