package pro.pantrypilot.db.classes.recipe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pro.pantrypilot.db.DatabaseConnectionManager;
import pro.pantrypilot.db.classes.ingredient.IngredientsDatabase;
import pro.pantrypilot.db.classes.unit.UnitsDatabase;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class RecipeDatabaseTest {
    
    private Connection connection;
    private static final int TEST_RECIPE_ID = 1;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize the test databases
        DatabaseConnectionManager.initializeDatabase();
        connection = DatabaseConnectionManager.getConnection();
        
        // Clean up any existing data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM recipe_ingredients");
            stmt.execute("DELETE FROM recipes");
            stmt.execute("DELETE FROM ingredients");
            stmt.execute("DELETE FROM units");
        }
        
        // Insert test data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("INSERT INTO units (unitID, unitName) VALUES (1, 'cup')");
            stmt.execute("INSERT INTO recipes (recipeID, title, instructions, thumbnailUrl, rating) " +
                        "VALUES (" + TEST_RECIPE_ID + ", 'Tiramisu Blondies', " +
                        "'Preheat the oven to 350 degrees...', " +
                        "'https://www.allrecipes.com/thmb/example.jpg', 0)");
            stmt.execute("INSERT INTO ingredients (id, name) " +
                        "VALUES (1, 'semisweet chocolate chips')");
            stmt.execute("INSERT INTO recipe_ingredients (recipeID, ingredientID, quantity, unitID) " +
                        "VALUES (" + TEST_RECIPE_ID + ", 1, 1, 1)");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up test data
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM recipe_ingredients");
            stmt.execute("DELETE FROM recipes");
            stmt.execute("DELETE FROM ingredients");
            stmt.execute("DELETE FROM units");
        }
    }

    @Test
    void initializeRecipeDatabase() {
        // This test verifies that we can initialize the database without errors
        RecipeDatabase.initializeRecipeDatabase();
        // If we get here without exceptions, the test passes
        assertTrue(true);
    }

    @Test
    void getRecipesNoIngredients() {
        List<Recipe> recipes = RecipeDatabase.getRecipesNoIngredients();
        
        assertNotNull(recipes, "Recipe list should not be null");
        assertEquals(1, recipes.size(), "Should have exactly one recipe");
        Recipe recipe = recipes.get(0);
        assertEquals("Tiramisu Blondies", recipe.getTitle());
        assertEquals("Preheat the oven to 350 degrees...", recipe.getInstructions());
    }

    @Test
    void getRecipesWithIngredients() {
        List<Recipe> recipes = RecipeDatabase.getRecipesWithIngredients();
        
        assertNotNull(recipes, "Recipe list should not be null");
        assertEquals(1, recipes.size(), "Should have exactly one recipe");
        
        Recipe recipe = recipes.get(0);
        assertEquals("Tiramisu Blondies", recipe.getTitle());
        assertNotNull(recipe.getIngredients(), "Recipe ingredients should not be null");
        assertEquals(1, recipe.getIngredients().size(), "Should have one ingredient");
        
        RecipeIngredient ingredient = recipe.getIngredients().get(0);
        assertEquals("semisweet chocolate chips", ingredient.getIngredientName());
        assertEquals(0, new BigDecimal("1.0").compareTo(ingredient.getQuantity()), "Quantity should be 1.0");
        assertEquals("cup", ingredient.getUnitName());
        assertEquals(1, ingredient.getUnitID());
    }

    @Test
    void getRecipeWithIngredients() {
        Recipe recipe = RecipeDatabase.getRecipeWithIngredients(TEST_RECIPE_ID);
        
        assertNotNull(recipe, "Recipe should not be null");
        assertEquals("Tiramisu Blondies", recipe.getTitle());
        assertNotNull(recipe.getIngredients(), "Recipe ingredients should not be null");
        assertEquals(1, recipe.getIngredients().size(), "Should have one ingredient");
        
        RecipeIngredient ingredient = recipe.getIngredients().get(0);
        assertEquals("semisweet chocolate chips", ingredient.getIngredientName());
        assertEquals(0, new BigDecimal("1.0").compareTo(ingredient.getQuantity()), "Quantity should be 1.0");
        assertEquals("cup", ingredient.getUnitName());
        assertEquals(1, ingredient.getUnitID());
    }

    @Test
    void getNonExistentRecipe() {
        Recipe recipe = RecipeDatabase.getRecipeWithIngredients(999);
        assertNull(recipe, "Non-existent recipe should return null");
    }
}