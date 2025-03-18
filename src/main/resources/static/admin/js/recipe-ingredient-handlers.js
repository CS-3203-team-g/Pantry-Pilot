const RecipeIngredientHandlers = {
    initIngredientHandlers() {
        document.getElementById("ingredientName")?.addEventListener("input", () => RecipeUI.updateUnitSuggestions());
        document.getElementById("ingredientName")?.addEventListener("blur", () => RecipeUI.updateUnitSuggestions());

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
                    RecipeUI.renderRecipePreview();
                    RecipeUI.clearIngredientForm();
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
                RecipeUI.renderRecipePreview();
            }
            
            RecipeUI.clearIngredientForm();
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
            RecipeUI.renderRecipePreview();
            
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
            RecipeUI.renderRecipePreview();
        });
    }
};

window.RecipeIngredientHandlers = RecipeIngredientHandlers;