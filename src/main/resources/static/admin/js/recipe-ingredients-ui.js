// UI for ingredient management
const RecipeIngredientsUI = {
    // Datalist management
    updateIngredientDatalist() {
        const datalist = document.getElementById("existingIngredients");
        const ingredientNameInput = document.getElementById("ingredientName");
        datalist.innerHTML = '';
        
        // Always populate the datalist regardless of input value
        if (RecipeData.data.ingredients && RecipeData.data.ingredients.length > 0) {
            RecipeData.data.ingredients.forEach(ing => {
                if (ing && ing.ingredientName) {
                    let option = document.createElement("option");
                    option.value = ing.ingredientName;
                    datalist.appendChild(option);
                }
            });
        }
        
        // Attach proper event listeners for showing datalist on click/focus
        RecipeUICore.enhanceInputWithAutocomplete(ingredientNameInput, "existingIngredients");
    },

    updateUnitSuggestions() {
        const ingName = document.getElementById("ingredientName").value.trim().toLowerCase();
        const unitInput = document.getElementById("unit");
        const datalist = document.getElementById("unitSuggestions");
        datalist.innerHTML = '';
        
        if (ingName) {
            // If ingredient is selected, show relevant units
            let ingredient = RecipeData.data.ingredients.find(ing =>
                ing.ingredientName && ing.ingredientName.trim().toLowerCase() === ingName
            );

            let mappings = [];
            if (RecipeData.data.ingredientUnits && ingredient) {
                mappings = RecipeData.data.ingredientUnits.filter(mapping =>
                    mapping.ingredientID === ingredient.ingredientID
                );
            }

            if (mappings.length > 0) {
                mappings.forEach(mapping => {
                    let unitObj = RecipeData.data.units.find(u => u.unitID === mapping.unitID);
                    if (unitObj) {
                        let option = document.createElement("option");
                        option.value = unitObj.unitName;
                        datalist.appendChild(option);
                    }
                });
            } else if (ingredient && ingredient.defaultUnitID) {
                let unitObj = RecipeData.data.units.find(u => u.unitID === ingredient.defaultUnitID);
                if (unitObj) {
                    let option = document.createElement("option");
                    option.value = unitObj.unitName;
                    datalist.appendChild(option);
                }
            }
        } else {
            // No ingredient selected, show all available units
            if (RecipeData.data.units && RecipeData.data.units.length > 0) {
                RecipeData.data.units.forEach(unit => {
                    if (unit && unit.unitName) {
                        let option = document.createElement("option");
                        option.value = unit.unitName;
                        datalist.appendChild(option);
                    }
                });
            }
        }
        
        // Attach proper event listeners for showing datalist on click/focus
        RecipeUICore.enhanceInputWithAutocomplete(unitInput, "unitSuggestions");
    },

    // Ingredient form management
    clearIngredientForm() {
        document.getElementById("ingredientName").value = '';
        document.getElementById("quantity").value = '';
        document.getElementById("unit").value = '';
        document.getElementById("editingIngredientIndex").value = '-1';
    },

    loadIngredientIntoForm(ingredient, index) {
        const ingredientObj = RecipeData.data.ingredients.find(i => i.ingredientID === ingredient.ingredientID);
        document.getElementById("ingredientName").value = ingredientObj?.ingredientName || '';
        document.getElementById("quantity").value = ingredient.quantity || '';
        document.getElementById("unit").value = ingredient.unitName || '';
        document.getElementById("editingIngredientIndex").value = index;
    },

    renderIngredientsList(recipe) {
        const ingredientsList = document.getElementById("ingredientsList");
        ingredientsList.innerHTML = '';
        
        if (!recipe || !recipe.recipeID) return;
        
        // Get ingredients for this recipe from recipeIngredients array
        const ingredients = RecipeData.getIngredientsForRecipe(recipe.recipeID);
        
        ingredients.forEach((ing, idx) => {
            let ingredientObj = RecipeData.data.ingredients.find(i => i.ingredientID === ing.ingredientID);
            let ingredientName = ingredientObj ? ingredientObj.ingredientName : "Unknown Ingredient";
            
            const ingItem = document.createElement("div");
            ingItem.className = "mb-2 p-2 border-bottom d-flex justify-content-between align-items-center ingredient-item";
            ingItem.innerHTML = `
                <div class="ingredient-info" data-index="${idx}">
                    <strong>${ing.quantity} ${ing.unitName || ''}</strong> of ${ingredientName}
                </div>
                <div class="ingredient-actions">
                    <button type="button" class="btn btn-sm btn-outline-danger remove-ingredient" data-index="${idx}">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            `;
            
            // Properly set up click-to-edit functionality
            const infoDiv = ingItem.querySelector('.ingredient-info');
            infoDiv.addEventListener('click', () => {
                document.getElementById("ingredientName").value = ingredientName;
                document.getElementById("quantity").value = ing.quantity;
                document.getElementById("unit").value = ing.unitName || '';
                document.getElementById("editingIngredientIndex").value = idx;
                
                // Trigger unit suggestions update immediately
                this.updateUnitSuggestions();
                RecipeRenderUI.renderRecipePreview();
            });

            // Add delete functionality
            const deleteBtn = ingItem.querySelector('.remove-ingredient');
            deleteBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                const index = parseInt(e.currentTarget.getAttribute('data-index'));
                if (index === parseInt(document.getElementById("editingIngredientIndex").value)) {
                    this.clearIngredientForm();
                }
                RecipeData.removeIngredientFromCurrentRecipe(index);
                RecipeUI.loadRecipeIntoForm(RecipeData.getCurrentRecipe());
            });

            ingredientsList.appendChild(ingItem);
        });
    }
};

// Export for use in other modules
window.RecipeIngredientsUI = RecipeIngredientsUI;