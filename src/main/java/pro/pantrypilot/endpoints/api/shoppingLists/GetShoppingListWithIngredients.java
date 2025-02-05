package pro.pantrypilot.endpoints.api.shoppingLists;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.shoppingList.ShoppingList;
import pro.pantrypilot.db.classes.shoppingList.ShoppingListsDatabase;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class GetShoppingListWithIngredients implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetShoppingListWithIngredients.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            logger.debug("Invalid request method: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String query = exchange.getRequestURI().getQuery();
        int shoppingListID;
        try {
            shoppingListID = Integer.parseInt(query.split("=")[1]);
        } catch (Exception e) {
            logger.error("Invalid shopping list ID format: {}", query);
            exchange.sendResponseHeaders(400, -1); // Bad Request
            return;
        }

        ShoppingList shoppingList = ShoppingListsDatabase.getShoppingListWithIngredients(shoppingListID);
        if (shoppingList == null) {
            logger.debug("Shopping list not found for ID: {}", shoppingListID);
            exchange.sendResponseHeaders(404, -1); // Not Found
            return;
        }

        Gson gson = new Gson();
        String jsonResponse = gson.toJson(shoppingList);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}