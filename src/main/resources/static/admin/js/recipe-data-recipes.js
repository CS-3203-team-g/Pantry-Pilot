// Recipe management
const RecipeDataRecipes = {
    createNewRecipe() {
        console.log('DEBUG - createNewRecipe called');
        const newRecipe = {
            title: "",
            instructions: "",
            rating: null,
            thumbnailUrl: "",
            recipeID: this.getNextRecipeID()
        };
        
        // Create the draft recipe directly without adding to the recipes array yet
        // The final recipe will be added to the array only when saved
        RecipeDataCore.draftRecipe = JSON.parse(JSON.stringify(newRecipe));
        RecipeDataCore.draftRecipeIngredients = [];
        RecipeDataCore.draftIngredientUnits = [];
        RecipeDataCore.draftNutritionFacts = [];
        
        // Initialize the recipeIngredients array if it doesn't exist
        if (!RecipeDataCore.data.recipeIngredients) {
            RecipeDataCore.data.recipeIngredients = [];
        }
        
        // Set currentRecipeIndex to null to indicate we're creating a new recipe
        // that hasn't been saved to the recipes array yet
        RecipeDataCore.currentRecipeIndex = null;
        
        RecipeDataCore.updateJsonEditor();
        console.log('DEBUG - New recipe created:', newRecipe);
        console.log('DEBUG - Current draftRecipe:', RecipeDataCore.draftRecipe);
        console.log('DEBUG - Current draftRecipeIngredients:', RecipeDataCore.draftRecipeIngredients);
        
        return newRecipe;
    },

    getNextRecipeID() {
        return RecipeDataCore.data.recipes.length > 0 ? 
            Math.max(...RecipeDataCore.data.recipes.map(r => r.recipeID || 0)) + 1 : 1;
    },

    getCurrentRecipe() {
        if (RecipeDataCore.draftRecipe) {
            return RecipeDataCore.draftRecipe;
        }
        return RecipeDataCore.currentRecipeIndex !== null ? 
            RecipeDataCore.data.recipes[RecipeDataCore.currentRecipeIndex] : null;
    },

    startEditing() {
        console.log('DEBUG - startEditing called, currentRecipeIndex:', RecipeDataCore.currentRecipeIndex);
        
        if (RecipeDataCore.currentRecipeIndex === null) {
            console.log('DEBUG - No currentRecipeIndex, returning early');
            return;
        }
        
        const currentRecipe = RecipeDataCore.data.recipes[RecipeDataCore.currentRecipeIndex];
        console.log('DEBUG - Current recipe being edited:', currentRecipe);
        
        RecipeDataCore.draftRecipe = JSON.parse(JSON.stringify(currentRecipe));
        console.log('DEBUG - Created draftRecipe:', RecipeDataCore.draftRecipe);
        
        if (currentRecipe.recipeID) {
            console.log('DEBUG - Processing recipe with ID:', currentRecipe.recipeID);
            
            // Initialize recipeIngredients if needed
            if (!RecipeDataCore.data.recipeIngredients) {
                console.log('DEBUG - Initializing empty recipeIngredients array');
                RecipeDataCore.data.recipeIngredients = [];
            }
            
            const recipeIngredients = RecipeDataCore.data.recipeIngredients.filter(
                ri => ri.recipeID === currentRecipe.recipeID
            );
            console.log('DEBUG - Filtered recipeIngredients:', recipeIngredients);
            
            RecipeDataCore.draftRecipeIngredients = recipeIngredients.map(
                ing => JSON.parse(JSON.stringify(ing))
            );
            console.log('DEBUG - Set draftRecipeIngredients:', RecipeDataCore.draftRecipeIngredients);
            
            const ingredientIDs = new Set(
                RecipeDataCore.draftRecipeIngredients.map(ri => ri.ingredientID)
            );
            
            const relevantIngredientUnits = RecipeDataCore.data.ingredientUnits.filter(
                iu => ingredientIDs.has(iu.ingredientID)
            );
            RecipeDataCore.draftIngredientUnits = relevantIngredientUnits.map(
                iu => JSON.parse(JSON.stringify(iu))
            );

            if (RecipeDataCore.data.nutritionFacts?.length > 0) {
                const relevantNutritionFacts = RecipeDataCore.data.nutritionFacts.filter(
                    nf => ingredientIDs.has(nf.ingredientID)
                );
                RecipeDataCore.draftNutritionFacts = relevantNutritionFacts.map(
                    nf => JSON.parse(JSON.stringify(nf))
                );
            } else {
                RecipeDataCore.draftNutritionFacts = [];
            }
        } else {
            console.log('DEBUG - New recipe without recipeID, initializing empty arrays');
            RecipeDataCore.draftRecipeIngredients = [];
            RecipeDataCore.draftIngredientUnits = [];
            RecipeDataCore.draftNutritionFacts = [];
        }
    },

    cancelEditing() {
        RecipeDataCore.draftRecipe = null;
        RecipeDataCore.draftRecipeIngredients = [];
        RecipeDataCore.draftIngredientUnits = [];
        RecipeDataCore.draftNutritionFacts = [];
    },

    saveEdits() {
        if (!RecipeDataCore.draftRecipe) return;
        
        const recipeID = RecipeDataCore.draftRecipe.recipeID;
        
        if (RecipeDataCore.currentRecipeIndex === null) {
            // This is a new recipe that hasn't been added to the array yet
            RecipeDataCore.data.recipes.push(RecipeDataCore.draftRecipe);
            RecipeDataCore.currentRecipeIndex = RecipeDataCore.data.recipes.length - 1;
            console.log('DEBUG - Added new recipe to recipes array at index:', RecipeDataCore.currentRecipeIndex);
        } else {
            // This is an existing recipe, update it
            RecipeDataCore.data.recipes[RecipeDataCore.currentRecipeIndex] = RecipeDataCore.draftRecipe;
            console.log('DEBUG - Updated existing recipe at index:', RecipeDataCore.currentRecipeIndex);
        }
        
        if (recipeID) {
            // Remove any existing recipe ingredients for this recipe
            RecipeDataCore.data.recipeIngredients = RecipeDataCore.data.recipeIngredients.filter(
                ri => ri.recipeID !== recipeID
            );
            
            // Add the draft recipe ingredients
            RecipeDataCore.draftRecipeIngredients.forEach(ing => {
                RecipeDataCore.data.recipeIngredients.push(ing);
            });
            
            RecipeDataConversion.syncDraftConversionFactors();
            RecipeDataNutrition.syncDraftNutritionFacts();
        }
        
        this.cancelEditing();
        RecipeDataCore.updateJsonEditor();
    },

    updateCurrentRecipe(recipeData) {
        if (RecipeDataCore.draftRecipe) {
            RecipeDataCore.draftRecipe = {
                ...RecipeDataCore.draftRecipe,
                ...recipeData
            };
        } else if (RecipeDataCore.currentRecipeIndex !== null) {
            RecipeDataCore.data.recipes[RecipeDataCore.currentRecipeIndex] = {
                ...RecipeDataCore.data.recipes[RecipeDataCore.currentRecipeIndex],
                ...recipeData
            };
            RecipeDataCore.updateJsonEditor();
        }
    },

    getIngredientsForRecipe(recipeID) {
        console.log('DEBUG - getIngredientsForRecipe called with recipeID:', recipeID);
        
        if (RecipeDataCore.draftRecipe && RecipeDataCore.draftRecipe.recipeID === recipeID) {
            console.log('DEBUG - Using draftRecipeIngredients:', RecipeDataCore.draftRecipeIngredients);
            return RecipeDataCore.draftRecipeIngredients || [];
        }
        
        if (!RecipeDataCore.data.recipeIngredients) {
            console.log('DEBUG - No recipeIngredients in data, returning empty array');
            return [];
        }
        
        const result = RecipeDataCore.data.recipeIngredients.filter(ri => ri.recipeID === recipeID);
        console.log('DEBUG - Filtered recipeIngredients:', result);
        return result;
    }
};

window.RecipeDataRecipes = RecipeDataRecipes;