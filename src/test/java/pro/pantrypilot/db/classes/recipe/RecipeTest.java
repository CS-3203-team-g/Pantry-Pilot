package pro.pantrypilot.db.classes.recipe;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;

class RecipeTest {

    @Test
    void testConstructorAndGettersWithIngredients() {
        ArrayList<RecipeIngredient> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredient(1, 1, 2, "cups", "flour"));
        
        Recipe recipe = new Recipe(1, "Test Recipe", "thumbnail.jpg", "Test instructions", ingredients, 4.5f, null);
        
        assertEquals(1, recipe.getRecipeID());
        assertEquals("Test Recipe", recipe.getTitle());
        assertEquals("thumbnail.jpg", recipe.getThumbnailUrl());
        assertEquals("Test instructions", recipe.getInstructions());
        assertEquals(ingredients, recipe.getIngredients());
        assertEquals(4.5f, recipe.getRating());
    }

    @Test
    void testConstructorWithoutIngredients() {
        Recipe recipe = new Recipe(1, "Test Recipe", "thumbnail.jpg", "Test instructions", 4.5f, null);
        
        assertEquals(1, recipe.getRecipeID());
        assertEquals("Test Recipe", recipe.getTitle());
        assertEquals("thumbnail.jpg", recipe.getThumbnailUrl());
        assertEquals("Test instructions", recipe.getInstructions());
        assertNotNull(recipe.getIngredients());
        assertTrue(recipe.getIngredients().isEmpty());
        assertEquals(4.5f, recipe.getRating());
        assertNull(recipe.getTagsString());
    }

    @Test
    void testSetIngredients() {
        Recipe recipe = new Recipe(1, "Test Recipe", "thumbnail.jpg", "Test instructions", 4.5f, null);
        ArrayList<RecipeIngredient> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredient(1, 1, 2, "cups", "flour"));
        
        recipe.setIngredients(ingredients);
        assertEquals(ingredients, recipe.getIngredients());
    }

    @Test
    void testToString() {
        ArrayList<RecipeIngredient> ingredients = new ArrayList<>();
        ingredients.add(new RecipeIngredient(1, 1, 2, "cups", "flour"));
        Recipe recipe = new Recipe(1, "Test Recipe", "thumbnail.jpg", "Test instructions", ingredients, 4.5f, null);
        
        String expected = "Recipe{recipeID=1, title='Test Recipe', thumbnailUrl='thumbnail.jpg', " +
                         "instructions='Test instructions', ingredients=" + ingredients + ", rating=4.5, tags='null'}";
        assertEquals(expected, recipe.toString());
    }
}