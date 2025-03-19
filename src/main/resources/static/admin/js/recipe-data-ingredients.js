// Ingredient management
const RecipeDataIngredients = {
    getIngredientByName(ingredientName) {
        if (!ingredientName) return null;
        return RecipeDataCore.data.ingredients.find(i => 
            i.ingredientName && i.ingredientName.trim().toLowerCase() === ingredientName.trim().toLowerCase()
        );
    },

    addOrGetIngredient(ingredientName, unitName) {
        if (!RecipeDataCore.data.ingredients) { RecipeDataCore.data.ingredients = []; }
        const normalizedName = ingredientName.trim().toLowerCase();
        let existingIngredient = this.getIngredientByName(ingredientName);
        let unitID = RecipeDataUnits.getOrCreateUnit(unitName);

        if (existingIngredient) {
            if (!RecipeDataCore.data.ingredientUnits) { RecipeDataCore.data.ingredientUnits = []; }
            let mappingExists = RecipeDataCore.data.ingredientUnits.some(mapping =>
                mapping.ingredientID === existingIngredient.ingredientID && 
                mapping.unitID === unitID
            );
            if (!mappingExists) {
                RecipeDataConversion.addOrUpdateConversionFactor(existingIngredient.ingredientID, unitID, 1);
            }
            RecipeDataCore.updateJsonEditor();
            return existingIngredient.ingredientID;
        } else {
            let newIngredientID = RecipeDataCore.data.ingredients.length > 0 ? 
                Math.max(...RecipeDataCore.data.ingredients.map(i => i.ingredientID)) + 1 : 1;
            let newIngredient = {
                ingredientID: newIngredientID,
                ingredientName: ingredientName.trim(),
                defaultUnitID: unitID
            };
            RecipeDataCore.data.ingredients.push(newIngredient);
            if (!RecipeDataCore.data.ingredientUnits) { RecipeDataCore.data.ingredientUnits = []; }
            RecipeDataConversion.addOrUpdateConversionFactor(newIngredientID, unitID, 1);
            RecipeDataCore.updateJsonEditor();
            return newIngredientID;
        }
    },

    addIngredientToCurrentRecipe(ingredientID, quantity, unitName) {
        const recipe = RecipeDataRecipes.getCurrentRecipe();
        if (!recipe || !recipe.recipeID) return;
        
        const unitID = RecipeDataUnits.getUnitIDByName(unitName);
        const newIngredient = {
            recipeID: recipe.recipeID,
            ingredientID,
            quantity: parseFloat(quantity),
            unitName,
            unitID
        };
        
        RecipeDataCore.logIngredientsState("Before Add");
        
        if (RecipeDataCore.draftRecipe && RecipeDataCore.draftRecipe.recipeID === recipe.recipeID) {
            RecipeDataCore.draftRecipeIngredients.push(newIngredient);
        } else {
            if (!RecipeDataCore.data.recipeIngredients) {
                RecipeDataCore.data.recipeIngredients = [];
            }
            RecipeDataCore.data.recipeIngredients.push(newIngredient);
            RecipeDataCore.updateJsonEditor();
        }
        
        RecipeDataCore.logIngredientsState("After Add");
    },

    removeIngredientFromCurrentRecipe(index) {
        const recipe = RecipeDataRecipes.getCurrentRecipe();
        if (!recipe || !recipe.recipeID) return;
        
        const ingredients = RecipeDataRecipes.getIngredientsForRecipe(recipe.recipeID);
        if (ingredients.length <= index) return;
        
        RecipeDataCore.logIngredientsState("Before Remove");
        
        if (RecipeDataCore.draftRecipe && RecipeDataCore.draftRecipe.recipeID === recipe.recipeID) {
            RecipeDataCore.draftRecipeIngredients.splice(index, 1);
        } else {
            const ingredientToRemove = ingredients[index];
            const globalIndex = RecipeDataCore.data.recipeIngredients.findIndex(ri => 
                ri.recipeID === recipe.recipeID && 
                ri.ingredientID === ingredientToRemove.ingredientID && 
                ri.quantity === ingredientToRemove.quantity &&
                ri.unitName === ingredientToRemove.unitName
            );
            
            if (globalIndex !== -1) {
                RecipeDataCore.data.recipeIngredients.splice(globalIndex, 1);
                RecipeDataCore.updateJsonEditor();
            }
        }
        
        RecipeDataCore.logIngredientsState("After Remove");
    },

    addOrUpdateIngredientInCurrentRecipe(ingredientName, quantity, unitName, editingIndex = -1) {
        const recipe = RecipeDataRecipes.getCurrentRecipe();
        if (!recipe || !recipe.recipeID) {
            console.error("Cannot update ingredient: No active recipe");
            return;
        }

        RecipeDataCore.logIngredientsState("Before Update/Add");

        const ingredientID = this.addOrGetIngredient(ingredientName, unitName);
        const unitID = RecipeDataUnits.getUnitIDByName(unitName);
        const parsedQuantity = parseFloat(quantity);
        
        if (isNaN(parsedQuantity)) {
            console.error("Invalid quantity:", quantity);
            return;
        }
        
        const ingredientData = { 
            recipeID: recipe.recipeID,
            ingredientID, 
            quantity: parsedQuantity,
            unitName,
            unitID
        };

        if (RecipeDataCore.draftRecipe && RecipeDataCore.draftRecipe.recipeID === recipe.recipeID) {
            if (editingIndex >= 0 && editingIndex < RecipeDataCore.draftRecipeIngredients.length) {
                RecipeDataCore.draftRecipeIngredients[editingIndex] = ingredientData;
            } else {
                RecipeDataCore.draftRecipeIngredients.push(ingredientData);
            }
        } else {
            if (!RecipeDataCore.data.recipeIngredients) {
                RecipeDataCore.data.recipeIngredients = [];
            }

            const ingredients = RecipeDataRecipes.getIngredientsForRecipe(recipe.recipeID);
            
            if (editingIndex >= 0 && editingIndex < ingredients.length) {
                const ingredientToUpdate = ingredients[editingIndex];
                const globalIndex = RecipeDataCore.data.recipeIngredients.findIndex(ri => 
                    ri.recipeID === recipe.recipeID && 
                    ri.ingredientID === ingredientToUpdate.ingredientID && 
                    ri.quantity === ingredientToUpdate.quantity &&
                    ri.unitName === ingredientToUpdate.unitName
                );
                
                if (globalIndex !== -1) {
                    RecipeDataCore.data.recipeIngredients[globalIndex] = ingredientData;
                } else {
                    RecipeDataCore.data.recipeIngredients.push(ingredientData);
                }
            } else {
                RecipeDataCore.data.recipeIngredients.push(ingredientData);
            }
            
            RecipeDataCore.updateJsonEditor();
        }
        
        RecipeDataCore.logIngredientsState("After Update/Add");
        return ingredientData;
    }
};

window.RecipeDataIngredients = RecipeDataIngredients;