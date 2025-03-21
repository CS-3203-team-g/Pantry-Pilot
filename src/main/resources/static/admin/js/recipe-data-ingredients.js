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
        console.log('DEBUG - addIngredientToCurrentRecipe called with:', { ingredientID, quantity, unitName });
        console.log('DEBUG - Current recipe:', recipe);
        
        if (!recipe || !recipe.recipeID) {
            console.error('DEBUG - No valid recipe or recipeID found');
            return;
        }
        
        const unitID = RecipeDataUnits.getUnitIDByName(unitName);
        const newIngredient = {
            recipeID: recipe.recipeID,
            ingredientID,
            quantity: parseFloat(quantity),
            unitName,
            unitID
        };
        
        console.log('DEBUG - New ingredient to add:', newIngredient);
        RecipeDataCore.logIngredientsState("Before Add");
        
        if (RecipeDataCore.draftRecipe && RecipeDataCore.draftRecipe.recipeID === recipe.recipeID) {
            // Initialize draftRecipeIngredients if it doesn't exist
            if (!RecipeDataCore.draftRecipeIngredients) {
                console.log('DEBUG - Initializing empty draftRecipeIngredients array');
                RecipeDataCore.draftRecipeIngredients = [];
            }
            RecipeDataCore.draftRecipeIngredients.push(newIngredient);
            console.log('DEBUG - Added to draftRecipeIngredients, new length:', RecipeDataCore.draftRecipeIngredients.length);
        } else {
            if (!RecipeDataCore.data.recipeIngredients) {
                console.log('DEBUG - Initializing empty recipeIngredients array');
                RecipeDataCore.data.recipeIngredients = [];
            }
            RecipeDataCore.data.recipeIngredients.push(newIngredient);
            console.log('DEBUG - Added to data.recipeIngredients, new length:', RecipeDataCore.data.recipeIngredients.length);
            RecipeDataCore.updateJsonEditor();
        }
        
        RecipeDataCore.logIngredientsState("After Add");
        
        // Update the UI after adding an ingredient
        console.log('DEBUG - About to update UI after adding ingredient');
        if (recipe) {
            // Re-render the ingredients list
            console.log('DEBUG - Calling renderIngredientsList');
            RecipeIngredientsUI.renderIngredientsList(recipe);
            // Update the recipe preview
            console.log('DEBUG - Calling renderRecipePreview');
            RecipeRenderUI.renderRecipePreview();
            // Update the conversion factors list
            console.log('DEBUG - Calling renderConversionFactorsList');
            RecipeConversionUI.renderConversionFactorsList();
        }
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
        console.log('DEBUG - addOrUpdateIngredientInCurrentRecipe called with:', { ingredientName, quantity, unitName, editingIndex });
        console.log('DEBUG - Current recipe:', recipe);
        
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
        
        console.log('DEBUG - Ingredient data to add/update:', ingredientData);

        if (RecipeDataCore.draftRecipe && RecipeDataCore.draftRecipe.recipeID === recipe.recipeID) {
            if (!RecipeDataCore.draftRecipeIngredients) {
                console.log('DEBUG - Initializing empty draftRecipeIngredients array');
                RecipeDataCore.draftRecipeIngredients = [];
            }
            
            if (editingIndex >= 0 && editingIndex < RecipeDataCore.draftRecipeIngredients.length) {
                console.log('DEBUG - Updating existing ingredient at index:', editingIndex);
                RecipeDataCore.draftRecipeIngredients[editingIndex] = ingredientData;
            } else {
                console.log('DEBUG - Adding new ingredient to draftRecipeIngredients');
                RecipeDataCore.draftRecipeIngredients.push(ingredientData);
            }
            console.log('DEBUG - Current draftRecipeIngredients:', RecipeDataCore.draftRecipeIngredients);
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
        
        // Update the UI after adding or updating an ingredient
        console.log('DEBUG - About to update UI after adding/updating ingredient');
        if (recipe) {
            // Re-render the ingredients list
            console.log('DEBUG - Calling renderIngredientsList with recipe:', recipe);
            RecipeIngredientsUI.renderIngredientsList(recipe);
            // Update the recipe preview
            console.log('DEBUG - Calling renderRecipePreview');
            RecipeRenderUI.renderRecipePreview();
            // Update the conversion factors list since ingredients affect it
            console.log('DEBUG - Calling renderConversionFactorsList');
            RecipeConversionUI.renderConversionFactorsList();
        }
        
        return ingredientData;
    }
};

window.RecipeDataIngredients = RecipeDataIngredients;