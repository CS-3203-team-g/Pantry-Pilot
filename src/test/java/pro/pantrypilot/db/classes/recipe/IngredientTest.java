package pro.pantrypilot.db.classes.recipe;

import org.junit.jupiter.api.Test;
import pro.pantrypilot.db.classes.ingredient.Ingredient;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    @Test
    void testConstructorAndGetters() {
        Ingredient ingredient = new Ingredient(1, "sugar", null);
        assertEquals(1, ingredient.getIngredientID());
        assertEquals("sugar", ingredient.getIngredientName());
        assertNull(ingredient.getDefaultUnitID());
    }

    @Test
    void testConstructorWithDefaultUnit() {
        Ingredient ingredient = new Ingredient(1, "sugar", 2);
        assertEquals(1, ingredient.getIngredientID());
        assertEquals("sugar", ingredient.getIngredientName());
        assertEquals(2, ingredient.getDefaultUnitID());
    }
}