package pro.pantrypilot.db.classes.recipe;

import org.junit.jupiter.api.Test;
import pro.pantrypilot.db.classes.ingredient.Ingredient;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    @Test
    void testConstructorAndGetters() {
        Ingredient ingredient = new Ingredient(1, "sugar");
        assertEquals(1, ingredient.getIngredientID());
        assertEquals("sugar", ingredient.getIngredientName());
    }
}