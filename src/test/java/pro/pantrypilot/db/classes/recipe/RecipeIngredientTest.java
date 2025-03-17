package pro.pantrypilot.db.classes.recipe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RecipeIngredientTest {

    @Test
    void testConstructorAndGetters() {
        RecipeIngredient ingredient = new RecipeIngredient(1, 2, 3, 4);
        assertEquals(1, ingredient.getRecipeID());
        assertEquals(2, ingredient.getIngredientID());
        assertEquals(3, ingredient.getQuantity());
        assertEquals(4, ingredient.getUnitID());
        assertNull(ingredient.getIngredientName());
        assertNull(ingredient.getUnitName());
    }

    @Test
    void testConstructorWithIngredientName() {
        RecipeIngredient ingredient = new RecipeIngredient(1, 2, 3, 4, "flour");
        assertEquals(1, ingredient.getRecipeID());
        assertEquals(2, ingredient.getIngredientID());
        assertEquals(3, ingredient.getQuantity());
        assertEquals(4, ingredient.getUnitID());
        assertEquals("flour", ingredient.getIngredientName());
        assertNull(ingredient.getUnitName());
    }

    @Test
    void testConstructorWithAllFields() {
        RecipeIngredient ingredient = new RecipeIngredient(1, 2, 3, 4, "flour", "cups");
        assertEquals(1, ingredient.getRecipeID());
        assertEquals(2, ingredient.getIngredientID());
        assertEquals(3, ingredient.getQuantity());
        assertEquals(4, ingredient.getUnitID());
        assertEquals("flour", ingredient.getIngredientName());
        assertEquals("cups", ingredient.getUnitName());
    }

    @Test
    void testToString() {
        RecipeIngredient ingredient = new RecipeIngredient(1, 2, 3, 4, "flour", "cups");
        String expected = "RecipeIngredient{recipeID=1, ingredientID=2, quantity=3, unitID=4, ingredientName='flour', unitName='cups'}";
        assertEquals(expected, ingredient.toString());
    }
}