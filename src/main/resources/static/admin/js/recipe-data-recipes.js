// Recipe management
const RecipeDataRecipes = {
    createNewRecipe() {
        const newRecipe = {
            title: "",
            instructions: "",
            rating: null,
            thumbnailUrl: "",
            recipeID: this.getNextRecipeID()
        };
        RecipeDataCore.data.recipes.push(newRecipe);
        RecipeDataCore.currentRecipeIndex = RecipeDataCore.data.recipes.length - 1;
        
        this.startEditing();
        RecipeDataCore.updateJsonEditor();
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
        if (RecipeDataCore.currentRecipeIndex === null) return;
        
        const currentRecipe = RecipeDataCore.data.recipes[RecipeDataCore.currentRecipeIndex];
        RecipeDataCore.draftRecipe = JSON.parse(JSON.stringify(currentRecipe));
        
        if (currentRecipe.recipeID) {
            const recipeIngredients = RecipeDataCore.data.recipeIngredients.filter(
                ri => ri.recipeID === currentRecipe.recipeID
            );
            RecipeDataCore.draftRecipeIngredients = recipeIngredients.map(
                ing => JSON.parse(JSON.stringify(ing))
            );
            
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
        if (!RecipeDataCore.draftRecipe || RecipeDataCore.currentRecipeIndex === null) return;
        
        const recipeID = RecipeDataCore.draftRecipe.recipeID;
        RecipeDataCore.data.recipes[RecipeDataCore.currentRecipeIndex] = RecipeDataCore.draftRecipe;
        
        if (recipeID) {
            RecipeDataCore.data.recipeIngredients = RecipeDataCore.data.recipeIngredients.filter(
                ri => ri.recipeID !== recipeID
            );
            
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
        if (RecipeDataCore.draftRecipe && RecipeDataCore.draftRecipe.recipeID === recipeID) {
            return RecipeDataCore.draftRecipeIngredients;
        }
        
        if (!RecipeDataCore.data.recipeIngredients) return [];
        return RecipeDataCore.data.recipeIngredients.filter(ri => ri.recipeID === recipeID);
    }
};

window.RecipeDataRecipes = RecipeDataRecipes;