// Recipe form event handlers
const RecipeFormHandlers = {
    // Recipe form handlers
    initRecipeFormHandlers() {
        // New Recipe Button
        document.getElementById("newRecipeBtn")?.addEventListener("click", function() {
            RecipeUICore.clearForm();
            const newRecipe = RecipeData.createNewRecipe();
            const recipeEditorModal = new bootstrap.Modal(document.getElementById('recipeEditorModal'));
            recipeEditorModal.show();
        });

        // Save Recipe Button
        document.getElementById("saveRecipeBtn")?.addEventListener("click", function() {
            const recipe = RecipeData.getCurrentRecipe();
            if (!recipe) {
                alert("No current recipe selected. Please create or load a recipe.");
                return;
            }

            RecipeData.updateCurrentRecipe({
                title: document.getElementById("recipeTitle").value,
                instructions: document.getElementById("instructions").value,
                thumbnailUrl: document.getElementById("thumbnailUrl").value,
                rating: parseFloat(document.getElementById("rating").value) || null
            });
            
            // Save the draft to actual data
            RecipeData.saveEdits();
            RecipeRenderUI.renderRecipePreview();
            
            // Close the modal
            const modalElement = document.getElementById('recipeEditorModal');
            const modalInstance = bootstrap.Modal.getInstance(modalElement);
            if (modalInstance) {
                modalInstance.hide();
            }
            
            RecipeRenderUI.populateMainRecipeList();
        });

        // Setup modal events
        this.setupModalEvents();
        
        // Live preview updates
        const formFields = ['recipeTitle', 'instructions', 'thumbnailUrl', 'rating'];
        formFields.forEach(fieldId => {
            document.getElementById(fieldId)?.addEventListener('input', function() {
                if (RecipeData.getCurrentRecipe()) {
                    RecipeData.updateCurrentRecipe({
                        title: document.getElementById("recipeTitle").value,
                        instructions: document.getElementById("instructions").value,
                        thumbnailUrl: document.getElementById("thumbnailUrl").value,
                        rating: parseFloat(document.getElementById("rating").value) || null
                    });
                    RecipeRenderUI.renderRecipePreview();
                }
            });
        });
    },
    
    // Modal event handlers
    setupModalEvents() {
        // Add modal open handler to start editing
        const recipeEditorModal = document.getElementById('recipeEditorModal');
        if (recipeEditorModal) {
            recipeEditorModal.addEventListener('show.bs.modal', function () {
                // Start editing when the modal opens
                if (RecipeData.currentRecipeIndex !== null && !RecipeData.draftRecipe) {
                    RecipeData.startEditing();
                }
                
                RecipeIngredientsUI.updateIngredientDatalist();
                RecipeIngredientsUI.updateUnitSuggestions();
                RecipeRenderUI.renderRecipePreview();
            });
            
            // Add event listener for modal close - cancel editing
            recipeEditorModal.addEventListener('hide.bs.modal', function(event) {
                // Cancel button clicked - discard changes
                if (event.target.classList.contains('btn-close') || 
                    event.relatedTarget?.classList.contains('btn-secondary')) {
                    RecipeData.cancelEditing();
                }
            });
            
            // Modal hidden event - ensure draft is canceled and backdrop removed
            recipeEditorModal.addEventListener('hidden.bs.modal', function() {
                RecipeData.cancelEditing();
                
                // Fix for stuck backdrop
                const backdrop = document.querySelector('.modal-backdrop');
                if (backdrop) {
                    backdrop.parentNode.removeChild(backdrop);
                }
                
                // Additional fix - ensure body can scroll again
                document.body.classList.remove('modal-open');
                document.body.style.overflow = '';
                document.body.style.paddingRight = '';
            });
        }
        
        // Add explicit event listener for cancel button
        document.querySelector('.modal-footer .btn-secondary')?.addEventListener('click', function() {
            RecipeData.cancelEditing();
            
            // Also ensure modal backdrop is removed when cancel is clicked
            const modalElement = document.getElementById('recipeEditorModal');
            const modalInstance = bootstrap.Modal.getInstance(modalElement);
            if (modalInstance) {
                modalInstance.hide();
                
                // Give time for the modal to hide, then force cleanup
                setTimeout(() => {
                    const backdrop = document.querySelector('.modal-backdrop');
                    if (backdrop) {
                        backdrop.parentNode.removeChild(backdrop);
                    }
                    document.body.classList.remove('modal-open');
                    document.body.style.overflow = '';
                    document.body.style.paddingRight = '';
                }, 150);
            }
        });
        
        // Also handle the X button explicitly
        document.querySelector('.modal-header .btn-close')?.addEventListener('click', function() {
            RecipeData.cancelEditing();
        });
    }
};

// Export for use in other modules
window.RecipeFormHandlers = RecipeFormHandlers;