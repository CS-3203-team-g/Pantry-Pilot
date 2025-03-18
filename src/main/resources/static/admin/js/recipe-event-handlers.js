// Main event handler coordinator
const RecipeEventHandlers = {
    initializeEventHandlers() {
        // Initialize the JSON editor with empty data
        RecipeData.updateJsonEditor();
        
        // Initialize form handlers
        RecipeFormHandlers.initRecipeFormHandlers();
        
        // Initialize ingredient handlers
        RecipeIngredientHandlers.initIngredientHandlers();
        
        // Initialize IO handlers
        RecipeIOHandlers.initIOHandlers();
        
        // Initialize UI components
        RecipeIngredientsUI.updateIngredientDatalist();
        RecipeRenderUI.populateMainRecipeList();
    }
};

// Export for use in other modules
window.RecipeEventHandlers = RecipeEventHandlers;