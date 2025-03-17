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

    updateJsonEditor() {
        document.getElementById("jsonEditor").value = JSON.stringify(this.data, null, 2);
    },

    loadFromJson(jsonData) {
        this.data = jsonData;
        this.currentRecipeIndex = null;
        this.updateJsonEditor();
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
            console.log("getOrCreateUnit: Created new unit:", newUnit);
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
                this.data.ingredientUnits.push({
                    ingredientID: existingIngredient.ingredientID,
                    unitID: unitID,
                    conversionFactor: 1
                });
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
            this.data.ingredientUnits.push({
                ingredientID: newIngredientID,
                unitID: unitID,
                conversionFactor: 1
            });
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
            ingredients: []
        };
        this.data.recipes.push(newRecipe);
        this.currentRecipeIndex = this.data.recipes.length - 1;
        this.updateJsonEditor();
        return newRecipe;
    },

    getCurrentRecipe() {
        return this.currentRecipeIndex !== null ? this.data.recipes[this.currentRecipeIndex] : null;
    },

    updateCurrentRecipe(recipeData) {
        if (this.currentRecipeIndex !== null) {
            this.data.recipes[this.currentRecipeIndex] = {
                ...this.data.recipes[this.currentRecipeIndex],
                ...recipeData
            };
            this.updateJsonEditor();
        }
    },

    addIngredientToCurrentRecipe(ingredientID, quantity, unitName) {
        const recipe = this.getCurrentRecipe();
        if (recipe) {
            if (!recipe.ingredients) { recipe.ingredients = []; }
            recipe.ingredients.push({ ingredientID, quantity, unitName });
            this.updateJsonEditor();
        }
    },

    removeIngredientFromCurrentRecipe(index) {
        const recipe = this.getCurrentRecipe();
        if (recipe && recipe.ingredients) {
            recipe.ingredients.splice(index, 1);
            this.updateJsonEditor();
        }
    }
};

// Export for use in other modules
window.RecipeData = RecipeData;