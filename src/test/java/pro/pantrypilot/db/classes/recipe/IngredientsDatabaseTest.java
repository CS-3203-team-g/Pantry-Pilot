package pro.pantrypilot.db.classes.recipe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.pantrypilot.db.DatabaseConnectionManager;
import pro.pantrypilot.db.classes.ingredient.Ingredient;
import pro.pantrypilot.db.classes.ingredient.IngredientsDatabase;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IngredientsDatabaseTest {
    
    private Connection connection;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize the test database
        IngredientsDatabase.initializeIngredientsDatabase();
        connection = DatabaseConnectionManager.getConnection();
        
        // Clean up any existing data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM ingredients");
        }
        
        // Insert test data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO ingredients (id, name) VALUES (1, 'flour')");
            stmt.execute("INSERT INTO ingredients (id, name) VALUES (2, 'sugar')");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up test data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM ingredients");
        }
    }

    @Test
    void initializeIngredientsDatabase() {
        // This test verifies that we can initialize the database without errors
        IngredientsDatabase.initializeIngredientsDatabase();
        // If we get here without exceptions, the test passes
        assertTrue(true);
    }

    @Test
    void getAllIngredientNames() {
        // Get the results
        List<String> ingredientNames = IngredientsDatabase.getAllIngredientNames();
        
        // Verify results
        assertNotNull(ingredientNames);
        assertEquals(2, ingredientNames.size());
        assertEquals("flour", ingredientNames.get(0));
        assertEquals("sugar", ingredientNames.get(1));
    }

    @Test
    void getAllIngredients() {
        // Get the results
        List<Ingredient> ingredients = IngredientsDatabase.getAllIngredients();
        
        // Verify results
        assertNotNull(ingredients);
        assertEquals(2, ingredients.size());
        assertEquals(1, ingredients.get(0).getIngredientID());
        assertEquals("flour", ingredients.get(0).getIngredientName());
        assertEquals(2, ingredients.get(1).getIngredientID());
        assertEquals("sugar", ingredients.get(1).getIngredientName());
    }
}