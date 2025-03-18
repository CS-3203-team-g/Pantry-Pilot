// UI for conversion factors and nutrition management
const RecipeConversionUI = {
    // Conversion factors management
    populateIngredientsDropdown() {
        const select = document.getElementById("cfIngredientSelect");
        if (!select) return;

        // Clear existing options except the first one
        while (select.options.length > 1) {
            select.remove(1);
        }

        // Add all ingredients to the dropdown
        if (RecipeData.data.ingredients && RecipeData.data.ingredients.length > 0) {
            RecipeData.data.ingredients
                .sort((a, b) => a.ingredientName.localeCompare(b.ingredientName)) // Sort alphabetically
                .forEach(ingredient => {
                    const option = document.createElement("option");
                    option.value = ingredient.ingredientID;
                    option.textContent = ingredient.ingredientName;
                    select.appendChild(option);
                });
        }
    },

    populateUnitsDropdown(ingredientID = null) {
        const select = document.getElementById("cfUnitSelect");
        if (!select) return;

        // Clear existing options except the first one
        while (select.options.length > 1) {
            select.remove(1);
        }

        if (RecipeData.data.units && RecipeData.data.units.length > 0) {
            // If an ingredient is selected, show only units that are used with this ingredient
            let relevantUnits = RecipeData.data.units;

            if (ingredientID) {
                // Get units that are already associated with this ingredient in ingredientUnits
                const unitIDs = RecipeData.data.ingredientUnits
                    .filter(iu => iu.ingredientID === parseInt(ingredientID))
                    .map(iu => iu.unitID);

                relevantUnits = RecipeData.data.units
                    .filter(unit => unitIDs.includes(unit.unitID));
            }

            // Add all relevant units to the dropdown
            relevantUnits
                .sort((a, b) => a.unitName.localeCompare(b.unitName)) // Sort alphabetically
                .forEach(unit => {
                    const option = document.createElement("option");
                    option.value = unit.unitID;
                    option.textContent = unit.unitName;
                    select.appendChild(option);
                });
        }
    },

    showConversionFactorForm() {
        const form = document.getElementById("conversionFactorForm");
        if (form) {
            form.style.display = "flex";
            this.populateIngredientsDropdown();
            this.clearConversionFactorForm();
        }
    },

    hideConversionFactorForm() {
        const form = document.getElementById("conversionFactorForm");
        if (form) {
            form.style.display = "none";
            this.clearConversionFactorForm();
        }
    },

    clearConversionFactorForm() {
        document.getElementById("cfIngredientSelect").selectedIndex = 0;
        document.getElementById("cfUnitSelect").selectedIndex = 0;
        document.getElementById("cfConversionFactor").value = '';
        document.getElementById("editingConversionFactorIndex").value = '-1';
    },

    loadConversionFactorIntoForm(conversionFactor, index) {
        if (!conversionFactor) return;

        const ingredientSelect = document.getElementById("cfIngredientSelect");
        const unitSelect = document.getElementById("cfUnitSelect");

        // Set the ingredient
        for (let i = 0; i < ingredientSelect.options.length; i++) {
            if (parseInt(ingredientSelect.options[i].value) === conversionFactor.ingredientID) {
                ingredientSelect.selectedIndex = i;
                break;
            }
        }

        // Populate units for this ingredient
        this.populateUnitsDropdown(conversionFactor.ingredientID);

        // Set the unit
        for (let i = 0; i < unitSelect.options.length; i++) {
            if (parseInt(unitSelect.options[i].value) === conversionFactor.unitID) {
                unitSelect.selectedIndex = i;
                break;
            }
        }

        // Set the conversion factor value
        document.getElementById("cfConversionFactor").value = conversionFactor.conversionFactor || 1;
        document.getElementById("editingConversionFactorIndex").value = index;

        // Show the form
        this.showConversionFactorForm();
    },

    renderConversionFactorsList() {
        const list = document.getElementById("conversionFactorsList");
        const emptyMessage = document.getElementById("emptyConversionFactorsMessage");
        
        if (!list) return;

        // Clear the list but keep the empty message
        const children = Array.from(list.children);
        children.forEach(child => {
            if (child.id !== "emptyConversionFactorsMessage") {
                list.removeChild(child);
            }
        });
        
        // Get current recipe
        const currentRecipe = RecipeData.getCurrentRecipe();
        if (!currentRecipe || !currentRecipe.recipeID) {
            if (emptyMessage) emptyMessage.style.display = "block";
            return;
        }
        
        // Get ingredients for current recipe
        const recipeIngredients = RecipeData.getIngredientsForRecipe(currentRecipe.recipeID);
        
        // Create a set of ingredient IDs in the current recipe for quick lookup
        const recipeIngredientIds = new Set(recipeIngredients.map(ri => ri.ingredientID));
        
        // Get all ingredient units data that match ingredients in the current recipe
        const allIngredientUnits = RecipeData.data.ingredientUnits || [];
        const relevantIngredientUnits = allIngredientUnits.filter(iu => 
            recipeIngredientIds.has(iu.ingredientID)
        );
        
        if (relevantIngredientUnits.length === 0) {
            if (emptyMessage) emptyMessage.style.display = "block";
            return;
        }
        
        if (emptyMessage) emptyMessage.style.display = "none";
        
        // Sort by ingredient name, then unit name
        const sortedData = [...relevantIngredientUnits].sort(function(a, b) {
            var ingredientA = RecipeData.data.ingredients.find(i => i.ingredientID === a.ingredientID);
            var ingredientB = RecipeData.data.ingredients.find(i => i.ingredientID === b.ingredientID);
            var ingredientNameA = ingredientA ? ingredientA.ingredientName : '';
            var ingredientNameB = ingredientB ? ingredientB.ingredientName : '';
            
            if (ingredientNameA !== ingredientNameB) {
                return ingredientNameA.localeCompare(ingredientNameB);
            }
            
            var unitA = RecipeData.data.units.find(u => u.unitID === a.unitID);
            var unitB = RecipeData.data.units.find(u => u.unitID === b.unitID);
            var unitNameA = unitA ? unitA.unitName : '';
            var unitNameB = unitB ? unitB.unitName : '';
            
            return unitNameA.localeCompare(unitNameB);
        });
        
        // Render each conversion factor
        sortedData.forEach((cf, index) => {
            const ingredient = RecipeData.data.ingredients.find(i => i.ingredientID === cf.ingredientID);
            const unit = RecipeData.data.units.find(u => u.unitID === cf.unitID);
            
            if (!ingredient || !unit) return;
            
            // Find the recipe ingredient to get the unit that's used in the recipe
            const recipeIngredient = recipeIngredients.find(ri => ri.ingredientID === cf.ingredientID);
            const recipeUnitName = recipeIngredient ? recipeIngredient.unitName : '';
            
            const div = document.createElement("div");
            div.className = "conversion-factor-item d-flex justify-content-between align-items-center";
            div.dataset.index = index;
            
            div.innerHTML = `
                <div class="d-flex justify-content-between align-items-start w-100">
                    <div class="conversion-factor-info flex-grow-0" style="width: 40%;">
                        <span class="fw-bold">${recipeUnitName} of ${ingredient.ingredientName}</span>
                        <span class="text-secondary"> → gram</span>
                        <span class="ms-2 badge bg-light text-dark conversion-factor-display">Factor: ${cf.conversionFactor || 1}</span>
                    </div>
                    <div class="nutritional-info flex-grow-1">
                        ${this.renderNutritionalInfo(cf.ingredientID, cf.unitID)}
                    </div>
                    <div class="conversion-factor-actions flex-grow-0">
                        <button class="btn btn-sm btn-outline-primary edit-conversion-factor me-1" data-index="${index}">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-primary edit-nutrition" data-ingredient="${cf.ingredientID}" data-unit="${cf.unitID}">
                            <i class="fas fa-drumstick-bite"></i>
                        </button>
                    </div>
                </div>
            `;

            // Add edit functionality for conversion factor
            const editBtn = div.querySelector('.edit-conversion-factor');
            editBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.enableInlineEditing(div, cf, index);
            });

            // Add nutrition edit functionality
            const nutritionBtn = div.querySelector('.edit-nutrition');
            nutritionBtn.addEventListener('click', (e) => {
                e.preventDefault();
                this.enableNutritionEditing(div, cf.ingredientID, cf.unitID);
            });
            
            list.appendChild(div);
        });
    },

    // New method to handle inline editing
    enableInlineEditing(itemElement, conversionFactor, index) {
        // Get the display span and conversion factor info div
        const displaySpan = itemElement.querySelector('.conversion-factor-display');
        const infoDiv = itemElement.querySelector('.conversion-factor-info');
        const currentValue = conversionFactor.conversionFactor || 1;
        
        // Create input element for inline editing
        const inputGroup = document.createElement('div');
        inputGroup.className = 'input-group input-group-sm conversion-factor-editor';
        inputGroup.innerHTML = `
            <input type="number" step="0.01" min="0.01" class="form-control form-control-sm conversion-factor-input" 
                   value="${currentValue}" style="width: 80px;">
            <button class="btn btn-sm btn-success save-conversion-factor" type="button">
                <i class="fas fa-check"></i>
            </button>
            <button class="btn btn-sm btn-secondary cancel-conversion-factor" type="button">
                <i class="fas fa-times"></i>
            </button>
        `;
        
        // Hide the display span and append the editor
        displaySpan.style.display = 'none';
        infoDiv.appendChild(inputGroup);
        
        // Disable the edit and delete buttons while editing
        const actionButtons = itemElement.querySelectorAll('.conversion-factor-actions button');
        actionButtons.forEach(button => button.disabled = true);
        
        // Focus the input
        const input = inputGroup.querySelector('.conversion-factor-input');
        input.focus();
        input.select();
        
        // Add event listener for the save button
        const saveBtn = inputGroup.querySelector('.save-conversion-factor');
        saveBtn.addEventListener('click', () => {
            const newValue = parseFloat(input.value);
            if (!isNaN(newValue) && newValue > 0) {
                // Update the conversion factor
                const ingredient = RecipeData.data.ingredients.find(i => i.ingredientID === conversionFactor.ingredientID);
                const unit = RecipeData.data.units.find(u => u.unitID === conversionFactor.unitID);
                
                RecipeData.addOrUpdateConversionFactor(
                    conversionFactor.ingredientID,
                    conversionFactor.unitID,
                    newValue,
                    index
                );
                
                // Update the display and revert to display mode
                displaySpan.textContent = `Factor: ${newValue}`;
                displaySpan.style.display = '';
                infoDiv.removeChild(inputGroup);
                
                // Re-enable the action buttons
                actionButtons.forEach(button => button.disabled = false);
                
                // Update the JSON editor
                RecipeData.updateJsonEditor();
            } else {
                alert('Please enter a valid positive number for the conversion factor.');
                input.focus();
            }
        });
        
        // Add event listener for the cancel button
        const cancelBtn = inputGroup.querySelector('.cancel-conversion-factor');
        cancelBtn.addEventListener('click', () => {
            // Revert to display mode without saving
            displaySpan.style.display = '';
            infoDiv.removeChild(inputGroup);
            
            // Re-enable the action buttons
            actionButtons.forEach(button => button.disabled = false);
        });
        
        // Add event listener for Enter key
        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                saveBtn.click();
            } else if (e.key === 'Escape') {
                e.preventDefault();
                cancelBtn.click();
            }
        });
    },

    renderNutritionalInfo(ingredientID, unitID) {
        const nutrition = RecipeData.data.nutritionFacts?.find(n => 
            n.ingredientID === ingredientID && n.unitID === unitID
        ) || { calories: 0, protein: 0, fat: 0, carbohydrates: 0 };

        return `
            <div class="nutrition-facts small text-muted mt-1">
                <span class="me-2">Cal: ${nutrition.calories || 0}</span>
                <span class="me-2">Pro: ${nutrition.protein || 0}g</span>
                <span class="me-2">Fat: ${nutrition.fat || 0}g</span>
                <span>Carbs: ${nutrition.carbohydrates || 0}g</span>
            </div>
        `;
    },

    enableNutritionEditing(itemElement, ingredientID, unitID) {
        const nutritionDiv = itemElement.querySelector('.nutritional-info');
        if (!nutritionDiv) return;

        const currentNutrition = RecipeData.data.nutritionFacts?.find(n => 
            n.ingredientID === ingredientID && n.unitID === unitID
        ) || { calories: 0, protein: 0, fat: 0, carbohydrates: 0 };

        const inputGroup = document.createElement('div');
        inputGroup.className = 'nutrition-editor mt-2';
        inputGroup.innerHTML = `
            <div class="d-flex gap-2 mb-2">
                <div class="input-group input-group-sm">
                    <span class="input-group-text">Cal</span>
                    <input type="number" class="form-control nutrition-calories" value="${currentNutrition.calories || 0}" min="0" step="1">
                </div>
                <div class="input-group input-group-sm">
                    <span class="input-group-text">Pro</span>
                    <input type="number" class="form-control nutrition-protein" value="${currentNutrition.protein || 0}" min="0" step="0.1">
                </div>
                <div class="input-group input-group-sm">
                    <span class="input-group-text">Fat</span>
                    <input type="number" class="form-control nutrition-fat" value="${currentNutrition.fat || 0}" min="0" step="0.1">
                </div>
                <div class="input-group input-group-sm">
                    <span class="input-group-text">Carb</span>
                    <input type="number" class="form-control nutrition-carbs" value="${currentNutrition.carbohydrates || 0}" min="0" step="0.1">
                </div>
            </div>
            <div class="btn-group btn-group-sm">
                <button class="btn btn-success save-nutrition">Save</button>
                <button class="btn btn-secondary cancel-nutrition">Cancel</button>
            </div>
        `;

        // Store original content
        const originalContent = nutritionDiv.innerHTML;

        // Show editor
        nutritionDiv.innerHTML = '';
        nutritionDiv.appendChild(inputGroup);

        // Add event handlers
        const saveBtn = inputGroup.querySelector('.save-nutrition');
        const cancelBtn = inputGroup.querySelector('.cancel-nutrition');

        saveBtn.addEventListener('click', () => {
            const calories = parseFloat(inputGroup.querySelector('.nutrition-calories').value);
            const protein = parseFloat(inputGroup.querySelector('.nutrition-protein').value);
            const fat = parseFloat(inputGroup.querySelector('.nutrition-fat').value);
            const carbs = parseFloat(inputGroup.querySelector('.nutrition-carbs').value);

            if ([calories, protein, fat, carbs].some(isNaN)) {
                alert('Please enter valid numbers for all nutritional values');
                return;
            }

            // Update or add nutrition facts
            const nutritionData = {
                ingredientID,
                unitID,
                calories,
                protein,
                fat,
                carbohydrates: carbs
            };

            if (!RecipeData.data.nutritionFacts) {
                RecipeData.data.nutritionFacts = [];
            }

            const existingIndex = RecipeData.data.nutritionFacts.findIndex(n => 
                n.ingredientID === ingredientID && n.unitID === unitID
            );

            if (existingIndex >= 0) {
                RecipeData.data.nutritionFacts[existingIndex] = nutritionData;
            } else {
                RecipeData.data.nutritionFacts.push(nutritionData);
            }

            // Update display
            nutritionDiv.innerHTML = this.renderNutritionalInfo(ingredientID, unitID);
            RecipeData.updateJsonEditor();
        });

        cancelBtn.addEventListener('click', () => {
            nutritionDiv.innerHTML = originalContent;
        });
    }
};

// Export for use in other modules
window.RecipeConversionUI = RecipeConversionUI;