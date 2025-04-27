package pro.pantrypilot.validation;

import pro.pantrypilot.db.classes.recipe.Recipe;
import pro.pantrypilot.db.classes.recipe.RecipeDatabase;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;

public class RecipeValidator {
    private static final Logger LOGGER = Logger.getLogger(RecipeValidator.class.getName());
    private static final RecipeDatabase recipeDatabase = new RecipeDatabase();

    public static ValidationResult validateRecipe(String recipeName) {
        LOGGER.info("=== Starting validation for recipe: " + recipeName + " ===");

        if (recipeName == null || recipeName.trim().isEmpty()) {
            String errorMessage = "Recipe name cannot be null or empty";
            LOGGER.warning(errorMessage);
            return ValidationResult.error(errorMessage);
        }

        // Remove the database existence check during loading
        // List<Recipe> recipes = recipeDatabase.getAllRecipes();
        // Recipe foundRecipe = recipes.stream()
        //     .filter(r -> r.getTitle().equals(recipeName))
        //     .findFirst()
        //     .orElse(null);
        //
        // if (foundRecipe == null) {
        //     String errorMessage = "Recipe '" + recipeName + "' not found in database";
        //     LOGGER.log(Level.WARNING, errorMessage);
        //     return ValidationResult.error(errorMessage);
        // }

        LOGGER.info("=== Recipe '" + recipeName + "' passed validation ===");
        return ValidationResult.success();
    }
}