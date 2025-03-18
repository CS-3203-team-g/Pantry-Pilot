// UI for conversion factors management
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
        
        // Get all ingredient units data
        const ingredientUnits = RecipeData.data.ingredientUnits || [];
        
        if (ingredientUnits.length === 0) {
            if (emptyMessage) emptyMessage.style.display = "block";
            return;
        }
        
        if (emptyMessage) emptyMessage.style.display = "none";
        
        // Sort by ingredient name, then unit name
        // Fixed: defining all variables before use to avoid temporal dead zone issues
        const sortedData = [...ingredientUnits].sort(function(a, b) {
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
            
            const div = document.createElement("div");
            div.className = "conversion-factor-item d-flex justify-content-between align-items-center";
            div.innerHTML = `
                <div class="conversion-factor-info">
                    <span class="fw-bold">${ingredient.ingredientName}</span>
                    <span class="text-secondary"> - ${unit.unitName}</span>
                    <span class="ms-2 badge bg-light text-dark">Factor: ${cf.conversionFactor || 1}</span>
                </div>
                <div class="conversion-factor-actions">
                    <button class="btn btn-sm btn-outline-primary edit-conversion-factor me-1" data-index="${index}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger delete-conversion-factor" data-index="${index}">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            `;
            
            // Add edit functionality
            const editBtn = div.querySelector('.edit-conversion-factor');
            editBtn.addEventListener('click', () => {
                this.loadConversionFactorIntoForm(cf, index);
            });
            
            // Add delete functionality
            const deleteBtn = div.querySelector('.delete-conversion-factor');
            deleteBtn.addEventListener('click', () => {
                if (confirm(`Are you sure you want to delete the conversion factor for ${ingredient.ingredientName} (${unit.unitName})?`)) {
                    RecipeData.removeConversionFactor(index);
                    this.renderConversionFactorsList();
                }
            });
            
            list.appendChild(div);
        });
    }
};

// Export for use in other modules
window.RecipeConversionUI = RecipeConversionUI;