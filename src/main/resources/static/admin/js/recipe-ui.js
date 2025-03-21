// Recipe UI - Main coordinator module that integrates all UI components
const RecipeUI = {
    // Recipe form management
    loadRecipeIntoForm(recipe) {
        console.log('DEBUG - loadRecipeIntoForm called with recipe:', recipe);
        
        if (!recipe) {
            console.log('DEBUG - No recipe provided, returning early');
            return;
        }
        
        document.getElementById("recipeTitle").value = recipe.title || '';
        document.getElementById("instructions").value = recipe.instructions || '';
        document.getElementById("thumbnailUrl").value = recipe.thumbnailUrl || '';
        document.getElementById("rating").value = recipe.rating || '';
        
        // Clear the ingredient form
        console.log('DEBUG - Clearing ingredient form');
        RecipeIngredientsUI.clearIngredientForm();
        
        // Start editing to create draft copies
        console.log('DEBUG - Calling startEditing to initialize draft copies');
        RecipeData.startEditing();
        
        // Render the ingredients list and preview
        console.log('DEBUG - Recipe ingredients list before rendering:', 
            RecipeData.getIngredientsForRecipe(recipe.recipeID));
        console.log('DEBUG - Calling renderIngredientsList');
        RecipeIngredientsUI.renderIngredientsList(recipe);
        console.log('DEBUG - Calling renderConversionFactorsList');
        RecipeConversionUI.renderConversionFactorsList();
        console.log('DEBUG - Calling renderRecipePreview');
        RecipeRenderUI.renderRecipePreview();
        
        // Update ingredient suggestions
        console.log('DEBUG - Updating ingredient suggestions');
        RecipeIngredientsUI.updateIngredientDatalist();
    },

    // Save recipe changes
    saveRecipe() {
        console.log('DEBUG - saveRecipe called');
        RecipeData.saveEdits();
        RecipeRenderUI.renderRecipePreview();
        RecipeConversionUI.renderConversionFactorsList();
    },

    // Cancel recipe editing
    cancelEditing() {
        RecipeData.cancelEditing();
        RecipeRenderUI.renderRecipePreview();
        RecipeConversionUI.renderConversionFactorsList();
    }
};

// Export for use in other modules
window.RecipeUI = RecipeUI;