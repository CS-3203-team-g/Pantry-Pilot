package pro.pantrypilot.db.classes.list;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.*;
import java.nio.charset.StandardCharsets;
import static org.mockito.Mockito.*;
import pro.pantrypilot.endpoints.api.shoppingLists.RemoveIngredientFromShoppingList;


public class RemoveIngredientFromShoppingListTest {
    private RemoveIngredientFromShoppingList handler;
    private HttpExchange exchange;

    @BeforeEach
    void setUp() {
        handler = new RemoveIngredientFromShoppingList();
        exchange = mock(HttpExchange.class);
    }

    @Test
    void testHandle_SuccessfulRemoval() throws IOException {
        when(exchange.getRequestMethod()).thenReturn("POST");

        // Use a separate test request class instead of accessing private inner class
        String requestBody = new Gson().toJson(new TestRemoveIngredientRequest(1, 101, 2, "kg", "validSession"));
        when(exchange.getRequestBody()).thenReturn(new ByteArrayInputStream(requestBody.getBytes(StandardCharsets.UTF_8)));

        OutputStream responseStream = new ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(responseStream);

        handler.handle(exchange);

        verify(exchange, atLeastOnce()).sendResponseHeaders(anyInt(), anyLong());
    }

    // Separate test class to mimic the real request format
    static class TestRemoveIngredientRequest {
        int shoppingListID;
        int ingredientID;
        int quantity;
        String unit;
        String sessionID;

        public TestRemoveIngredientRequest(int shoppingListID, int ingredientID, int quantity, String unit, String sessionID) {
            this.shoppingListID = shoppingListID;
            this.ingredientID = ingredientID;
            this.quantity = quantity;
            this.unit = unit;
            this.sessionID = sessionID;
        }
    }
}
