package pro.pantrypilot.endpoints.api.recipes;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.ingredient.Ingredient;
import pro.pantrypilot.db.classes.ingredient.IngredientsDatabase;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GetIngredients implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetIngredients.class);
    private static List<Ingredient> cachedIngredients = new ArrayList<>();

    static {
        initializeCache();
    }

    private static void initializeCache() {
        try {
            cachedIngredients = IngredientsDatabase.getAllIngredients();
            logger.info("Ingredients cache initialized with {} items", cachedIngredients.size());
        } catch (Exception e) {
            logger.error("Error initializing ingredients cache", e);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            logger.debug("Invalid request method: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(cachedIngredients);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}