// Core data management
const RecipeDataCore = {
    data: {
        recipes: [],
        ingredients: [],
        units: [],
        ingredientUnits: [],
        nutritionFacts: [],
        recipeIngredients: []
    },
    currentRecipeIndex: null,
    draftRecipe: null,
    draftRecipeIngredients: [],
    draftIngredientUnits: [],
    draftNutritionFacts: [],

    updateJsonEditor() {
        document.getElementById("jsonEditor").value = JSON.stringify(this.data, null, 2);
    },

    loadFromJson(jsonData) {
        this.data = jsonData;
        if (!this.data.recipeIngredients) {
            this.data.recipeIngredients = [];
        }
        
        if (this.data.recipes) {
            this.data.recipes.forEach(recipe => {
                if (recipe.ingredients && recipe.ingredients.length > 0 && recipe.recipeID) {
                    recipe.ingredients.forEach(ing => {
                        const exists = this.data.recipeIngredients.some(ri => 
                            ri.recipeID === recipe.recipeID && 
                            ri.ingredientID === ing.ingredientID && 
                            ri.quantity === ing.quantity
                        );
                        
                        if (!exists) {
                            this.data.recipeIngredients.push({
                                recipeID: recipe.recipeID,
                                ingredientID: ing.ingredientID,
                                quantity: ing.quantity,
                                unitName: ing.unitName,
                                unitID: RecipeDataUnits.getUnitIDByName(ing.unitName)
                            });
                        }
                    });
                    delete recipe.ingredients;
                }
            });
        }
        
        if (!this.data.ingredientUnits) {
            this.data.ingredientUnits = [];
        }
        
        this.currentRecipeIndex = null;
        this.draftRecipe = null;
        this.draftRecipeIngredients = [];
        this.draftIngredientUnits = [];
        this.updateJsonEditor();
    },

    logIngredientsState(location) {
        const recipe = RecipeDataRecipes.getCurrentRecipe();
        if (recipe && recipe.recipeID) {
            const ingredients = RecipeDataRecipes.getIngredientsForRecipe(recipe.recipeID);
        }
    }
};

window.RecipeDataCore = RecipeDataCore;