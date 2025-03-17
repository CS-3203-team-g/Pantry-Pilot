package pro.pantrypilot.db.classes.recipe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

class RecipeTest {

    @Test
    void testConstructorAndGettersWithIngredients() {
        ArrayList<RecipeIngredient> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredient(1, 1, 2, 1, "flour", "cups"));
        
        Recipe recipe = new Recipe(1, "Test Recipe", "thumbnail.jpg", "Test instructions", ingredients, 4.5f);
        
        assertEquals(1, recipe.getRecipeID());
        assertEquals("Test Recipe", recipe.getTitle());
        assertEquals("thumbnail.jpg", recipe.getThumbnailUrl());
        assertEquals("Test instructions", recipe.getInstructions());
        assertEquals(ingredients, recipe.getIngredients());
        assertEquals(4.5f, recipe.getRating());
    }

    @Test
    void testConstructorWithoutIngredients() {
        Recipe recipe = new Recipe(1, "Test Recipe", "thumbnail.jpg", "Test instructions", 4.5f);
        
        assertEquals(1, recipe.getRecipeID());
        assertEquals("Test Recipe", recipe.getTitle());
        assertEquals("thumbnail.jpg", recipe.getThumbnailUrl());
        assertEquals("Test instructions", recipe.getInstructions());
        assertNull(recipe.getIngredients());
        assertEquals(4.5f, recipe.getRating());
    }

    @Test
    void testSetIngredients() {
        Recipe recipe = new Recipe(1, "Test Recipe", "thumbnail.jpg", "Test instructions", 4.5f);
        ArrayList<RecipeIngredient> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredient(1, 1, 2, 1, "flour", "cups"));
        
        recipe.setIngredients(ingredients);
        assertEquals(ingredients, recipe.getIngredients());
    }

    @Test
    void testToString() {
        ArrayList<RecipeIngredient> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredient(1, 1, 2, 1, "flour", "cups"));
        Recipe recipe = new Recipe(1, "Test Recipe", "thumbnail.jpg", "Test instructions", ingredients, 4.5f);
        
        String expected = "Recipe{recipeID=1, title='Test Recipe', thumbnailUrl='thumbnail.jpg', " +
                         "instructions='Test instructions', ingredients=" + ingredients + ", rating=4.5}";
        assertEquals(expected, recipe.toString());
    }
}