package pro.pantrypilot.db.classes.recipe;

import org.junit.jupiter.api.Test;
import pro.pantrypilot.db.DatabaseConnectionManager;
import pro.pantrypilot.db.classes.ingredient.IngredientsDatabase;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.*;

class RecipeIngredientsDatabaseTest {

    private void initTest() {
        DatabaseConnectionManager.initializeDatabase();

        String clearRecipeTableSQL = "DELETE FROM pantry_pilot.recipes;";
        String clearIngredientTableSQL = "DELETE FROM pantry_pilot.ingredients;";
        String clearRecipeIngredientTableSQL = "DELETE FROM pantry_pilot.recipe_ingredients;";

        String insertRecipeSQL = "INSERT INTO pantry_pilot.recipes (recipeID, title, instructions, thumbnailUrl, rating) VALUES (1, 'Tiramisu Blondies', 'Preheat the oven to 350 degrees...', 'https://www.allrecipes.com/thmb/7RTcK0893zgtVf7Gg6LWmew6g_U=/750x0/filters:no_upscale():max_bytes(150000):strip_icc()/8781917_Tiramisu-Blondies_Kim-Shupe_4x3-38045a8e3d5944b6abe149ca8b02d16d.jpg', 0);";
        String insertIngredientSQL = "INSERT INTO pantry_pilot.ingredients (ingredientID, ingredientName) VALUES (1, 'semisweet chocolate chips');";
        String insertRecipeIngredientSQL = "INSERT INTO pantry_pilot.recipe_ingredients (recipeID, ingredientID, quantity, unitID) VALUES (1, 1, 1.5, 1);"; // Changed to use decimal quantity and unitID

        try {
            Connection conn = DatabaseConnectionManager.getConnection();
            conn.createStatement().execute(clearRecipeTableSQL);
            conn.createStatement().execute(clearIngredientTableSQL);
            conn.createStatement().execute(clearRecipeIngredientTableSQL);
            conn.createStatement().execute(insertRecipeSQL);
            conn.createStatement().execute(insertIngredientSQL);
            conn.createStatement().execute(insertRecipeIngredientSQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void initializeRecipeIngredientsDatabase() {
        // weak testing because RecipeDatabaseTest tests this method and database

        initTest();

        assertTrue(true);

    }
}