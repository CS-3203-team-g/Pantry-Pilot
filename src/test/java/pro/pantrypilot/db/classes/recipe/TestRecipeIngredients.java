package pro.pantrypilot.db.classes.recipe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TestRecipeIngredients {
    @Test
    void testConstructorAndGetters() {
        RecipeIngredient ingredient = new RecipeIngredient(1, 2, 3, "cups");
        assertEquals(1, ingredient.getRecipeID());
        assertEquals(2, ingredient.getIngredientID());
        assertEquals(3, ingredient.getQuantity());
        assertEquals("cups", ingredient.getUnit());
        assertNull(ingredient.getIngredientName());
    }

    @Test
    void testConstructorWithIngredientName() {
        RecipeIngredient ingredient = new RecipeIngredient(1, 2, 3, "cups", "flour");
        assertEquals(1, ingredient.getRecipeID());
        assertEquals(2, ingredient.getIngredientID());
        assertEquals(3, ingredient.getQuantity());
        assertEquals("cups", ingredient.getUnit());
        assertEquals("flour", ingredient.getIngredientName());
    }

    @Test
    void testToString() {
        RecipeIngredient ingredient = new RecipeIngredient(1, 2, 3, "cups", "flour");
        String expected = "RecipeIngredient{recipeID=1, ingredientID=2, quantity=3, unit='cups'}";
        assertEquals(expected, ingredient.toString());
    }



}
