const RecipeIngredientHandlers = {
    initIngredientHandlers() {
        document.getElementById("ingredientName")?.addEventListener("input", () => RecipeIngredientsUI.updateUnitSuggestions());
        document.getElementById("ingredientName")?.addEventListener("blur", () => RecipeIngredientsUI.updateUnitSuggestions());

        // Save/Update Ingredient Button
        const saveBtn = document.getElementById("saveIngredientBtn");
        if (saveBtn && !saveBtn.dataset.handlerInitialized) {
            saveBtn.addEventListener("click", function(event) {
                event.preventDefault();
                event.stopPropagation();
                
                if (this.dataset.processing === 'true') {
                    return;
                }
                this.dataset.processing = 'true';
                
                try {
                    const ingredientName = document.getElementById("ingredientName")?.value;
                    const quantity = document.getElementById("quantity")?.value;
                    const unitName = document.getElementById("unit")?.value;
                    const editingIndex = document.getElementById("editingIngredientIndex")?.value;
                    
                    if (!ingredientName || !quantity || !unitName) {
                        alert("Please enter ingredient name, quantity, and unit.");
                        return;
                    }

                    const parsedQuantity = parseFloat(quantity);
                    if (isNaN(parsedQuantity)) {
                        alert("Please enter a valid number for quantity.");
                        return;
                    }

                    RecipeData.addOrUpdateIngredientInCurrentRecipe(
                        ingredientName, 
                        parsedQuantity, 
                        unitName, 
                        parseInt(editingIndex)
                    );
                    
                    RecipeUI.loadRecipeIntoForm(RecipeData.getCurrentRecipe());
                    RecipeRenderUI.renderRecipePreview();
                    RecipeIngredientsUI.clearIngredientForm();
                } finally {
                    this.dataset.processing = 'false';
                }
            });
            
            saveBtn.dataset.handlerInitialized = 'true';
        }
        
        // New Ingredient Button
        document.getElementById("newIngredientBtn")?.addEventListener("click", function() {
            const ingredientName = document.getElementById("ingredientName").value;
            const quantity = parseFloat(document.getElementById("quantity").value);
            const unitName = document.getElementById("unit").value;
            
            if (ingredientName && !isNaN(quantity) && unitName) {
                RecipeData.addOrUpdateIngredientInCurrentRecipe(ingredientName, quantity, unitName);
                RecipeUI.loadRecipeIntoForm(RecipeData.getCurrentRecipe());
                RecipeRenderUI.renderRecipePreview();
            }
            
            RecipeIngredientsUI.clearIngredientForm();
        });

        // Save & New Ingredient Button
        document.getElementById("saveAndNewIngredientBtn")?.addEventListener("click", function() {
            const ingredientName = document.getElementById("ingredientName").value;
            const quantity = parseFloat(document.getElementById("quantity").value);
            const unitName = document.getElementById("unit").value;
            const editingIndex = parseInt(document.getElementById("editingIngredientIndex").value);
            
            if (!ingredientName || isNaN(quantity) || !unitName) {
                alert("Please enter ingredient name, quantity, and unit.");
                return;
            }

            RecipeData.addOrUpdateIngredientInCurrentRecipe(ingredientName, quantity, unitName, editingIndex);
            
            document.getElementById("editingIngredientIndex").value = "-1";
            RecipeUI.loadRecipeIntoForm(RecipeData.getCurrentRecipe());
            RecipeRenderUI.renderRecipePreview();
            
            document.getElementById("ingredientName").value = '';
            document.getElementById("ingredientName").focus();
        });

        // Add Ingredient Button (legacy - kept for backward compatibility)
        document.getElementById("addIngredientBtn")?.addEventListener("click", function() {
            const ingredientName = document.getElementById("ingredientName").value;
            const quantity = parseFloat(document.getElementById("quantity").value);
            const unitName = document.getElementById("unit").value;
            
            if (!ingredientName || isNaN(quantity) || !unitName) {
                alert("Please enter ingredient name, quantity, and unit.");
                return;
            }

            const ingredientID = RecipeData.addOrGetIngredient(ingredientName, unitName);
            RecipeData.addIngredientToCurrentRecipe(ingredientID, quantity, unitName);
            
            document.getElementById("ingredientName").value = '';
            document.getElementById("quantity").value = '';
            document.getElementById("unit").value = '';
            
            RecipeUI.loadRecipeIntoForm(RecipeData.getCurrentRecipe());
            RecipeRenderUI.renderRecipePreview();
        });

        // Initialize conversion factor handlers
        this.initConversionFactorHandlers();
    },

    // Conversion Factor Handlers
    initConversionFactorHandlers() {
        // Show conversion factor form button
        document.getElementById("addConversionFactorBtn")?.addEventListener("click", function() {
            RecipeConversionUI.showConversionFactorForm();
        });

        // Cancel conversion factor button
        document.getElementById("cancelConversionFactorBtn")?.addEventListener("click", function() {
            RecipeConversionUI.hideConversionFactorForm();
        });

        // Ingredient select change - populate units dropdown
        document.getElementById("cfIngredientSelect")?.addEventListener("change", function() {
            const ingredientID = this.value;
            if (ingredientID) {
                RecipeConversionUI.populateUnitsDropdown(ingredientID);
            } else {
                // Clear units if no ingredient selected
                const unitSelect = document.getElementById("cfUnitSelect");
                while (unitSelect.options.length > 1) {
                    unitSelect.remove(1);
                }
            }
        });

        // Save conversion factor button
        document.getElementById("saveConversionFactorBtn")?.addEventListener("click", function() {
            const ingredientID = document.getElementById("cfIngredientSelect").value;
            const unitID = document.getElementById("cfUnitSelect").value;
            const conversionFactor = document.getElementById("cfConversionFactor").value;
            const editingIndex = parseInt(document.getElementById("editingConversionFactorIndex").value);
            
            if (!ingredientID || !unitID || !conversionFactor) {
                alert("Please select an ingredient, unit, and enter a conversion factor.");
                return;
            }
            
            const parsedFactor = parseFloat(conversionFactor);
            if (isNaN(parsedFactor) || parsedFactor <= 0) {
                alert("Please enter a valid positive number for the conversion factor.");
                return;
            }
            
            // Add or update the conversion factor
            RecipeData.addOrUpdateConversionFactor(
                ingredientID,
                unitID,
                parsedFactor,
                editingIndex >= 0 ? editingIndex : -1
            );
            
            // Hide the form and refresh the list
            RecipeConversionUI.hideConversionFactorForm();
            RecipeConversionUI.renderConversionFactorsList();
        });
        
        // Initially render the conversion factors list
        RecipeConversionUI.renderConversionFactorsList();
    }
};

window.RecipeIngredientHandlers = RecipeIngredientHandlers;