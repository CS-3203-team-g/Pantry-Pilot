// UI rendering and management
const RecipeUI = {
    // Datalist management
    updateIngredientDatalist() {
        const datalist = document.getElementById("existingIngredients");
        datalist.innerHTML = '';
        if (RecipeData.data.ingredients && RecipeData.data.ingredients.length > 0) {
            RecipeData.data.ingredients.forEach(ing => {
                if (ing && ing.ingredientName) {
                    let option = document.createElement("option");
                    option.value = ing.ingredientName;
                    datalist.appendChild(option);
                }
            });
        }
    },

    updateUnitSuggestions() {
        const ingName = document.getElementById("ingredientName").value.trim().toLowerCase();
        const datalist = document.getElementById("unitSuggestions");
        datalist.innerHTML = '';
        if (!ingName) return;

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
    },

    // Recipe form management
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

    loadRecipeIntoForm(recipe) {
        if (!recipe) return;
        
        document.getElementById("recipeTitle").value = recipe.title || '';
        document.getElementById("instructions").value = recipe.instructions || '';
        document.getElementById("thumbnailUrl").value = recipe.thumbnailUrl || '';
        document.getElementById("rating").value = recipe.rating || '';
        
        // Clear the ingredient form
        this.clearIngredientForm();
        
        // Render the ingredients list and preview
        this.renderIngredientsList(recipe);
        this.renderRecipePreview();
        
        // Update ingredient suggestions
        this.updateIngredientDatalist();
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
                this.renderRecipePreview();
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
                this.loadRecipeIntoForm(RecipeData.getCurrentRecipe());
            });

            ingredientsList.appendChild(ingItem);
        });
    },

    clearForm() {
        document.getElementById("recipeTitle").value = '';
        document.getElementById("instructions").value = '';
        document.getElementById("thumbnailUrl").value = '';
        document.getElementById("rating").value = '';
        document.getElementById("ingredientsList").innerHTML = '';
    },

    // Recipe preview and list rendering
    renderRecipePreview() {
        const previewContainer = document.getElementById("recipePreview");
        if (!previewContainer) return;

        previewContainer.innerHTML = '';
        const recipe = RecipeData.getCurrentRecipe();
        
        if (!recipe) {
            previewContainer.innerHTML = '<div class="alert alert-info"><i class="fas fa-info-circle me-2"></i>Complete the form above to see a preview of your recipe</div>';
            return;
        }

        let ingredientsHtml = this.generateIngredientsHtml(recipe);
        previewContainer.innerHTML = `
            <div class="recipe-card p-3">
                <h5>${recipe.title || 'Untitled Recipe'}</h5>
                ${recipe.thumbnailUrl ? `<img src="${recipe.thumbnailUrl}" alt="${recipe.title}" class="img-fluid mb-2" style="max-height: 150px;">` : ''}
                <div class="mb-2">
                    <span class="badge bg-info">${recipe.rating ? recipe.rating + ' / 5' : 'No rating'}</span>
                </div>
                <div class="mb-3">
                    <h6>Instructions:</h6>
                    <p>${recipe.instructions || 'No instructions provided'}</p>
                </div>
                ${ingredientsHtml}
            </div>
        `;
    },

    generateIngredientsHtml(recipe) {
        if (!recipe || !recipe.recipeID) return '';
        
        // Get ingredients for this recipe from recipeIngredients array
        const ingredientList = RecipeData.getIngredientsForRecipe(recipe.recipeID);
        
        if (ingredientList.length === 0) return '';

        let html = '<div class="mt-3"><h6>Ingredients:</h6><div class="ingredients-list">';
        ingredientList.forEach(ing => {
            let ingredientObj = RecipeData.data.ingredients?.find(i => i.ingredientID === ing.ingredientID);
            let ingredientDisplayName = ingredientObj ? ingredientObj.ingredientName : "Unknown Ingredient";
            html += `
                <div class="ingredient-item">
                    <strong>${ing.quantity} ${ing.unitName || ''}</strong> of ${ingredientDisplayName}
                </div>`;
        });
        html += '</div></div>';
        return html;
    },

    populateMainRecipeList() {
        const recipeList = document.getElementById("mainRecipeList");
        if (!recipeList) {
            console.error("Error: mainRecipeList element not found!");
            return;
        }
        
        recipeList.innerHTML = '';
        this.updateEmptyMessage(recipeList);
        
        if (RecipeData.data.recipes && RecipeData.data.recipes.length > 0) {
            const emptyMessage = document.getElementById("emptyRecipeListMessage");
            if (emptyMessage) emptyMessage.style.display = 'none';
            
            RecipeData.data.recipes.forEach((recipe, index) => {
                const item = this.createRecipeListItem(recipe, index);
                recipeList.appendChild(item);
            });
        }
    },

    updateEmptyMessage(container) {
        let emptyMessage = document.getElementById("emptyRecipeListMessage");
        if (!emptyMessage) {
            emptyMessage = document.createElement("div");
            emptyMessage.id = "emptyRecipeListMessage";
            emptyMessage.className = "alert alert-info";
            emptyMessage.innerHTML = '<i class="fas fa-info-circle me-2"></i>No recipes available. Create a new recipe or load from database.';
            container.appendChild(emptyMessage);
        }
        emptyMessage.style.display = RecipeData.data.recipes?.length > 0 ? 'none' : 'block';
    },

    createRecipeListItem(recipe, index) {
        const item = document.createElement("button");
        item.type = "button";
        item.className = "list-group-item list-group-item-action d-flex justify-content-between align-items-center";
        
        const titleSpan = document.createElement("span");
        titleSpan.textContent = recipe.title || "Untitled Recipe";
        
        const ratingBadge = document.createElement("span");
        ratingBadge.className = "badge bg-info";
        ratingBadge.textContent = recipe.rating ? `${recipe.rating}/5` : 'No rating';
        
        item.appendChild(titleSpan);
        item.appendChild(ratingBadge);
        
        item.addEventListener("click", () => this.handleRecipeClick(recipe, index));
        
        return item;
    },

    handleRecipeClick(recipe, index) {
        RecipeData.currentRecipeIndex = index;
        this.loadRecipeIntoForm(recipe);
        
        try {
            const recipeEditorModal = document.getElementById('recipeEditorModal');
            if (!recipeEditorModal) {
                console.error("Recipe editor modal element not found!");
                return;
            }
            
            try {
                const bsModal = new bootstrap.Modal(recipeEditorModal);
                bsModal.show();
            } catch (e) {
                console.error("Error using Bootstrap 5 method:", e);
                try {
                    $(recipeEditorModal).modal('show');
                } catch (e2) {
                    console.error("Both methods failed to show modal:", e2);
                    alert("There was an error showing the recipe editor. Please check the console.");
                }
            }
        } catch (error) {
            console.error("Error showing modal:", error);
        }
    }
};

// Export for use in other modules
window.RecipeUI = RecipeUI;