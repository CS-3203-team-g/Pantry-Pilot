const RecipeIngredientHandlers = {
    // Helper function to parse mixed numbers and fractions
    parseQuantityInput(input) {
        if (!input) return NaN;
        
        // Clean up the input
        input = input.trim();
        
        // If it's a simple decimal number
        const simpleNumber = parseFloat(input);
        if (!isNaN(simpleNumber) && !input.includes(' ') && !input.includes('/')) {
            return simpleNumber;
        }
        
        // Handle mixed numbers (e.g., "1 3/4")
        if (input.includes(' ')) {
            const parts = input.split(' ');
            if (parts.length !== 2) return NaN;
            
            const whole = parseInt(parts[0]);
            const fractionPart = parts[1];
            
            if (isNaN(whole) || !fractionPart.includes('/')) return NaN;
            
            const fractionParts = fractionPart.split('/');
            if (fractionParts.length !== 2) return NaN;
            
            const numerator = parseInt(fractionParts[0]);
            const denominator = parseInt(fractionParts[1]);
            
            if (isNaN(numerator) || isNaN(denominator) || denominator === 0) return NaN;
            
            return whole + (numerator / denominator);
        }
        
        // Handle simple fractions (e.g., "3/4")
        if (input.includes('/')) {
            const parts = input.split('/');
            if (parts.length !== 2) return NaN;
            
            const numerator = parseInt(parts[0]);
            const denominator = parseInt(parts[1]);
            
            if (isNaN(numerator) || isNaN(denominator) || denominator === 0) return NaN;
            
            return numerator / denominator;
        }
        
        return NaN;
    },

    initIngredientHandlers() {
        document.getElementById("ingredientName")?.addEventListener("input", () => RecipeIngredientsUI.updateUnitSuggestions());
        document.getElementById("ingredientName")?.addEventListener("blur", () => RecipeIngredientsUI.updateUnitSuggestions());

        // Save/Update Ingredient Button
        const saveBtn = document.getElementById("saveIngredientBtn");
        if (saveBtn && !saveBtn.dataset.handlerInitialized) {
            saveBtn.addEventListener("click", function(event) {
                console.log('DEBUG - saveIngredientBtn clicked');
                event.preventDefault();
                event.stopPropagation();
                
                if (this.dataset.processing === 'true') {
                    console.log('DEBUG - Already processing, ignoring click');
                    return;
                }
                this.dataset.processing = 'true';
                
                try {
                    const ingredientName = document.getElementById("ingredientName")?.value;
                    const quantityInput = document.getElementById("quantity")?.value;
                    const unitName = document.getElementById("unit")?.value;
                    const editingIndex = document.getElementById("editingIngredientIndex")?.value;
                    
                    console.log('DEBUG - Form values:', { ingredientName, quantityInput, unitName, editingIndex });
                    
                    if (!ingredientName || !quantityInput || !unitName) {
                        console.log('DEBUG - Missing required fields');
                        alert("Please enter ingredient name, quantity, and unit.");
                        return;
                    }
                    
                    // Parse the quantity using our new helper function
                    const parsedQuantity = RecipeIngredientHandlers.parseQuantityInput(quantityInput);
                    if (isNaN(parsedQuantity)) {
                        console.log('DEBUG - Invalid quantity format');
                        alert("Please enter a valid number, fraction (like 3/4), or mixed number (like 1 3/4).");
                        return;
                    }

                    console.log('DEBUG - Parsed quantity:', parsedQuantity);
                    console.log('DEBUG - Calling addOrUpdateIngredientInCurrentRecipe');
                    const result = RecipeData.addOrUpdateIngredientInCurrentRecipe(
                        ingredientName, 
                        parsedQuantity, 
                        unitName, 
                        parseInt(editingIndex)
                    );
                    console.log('DEBUG - Result from addOrUpdateIngredientInCurrentRecipe:', result);
                    
                    // REMOVED: Don't reload the entire recipe form as it resets draftRecipeIngredients
                    // RecipeUI.loadRecipeIntoForm(RecipeData.getCurrentRecipe());
                    
                    // Just update the recipe preview
                    console.log('DEBUG - Calling renderRecipePreview directly');
                    RecipeRenderUI.renderRecipePreview();
                    
                    // Clear the ingredient form without reloading the recipe
                    console.log('DEBUG - Calling clearIngredientForm');
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
            const quantityInput = document.getElementById("quantity").value;
            const unitName = document.getElementById("unit").value;
            
            // Parse the quantity using our new helper function
            const parsedQuantity = RecipeIngredientHandlers.parseQuantityInput(quantityInput);
            
            if (ingredientName && !isNaN(parsedQuantity) && unitName) {
                RecipeData.addOrUpdateIngredientInCurrentRecipe(ingredientName, parsedQuantity, unitName);
                
                // REMOVED: Don't reload the entire recipe form
                // RecipeUI.loadRecipeIntoForm(RecipeData.getCurrentRecipe());
                
                // Just update the recipe preview
                RecipeRenderUI.renderRecipePreview();
            }
            
            RecipeIngredientsUI.clearIngredientForm();
        });

        // Save & New Ingredient Button
        document.getElementById("saveAndNewIngredientBtn")?.addEventListener("click", function() {
            const ingredientName = document.getElementById("ingredientName").value;
            const quantityInput = document.getElementById("quantity").value;
            const unitName = document.getElementById("unit").value;
            const editingIndex = parseInt(document.getElementById("editingIngredientIndex").value);
            
            // Parse the quantity using our new helper function
            const parsedQuantity = RecipeIngredientHandlers.parseQuantityInput(quantityInput);
            
            if (!ingredientName || isNaN(parsedQuantity) || !unitName) {
                alert("Please enter ingredient name, quantity, and unit.");
                return;
            }

            RecipeData.addOrUpdateIngredientInCurrentRecipe(ingredientName, parsedQuantity, unitName, editingIndex);
            
            document.getElementById("editingIngredientIndex").value = "-1";
            
            // REMOVED: Don't reload the entire recipe form
            // RecipeUI.loadRecipeIntoForm(RecipeData.getCurrentRecipe());
            
            // Just update the recipe preview
            RecipeRenderUI.renderRecipePreview();
            
            document.getElementById("ingredientName").value = '';
            document.getElementById("ingredientName").focus();
        });

        // Add Ingredient Button (legacy - kept for backward compatibility)
        document.getElementById("addIngredientBtn")?.addEventListener("click", function() {
            const ingredientName = document.getElementById("ingredientName").value;
            const quantityInput = document.getElementById("quantity").value;
            const unitName = document.getElementById("unit").value;
            
            // Parse the quantity using our new helper function
            const parsedQuantity = RecipeIngredientHandlers.parseQuantityInput(quantityInput);
            
            if (!ingredientName || isNaN(parsedQuantity) || !unitName) {
                alert("Please enter ingredient name, quantity, and unit.");
                return;
            }

            const ingredientID = RecipeData.addOrGetIngredient(ingredientName, unitName);
            RecipeData.addIngredientToCurrentRecipe(ingredientID, parsedQuantity, unitName);
            
            document.getElementById("ingredientName").value = '';
            document.getElementById("quantity").value = '';
            document.getElementById("unit").value = '';
            
            // REMOVED: Don't reload the entire recipe form
            // RecipeUI.loadRecipeIntoForm(RecipeData.getCurrentRecipe());
            
            // Just update the recipe preview
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