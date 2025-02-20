package pro.pantrypilot.db.classes.recipe;

import org.junit.jupiter.api.Test;
import pro.pantrypilot.db.DatabaseConnectionManager;

import static org.junit.jupiter.api.Assertions.*;

class IngredientsDatabaseTest {

    private void initTest() {
        IngredientsDatabase.initializeIngredientsDatabase();

        String insertIngredientSQL = "INSERT INTO pantry_pilot.ingredients (ingredientID, ingredientName) VALUES (1, 'baking soda');";

        DatabaseConnectionManager.initializeDatabase();
        try {
            DatabaseConnectionManager.getConnection().createStatement().execute(insertIngredientSQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void initializeIngredientsDatabase() {
        IngredientsDatabase.initializeIngredientsDatabase();
        assertTrue(true);
    }

    @Test
    void getAllIngredientNames() {
        IngredientsDatabase.initializeIngredientsDatabase();
        initTest();
        assertEquals("baking soda", IngredientsDatabase.getAllIngredientNames().get(0));
    }

    @Test
    void getAllIngredients() {
        IngredientsDatabase.initializeIngredientsDatabase();
        initTest();
        assertEquals(1, IngredientsDatabase.getAllIngredients().get(0).getIngredientID());
    }
}