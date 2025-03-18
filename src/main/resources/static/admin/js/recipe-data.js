// Global JSON data management
const RecipeData = {
    data: {
        recipes: [],
        ingredients: [],
        units: [],
        ingredientUnits: [],
        nutritionFacts: [],
        recipeIngredients: []
    },
    currentRecipeIndex: null,
    // Store a draft copy of the current recipe and its ingredients when editing
    draftRecipe: null,
    draftRecipeIngredients: [],
    draftIngredientUnits: [],

    updateJsonEditor() {
        document.getElementById("jsonEditor").value = JSON.stringify(this.data, null, 2);
    },

    loadFromJson(jsonData) {
        this.data = jsonData;
        // Ensure recipeIngredients array exists
        if (!this.data.recipeIngredients) {
            this.data.recipeIngredients = [];
        }
        
        // For backwards compatibility - if recipes have ingredients arrays
        // convert them to recipeIngredients
        if (this.data.recipes) {
            this.data.recipes.forEach(recipe => {
                if (recipe.ingredients && recipe.ingredients.length > 0 && recipe.recipeID) {
                    // Convert recipe.ingredients to recipeIngredients entries
                    recipe.ingredients.forEach(ing => {
                        // Check if this ingredient doesn't already exist in recipeIngredients
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
                                unitID: this.getUnitIDByName(ing.unitName)
                            });
                        }
                    });
                    
                    // Remove the ingredients from the recipe after converting
                    delete recipe.ingredients;
                }
            });
        }
        
        // Ensure ingredientUnits array exists
        if (!this.data.ingredientUnits) {
            this.data.ingredientUnits = [];
        }
        
        this.currentRecipeIndex = null;
        this.draftRecipe = null;
        this.draftRecipeIngredients = [];
        this.draftIngredientUnits = [];
        this.updateJsonEditor();
    },

    // Helper to get unitID by name
    getUnitIDByName(unitName) {
        if (!unitName) return null;
        const unit = this.data.units.find(u => u.unitName === unitName);
        return unit ? unit.unitID : null;
    },

    // Helper to get ingredient by name
    getIngredientByName(ingredientName) {
        if (!ingredientName) return null;
        return this.data.ingredients.find(i => 
            i.ingredientName && i.ingredientName.trim().toLowerCase() === ingredientName.trim().toLowerCase()
        );
    },

    // Unit management
    getOrCreateUnit(unitName) {
        if (!this.data.units) { this.data.units = []; }
        const normalizedUnit = unitName.trim().toLowerCase();
        let existingUnit = this.data.units.find(u =>
            u.unitName && u.unitName.trim().toLowerCase() === normalizedUnit
        );
        if (existingUnit) {
            return existingUnit.unitID;
        } else {
            let newUnitID = this.data.units.length > 0 ? Math.max(...this.data.units.map(u => u.unitID)) + 1 : 1;
            let newUnit = {
                unitID: newUnitID,
                unitName: unitName.trim()
            };
            this.data.units.push(newUnit);
            this.updateJsonEditor();
            return newUnitID;
        }
    },

    // Ingredient management
    addOrGetIngredient(ingredientName, unitName) {
        if (!this.data.ingredients) { this.data.ingredients = []; }
        const normalizedName = ingredientName.trim().toLowerCase();
        let existingIngredient = this.data.ingredients.find(ing =>
            ing.ingredientName && ing.ingredientName.trim().toLowerCase() === normalizedName
        );
        let unitID = this.getOrCreateUnit(unitName);

        if (existingIngredient) {
            if (!this.data.ingredientUnits) { this.data.ingredientUnits = []; }
            let mappingExists = this.data.ingredientUnits.some(mapping =>
                mapping.ingredientID === existingIngredient.ingredientID && mapping.unitID === unitID
            );
            if (!mappingExists) {
                this.addOrUpdateConversionFactor(existingIngredient.ingredientID, unitID, 1);
            }
            this.updateJsonEditor();
            return existingIngredient.ingredientID;
        } else {
            let newIngredientID = this.data.ingredients.length > 0 ? 
                Math.max(...this.data.ingredients.map(i => i.ingredientID)) + 1 : 1;
            let newIngredient = {
                ingredientID: newIngredientID,
                ingredientName: ingredientName.trim(),
                defaultUnitID: unitID
            };
            this.data.ingredients.push(newIngredient);
            if (!this.data.ingredientUnits) { this.data.ingredientUnits = []; }
            this.addOrUpdateConversionFactor(newIngredientID, unitID, 1);
            this.updateJsonEditor();
            return newIngredientID;
        }
    },

    // Recipe management
    createNewRecipe() {
        const newRecipe = {
            title: "",
            instructions: "",
            rating: null,
            thumbnailUrl: "",
            recipeID: this.getNextRecipeID()
        };
        this.data.recipes.push(newRecipe);
        this.currentRecipeIndex = this.data.recipes.length - 1;
        
        // Create draft copy
        this.startEditing();
        
        this.updateJsonEditor();
        return newRecipe;
    },

    getNextRecipeID() {
        return this.data.recipes.length > 0 ? Math.max(...this.data.recipes.map(r => r.recipeID || 0)) + 1 : 1;
    },

    getCurrentRecipe() {
        // Return the draft if editing, otherwise return the actual recipe
        if (this.draftRecipe) {
            return this.draftRecipe;
        }
        return this.currentRecipeIndex !== null ? this.data.recipes[this.currentRecipeIndex] : null;
    },
    
    // Debug function to log current state of ingredients
    logIngredientsState(location) {
        const recipe = this.getCurrentRecipe();
        if (recipe && recipe.recipeID) {
            const ingredients = this.getIngredientsForRecipe(recipe.recipeID);
        }
    },
    
    // Start editing a recipe - create draft copies
    startEditing() {
        if (this.currentRecipeIndex === null) return;
        
        // Create a deep copy of the current recipe
        const currentRecipe = this.data.recipes[this.currentRecipeIndex];
        this.draftRecipe = JSON.parse(JSON.stringify(currentRecipe));
        
        // Create copies of all related recipe ingredients
        if (currentRecipe.recipeID) {
            const recipeIngredients = this.data.recipeIngredients.filter(
                ri => ri.recipeID === currentRecipe.recipeID
            );
            this.draftRecipeIngredients = recipeIngredients.map(
                ing => JSON.parse(JSON.stringify(ing))
            );
            
            // Make a copy of ingredientUnits for all ingredients in this recipe
            const ingredientIDs = new Set(
                this.draftRecipeIngredients.map(ri => ri.ingredientID)
            );
            const relevantIngredientUnits = this.data.ingredientUnits.filter(
                iu => ingredientIDs.has(iu.ingredientID)
            );
            this.draftIngredientUnits = relevantIngredientUnits.map(
                iu => JSON.parse(JSON.stringify(iu))
            );
        } else {
            this.draftRecipeIngredients = [];
            this.draftIngredientUnits = [];
        }
    },
    
    // Cancel editing and discard draft
    cancelEditing() {
        this.draftRecipe = null;
        this.draftRecipeIngredients = [];
        this.draftIngredientUnits = [];
    },
    
    // Save edits to the actual data
    saveEdits() {
        if (!this.draftRecipe || this.currentRecipeIndex === null) return;
        
        const recipeID = this.draftRecipe.recipeID;

        // Update the recipe
        this.data.recipes[this.currentRecipeIndex] = this.draftRecipe;
        
        // Remove old recipe ingredients
        if (recipeID) {
            this.data.recipeIngredients = this.data.recipeIngredients.filter(
                ri => ri.recipeID !== recipeID
            );
            
            // Add draft ingredients to the actual data
            this.draftRecipeIngredients.forEach(ing => {
                this.data.recipeIngredients.push(ing);
            });
            
            // Update conversion factors
            this.syncDraftConversionFactors();
        }
        
        // Clear drafts
        this.draftRecipe = null;
        this.draftRecipeIngredients = [];
        this.draftIngredientUnits = [];
        
        this.updateJsonEditor();
    },

    // Synchronize draft conversion factors with the main data
    syncDraftConversionFactors() {
        if (this.draftIngredientUnits.length === 0) return;
        
        // For each draft conversion factor
        this.draftIngredientUnits.forEach(draftCF => {
            // Find matching conversion factor in the main data
            const existingIndex = this.data.ingredientUnits.findIndex(cf => 
                cf.ingredientID === draftCF.ingredientID && 
                cf.unitID === draftCF.unitID
            );
            
            if (existingIndex >= 0) {
                // Update existing conversion factor
                this.data.ingredientUnits[existingIndex] = draftCF;
            } else {
                // Add new conversion factor
                this.data.ingredientUnits.push(draftCF);
            }
        });
    },

    updateCurrentRecipe(recipeData) {
        if (this.draftRecipe) {
            // Update the draft instead of the actual recipe
            this.draftRecipe = {
                ...this.draftRecipe,
                ...recipeData
            };
        } else if (this.currentRecipeIndex !== null) {
            // Fallback to updating the actual recipe directly
            this.data.recipes[this.currentRecipeIndex] = {
                ...this.data.recipes[this.currentRecipeIndex],
                ...recipeData
            };
            this.updateJsonEditor();
        }
    },

    getIngredientsForRecipe(recipeID) {
        // When editing, return draft ingredients instead
        if (this.draftRecipe && this.draftRecipe.recipeID === recipeID) {
            return this.draftRecipeIngredients;
        }
        
        if (!this.data.recipeIngredients) return [];
        return this.data.recipeIngredients.filter(ri => ri.recipeID === recipeID);
    },

    addIngredientToCurrentRecipe(ingredientID, quantity, unitName) {
        const recipe = this.getCurrentRecipe();
        if (!recipe || !recipe.recipeID) return;
        
        const unitID = this.getUnitIDByName(unitName);
        const newIngredient = {
            recipeID: recipe.recipeID,
            ingredientID,
            quantity: parseFloat(quantity),
            unitName,
            unitID
        };
        
        // Log current state before adding
        this.logIngredientsState("Before Add");
        
        if (this.draftRecipe && this.draftRecipe.recipeID === recipe.recipeID) {
            // Add to draft ingredients
            this.draftRecipeIngredients.push(newIngredient);
        } else {
            // Add directly to data
            if (!this.data.recipeIngredients) {
                this.data.recipeIngredients = [];
            }
            this.data.recipeIngredients.push(newIngredient);
            this.updateJsonEditor();
        }
        
        // Log state after adding
        this.logIngredientsState("After Add");
    },

    removeIngredientFromCurrentRecipe(index) {
        const recipe = this.getCurrentRecipe();
        if (!recipe || !recipe.recipeID) return;
        
        // Get ingredients for current recipe
        const ingredients = this.getIngredientsForRecipe(recipe.recipeID);
        if (ingredients.length <= index) return;
        
        // Log current state before removing
        this.logIngredientsState("Before Remove");
        
        if (this.draftRecipe && this.draftRecipe.recipeID === recipe.recipeID) {
            // Remove from draft ingredients
            this.draftRecipeIngredients.splice(index, 1);
        } else {
            // Remove from actual data
            const ingredientToRemove = ingredients[index];
            const globalIndex = this.data.recipeIngredients.findIndex(ri => 
                ri.recipeID === recipe.recipeID && 
                ri.ingredientID === ingredientToRemove.ingredientID && 
                ri.quantity === ingredientToRemove.quantity &&
                ri.unitName === ingredientToRemove.unitName
            );
            
            if (globalIndex !== -1) {
                this.data.recipeIngredients.splice(globalIndex, 1);
                this.updateJsonEditor();
            }
        }
        
        // Log state after removing
        this.logIngredientsState("After Remove");
    },

    addOrUpdateIngredientInCurrentRecipe(ingredientName, quantity, unitName, editingIndex = -1) {
        const recipe = this.getCurrentRecipe();
        if (!recipe || !recipe.recipeID) {
            console.error("Cannot update ingredient: No active recipe");
            return;
        }

        // Log current state before operation
        this.logIngredientsState("Before Update/Add");

        const ingredientID = this.addOrGetIngredient(ingredientName, unitName);
        const unitID = this.getUnitIDByName(unitName);
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

        if (this.draftRecipe && this.draftRecipe.recipeID === recipe.recipeID) {
            // We're in draft mode (editing an existing recipe)
            if (editingIndex >= 0 && editingIndex < this.draftRecipeIngredients.length) {
                // Update existing ingredient in draft
                this.draftRecipeIngredients[editingIndex] = ingredientData;
            } else {
                // Add as new ingredient to draft
                this.draftRecipeIngredients.push(ingredientData);
            }
        } else {
            // Direct update mode (not using draft) - should be rare

            // Ensure recipeIngredients array exists
            if (!this.data.recipeIngredients) {
                this.data.recipeIngredients = [];
            }

            // Get ingredients for this recipe
            const ingredients = this.getIngredientsForRecipe(recipe.recipeID);
            
            if (editingIndex >= 0 && editingIndex < ingredients.length) {
                // Update existing ingredient
                const ingredientToUpdate = ingredients[editingIndex];
                const globalIndex = this.data.recipeIngredients.findIndex(ri => 
                    ri.recipeID === recipe.recipeID && 
                    ri.ingredientID === ingredientToUpdate.ingredientID && 
                    ri.quantity === ingredientToUpdate.quantity &&
                    ri.unitName === ingredientToUpdate.unitName
                );
                
                if (globalIndex !== -1) {
                    this.data.recipeIngredients[globalIndex] = ingredientData;
                } else {
                    this.data.recipeIngredients.push(ingredientData);
                }
            } else {
                // Add new ingredient
                this.data.recipeIngredients.push(ingredientData);
            }
            
            this.updateJsonEditor();
        }
        
        // Log state after operation
        this.logIngredientsState("After Update/Add");
        
        return ingredientData;
    },

    // Conversion Factor Management
    getConversionFactors() {
        // Return draft conversion factors if in draft mode, otherwise return from main data
        return this.draftRecipe ? this.draftIngredientUnits : (this.data.ingredientUnits || []);
    },

    addOrUpdateConversionFactor(ingredientID, unitID, conversionFactor) {
        if (!this.data.ingredientUnits) {
            this.data.ingredientUnits = [];
        }
        
        const existingIndex = this.data.ingredientUnits.findIndex(cf => 
            cf.ingredientID === parseInt(ingredientID) && cf.unitID === parseInt(unitID)
        );
        
        if (existingIndex >= 0) {
            this.data.ingredientUnits[existingIndex].conversionFactor = parseFloat(conversionFactor);
        }
        
        this.updateJsonEditor();
    },

    getConversionFactor(ingredientID, unitID) {
        ingredientID = parseInt(ingredientID);
        unitID = parseInt(unitID);
        
        const conversionFactors = this.getConversionFactors();
        const factor = conversionFactors.find(cf => 
            cf.ingredientID === ingredientID && cf.unitID === unitID
        );
        
        return factor ? factor.conversionFactor : 1; // Default to 1 if not found
    }
};

// Export for use in other modules
window.RecipeData = RecipeData;