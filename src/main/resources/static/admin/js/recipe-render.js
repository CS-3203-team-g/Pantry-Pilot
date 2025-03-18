// Recipe rendering and display
const RecipeRenderUI = {
    // Recipe preview rendering
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

    // Recipe list rendering
    populateMainRecipeList() {
        const recipeList = document.getElementById("mainRecipeList");
        if (!recipeList) {
            console.error("Error: mainRecipeList element not found!");
            return;
        }
        
        recipeList.innerHTML = '';
        RecipeUICore.updateEmptyMessage(recipeList);
        
        if (RecipeData.data.recipes && RecipeData.data.recipes.length > 0) {
            const emptyMessage = document.getElementById("emptyRecipeListMessage");
            if (emptyMessage) emptyMessage.style.display = 'none';
            
            RecipeData.data.recipes.forEach((recipe, index) => {
                const item = this.createRecipeListItem(recipe, index);
                recipeList.appendChild(item);
            });
        }
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
        RecipeUI.loadRecipeIntoForm(recipe);
        
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
window.RecipeRenderUI = RecipeRenderUI;