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

        LOGGER.info("=== Recipe '" + recipeName + "' passed validation ===");
        return ValidationResult.success();
    }

    public static ValidationResult validateRecipeExists(String recipeName) {
        if (recipeName == null || recipeName.trim().isEmpty()) {
            String errorMessage = "Recipe name cannot be null or empty";
            LOGGER.warning(errorMessage);
            return ValidationResult.error(errorMessage);
        }

        List<Recipe> recipes = recipeDatabase.getAllRecipes();
        boolean exists = recipes.stream().anyMatch(r -> r.getTitle().equals(recipeName));
        if (!exists) {
            String errorMessage = "Recipe '" + recipeName + "' not found in database";
            LOGGER.warning(errorMessage);
            return ValidationResult.error(errorMessage);
        }
        return ValidationResult.success();
    }
}