// Backwards compatibility facade
const RecipeData = {
    // Core data access
    get data() { return RecipeDataCore.data; },
    get currentRecipeIndex() { return RecipeDataCore.currentRecipeIndex; },
    set currentRecipeIndex(value) { RecipeDataCore.currentRecipeIndex = value; },
    get draftRecipe() { return RecipeDataCore.draftRecipe; },
    get draftRecipeIngredients() { return RecipeDataCore.draftRecipeIngredients; },
    get draftIngredientUnits() { return RecipeDataCore.draftIngredientUnits; },
    get draftNutritionFacts() { return RecipeDataCore.draftNutritionFacts; },

    // Core functions
    updateJsonEditor: () => RecipeDataCore.updateJsonEditor(),
    loadFromJson: jsonData => RecipeDataCore.loadFromJson(jsonData),
    logIngredientsState: location => RecipeDataCore.logIngredientsState(location),

    // Recipe functions
    createNewRecipe: () => RecipeDataRecipes.createNewRecipe(),
    getNextRecipeID: () => RecipeDataRecipes.getNextRecipeID(),
    getCurrentRecipe: () => RecipeDataRecipes.getCurrentRecipe(),
    startEditing: () => RecipeDataRecipes.startEditing(),
    cancelEditing: () => RecipeDataRecipes.cancelEditing(),
    saveEdits: () => RecipeDataRecipes.saveEdits(),
    updateCurrentRecipe: recipeData => RecipeDataRecipes.updateCurrentRecipe(recipeData),
    getIngredientsForRecipe: recipeID => RecipeDataRecipes.getIngredientsForRecipe(recipeID),

    // Unit functions
    getUnitIDByName: unitName => RecipeDataUnits.getUnitIDByName(unitName),
    getOrCreateUnit: unitName => RecipeDataUnits.getOrCreateUnit(unitName),

    // Ingredient functions
    getIngredientByName: ingredientName => RecipeDataIngredients.getIngredientByName(ingredientName),
    addOrGetIngredient: (ingredientName, unitName) => RecipeDataIngredients.addOrGetIngredient(ingredientName, unitName),
    addIngredientToCurrentRecipe: (ingredientID, quantity, unitName) => 
        RecipeDataIngredients.addIngredientToCurrentRecipe(ingredientID, quantity, unitName),
    removeIngredientFromCurrentRecipe: index => RecipeDataIngredients.removeIngredientFromCurrentRecipe(index),
    addOrUpdateIngredientInCurrentRecipe: (ingredientName, quantity, unitName, editingIndex) =>
        RecipeDataIngredients.addOrUpdateIngredientInCurrentRecipe(ingredientName, quantity, unitName, editingIndex),

    // Conversion factor functions
    getConversionFactors: () => RecipeDataConversion.getConversionFactors(),
    addOrUpdateConversionFactor: (ingredientID, unitID, conversionFactor) =>
        RecipeDataConversion.addOrUpdateConversionFactor(ingredientID, unitID, conversionFactor),
    getConversionFactor: (ingredientID, unitID) => RecipeDataConversion.getConversionFactor(ingredientID, unitID),

    // Nutrition functions
    getNutritionFacts: () => RecipeDataNutrition.getNutritionFacts(),
    addOrUpdateNutritionFact: (ingredientID, unitID, nutritionData) =>
        RecipeDataNutrition.addOrUpdateNutritionFact(ingredientID, unitID, nutritionData),
    getNutritionFact: (ingredientID, unitID) => RecipeDataNutrition.getNutritionFact(ingredientID, unitID)
};

// Export for use in other modules
window.RecipeData = RecipeData;