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
        
        if (recipeIngredients.length === 0) {
            if (emptyMessage) emptyMessage.style.display = "block";
            return;
        }
        
        if (emptyMessage) emptyMessage.style.display = "none";
        
        // Create virtual conversion factors for all ingredient-unit combinations in the recipe
        const allConversionFactors = RecipeData.getConversionFactors();
        const virtualConversionFactors = recipeIngredients.map(ri => {
            // Look for existing conversion factor
            const existingFactor = allConversionFactors.find(cf => 
                cf.ingredientID === ri.ingredientID && 
                cf.unitID === ri.unitID
            );
            
            // If it exists, use it; otherwise create a virtual one
            return existingFactor || {
                ingredientID: ri.ingredientID,
                unitID: ri.unitID,
                conversionFactor: 1 // Default value
            };
        });
        
        // Sort by ingredient name, then unit name
        const sortedData = [...virtualConversionFactors].sort((a, b) => {
            const ingredientA = RecipeData.data.ingredients.find(i => i.ingredientID === a.ingredientID);
            const ingredientB = RecipeData.data.ingredients.find(i => i.ingredientID === b.ingredientID);
            const ingredientNameA = ingredientA ? ingredientA.ingredientName : '';
            const ingredientNameB = ingredientB ? ingredientB.ingredientName : '';
            
            if (ingredientNameA !== ingredientNameB) {
                return ingredientNameA.localeCompare(ingredientNameB);
            }
            
            const unitA = RecipeData.data.units.find(u => u.unitID === a.unitID);
            const unitB = RecipeData.data.units.find(u => u.unitID === b.unitID);
            const unitNameA = unitA ? unitA.unitName : '';
            const unitNameB = unitB ? unitB.unitName : '';
            
            return unitNameA.localeCompare(unitNameB);
        });
        
        // Render each conversion factor
        sortedData.forEach((cf, index) => {
            const ingredient = RecipeData.data.ingredients.find(i => i.ingredientID === cf.ingredientID);
            const unit = RecipeData.data.units.find(u => u.unitID === cf.unitID);
            
            if (!ingredient || !unit) return;
            
            const recipeIngredient = recipeIngredients.find(ri => 
                ri.ingredientID === cf.ingredientID && 
                ri.unitID === cf.unitID
            );
            
            if (!recipeIngredient) return; // Skip if not used in recipe
            
            const div = document.createElement("div");
            div.className = "conversion-factor-item d-flex justify-content-between align-items-center";
            div.dataset.index = index;
            
            // Add a visual indicator if this is a default conversion factor
            const isDefault = !allConversionFactors.some(existing => 
                existing.ingredientID === cf.ingredientID && 
                existing.unitID === cf.unitID
            );
            
            div.innerHTML = `
                <div class="d-flex justify-content-between align-items-start w-100">
                    <div class="conversion-factor-info flex-grow-0" style="width: 40%;">
                        <span class="fw-bold">${unit.unitName} of ${ingredient.ingredientName}</span>
                        <span class="text-secondary"> → gram</span>
                        <span class="ms-2 badge ${isDefault ? 'bg-warning' : 'bg-light'} text-dark conversion-factor-display">
                            Factor: ${cf.conversionFactor || 1}
                            ${isDefault ? ' (default)' : ''}
                        </span>
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
                // Get the current recipe to check if we're in draft mode
                const currentRecipe = RecipeData.getCurrentRecipe();
                
                // Update the conversion factor
                if (currentRecipe && currentRecipe === RecipeData.draftRecipe) {
                    // We're in draft mode, update the draft conversion factors
                    const draftIndex = RecipeData.draftIngredientUnits.findIndex(cf => 
                        cf.ingredientID === conversionFactor.ingredientID && 
                        cf.unitID === conversionFactor.unitID
                    );
                    
                    if (draftIndex >= 0) {
                        RecipeData.draftIngredientUnits[draftIndex].conversionFactor = newValue;
                    } else {
                        RecipeData.draftIngredientUnits.push({
                            ingredientID: conversionFactor.ingredientID,
                            unitID: conversionFactor.unitID,
                            conversionFactor: newValue
                        });
                    }
                } else {
                    // Direct update mode
                    RecipeData.addOrUpdateConversionFactor(
                        conversionFactor.ingredientID,
                        conversionFactor.unitID,
                        newValue
                    );
                }
                
                // Update the display
                displaySpan.textContent = `Factor: ${newValue}`;
                displaySpan.style.display = '';
                infoDiv.removeChild(inputGroup);
                
                // Re-enable the action buttons
                actionButtons.forEach(button => button.disabled = false);
                
                // Update the JSON editor and preview
                RecipeData.updateJsonEditor();
                RecipeRenderUI.renderRecipePreview();
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
        // Use the new getNutritionFact method to get the nutrition data
        const nutrition = RecipeData.getNutritionFact(ingredientID, unitID);

        // Only show values that actually exist, don't default to 0
        const formatValue = (val) => val !== null && val !== undefined ? Math.round(val) : '-';
        
        return `
            <div class="nutrition-facts small text-muted mt-1">
                <span class="me-2">Cal: ${formatValue(nutrition?.calories)}</span>
                <span class="me-2">Pro: ${formatValue(nutrition?.protein)}${nutrition?.protein ? 'g' : ''}</span>
                <span class="me-2">Fat: ${formatValue(nutrition?.fat)}${nutrition?.fat ? 'g' : ''}</span>
                <span>Carbs: ${formatValue(nutrition?.carbohydrates)}${nutrition?.carbohydrates ? 'g' : ''}</span>
            </div>
        `;
    },

    enableNutritionEditing(itemElement, ingredientID, unitID) {
        const nutritionDiv = itemElement.querySelector('.nutritional-info');
        if (!nutritionDiv) return;

        // Use the new getNutritionFact method to get the nutrition data
        const currentNutrition = RecipeData.getNutritionFact(ingredientID, unitID);

        const inputGroup = document.createElement('div');
        inputGroup.className = 'nutrition-editor mt-2';
        inputGroup.innerHTML = `
            <div class="d-flex gap-2 mb-2">
                <div class="input-group input-group-sm">
                    <span class="input-group-text">Cal</span>
                    <input type="number" class="form-control nutrition-calories" value="${currentNutrition?.calories ?? ''}" min="0" step="1">
                </div>
                <div class="input-group input-group-sm">
                    <span class="input-group-text">Pro</span>
                    <input type="number" class="form-control nutrition-protein" value="${currentNutrition?.protein ?? ''}" min="0" step="0.1">
                </div>
                <div class="input-group input-group-sm">
                    <span class="input-group-text">Fat</span>
                    <input type="number" class="form-control nutrition-fat" value="${currentNutrition?.fat ?? ''}" min="0" step="0.1">
                </div>
                <div class="input-group input-group-sm">
                    <span class="input-group-text">Carb</span>
                    <input type="number" class="form-control nutrition-carbs" value="${currentNutrition?.carbohydrates ?? ''}" min="0" step="0.1">
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
            // Get values from inputs, using null for empty/invalid values
            const calories = parseFloat(inputGroup.querySelector('.nutrition-calories').value) || null;
            const protein = parseFloat(inputGroup.querySelector('.nutrition-protein').value) || null;
            const fat = parseFloat(inputGroup.querySelector('.nutrition-fat').value) || null;
            const carbs = parseFloat(inputGroup.querySelector('.nutrition-carbs').value) || null;

            // Only save nutrition facts if at least one value is provided
            if ([calories, protein, fat, carbs].every(val => val === null)) {
                nutritionDiv.innerHTML = originalContent;
                return;
            }

            // Use the new addOrUpdateNutritionFact method
            RecipeData.addOrUpdateNutritionFact(ingredientID, unitID, {
                calories,
                protein,
                fat,
                carbohydrates: carbs
            });

            // Update display using the new renderNutritionalInfo method
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