// Recipe UI - Main coordinator module that integrates all UI components
const RecipeUI = {
    // Recipe form management
    loadRecipeIntoForm(recipe) {
        if (!recipe) return;
        
        document.getElementById("recipeTitle").value = recipe.title || '';
        document.getElementById("instructions").value = recipe.instructions || '';
        document.getElementById("thumbnailUrl").value = recipe.thumbnailUrl || '';
        document.getElementById("rating").value = recipe.rating || '';
        
        // Clear the ingredient form
        RecipeIngredientsUI.clearIngredientForm();
        
        // Render the ingredients list and preview
        RecipeIngredientsUI.renderIngredientsList(recipe);
        RecipeConversionUI.renderConversionFactorsList();
        RecipeRenderUI.renderRecipePreview();
        
        // Update ingredient suggestions
        RecipeIngredientsUI.updateIngredientDatalist();
    }
};

// Export for use in other modules
window.RecipeUI = RecipeUI;