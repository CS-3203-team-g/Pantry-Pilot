document.addEventListener('DOMContentLoaded', function() {
    // Check if Bootstrap is loaded properly
    if (typeof bootstrap === 'undefined') {
        console.error("Bootstrap is not loaded! Adding it dynamically...");
        
        // Add Bootstrap CSS if needed
        if (!document.querySelector('link[href*="bootstrap"]')) {
            const bootstrapCSS = document.createElement('link');
            bootstrapCSS.rel = 'stylesheet';
            bootstrapCSS.href = 'https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css';
            document.head.appendChild(bootstrapCSS);
        }
        
        // Add Bootstrap JS
        const bootstrapScript = document.createElement('script');
        bootstrapScript.src = 'https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js';
        document.head.appendChild(bootstrapScript);
        
        // Add jQuery if needed
        if (typeof jQuery === 'undefined') {
            const jQueryScript = document.createElement('script');
            jQueryScript.src = 'https://code.jquery.com/jquery-3.6.0.min.js';
            document.head.appendChild(jQueryScript);
        }
        
        // Wait for scripts to load
        bootstrapScript.onload = function() {
            console.log("Bootstrap loaded successfully!");
            initializeApp(); // Initialize app after Bootstrap loads
        };
    } else {
        // Bootstrap is already loaded
        initializeApp();
    }
    
    function initializeApp() {
        // Initialize the JSON editor with empty data
        RecipeData.updateJsonEditor();

        // Setup event listeners for ingredient input fields
        document.getElementById("ingredientName")?.addEventListener("input", () => RecipeUI.updateUnitSuggestions());
        document.getElementById("ingredientName")?.addEventListener("blur", () => RecipeUI.updateUnitSuggestions());

        // New Recipe Button
        document.getElementById("newRecipeBtn")?.addEventListener("click", function() {
            RecipeUI.clearForm();
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
            
            RecipeUI.renderRecipePreview();
            
            // Close the modal
            const modalElement = document.getElementById('recipeEditorModal');
            const modalInstance = bootstrap.Modal.getInstance(modalElement);
            if (modalInstance) {
                modalInstance.hide();
            }
            
            RecipeUI.populateMainRecipeList();
        });

        // Add Ingredient Button
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
            
            // Clear ingredient input fields
            document.getElementById("ingredientName").value = '';
            document.getElementById("quantity").value = '';
            document.getElementById("unit").value = '';
            
            // Update UI
            RecipeUI.loadRecipeIntoForm(RecipeData.getCurrentRecipe());
        });

        // Load JSON from File
        document.getElementById("loadFromFileButton")?.addEventListener("click", () => {
            document.getElementById("recipeFileInput").click();
        });

        document.getElementById("recipeFileInput")?.addEventListener("change", async function() {
            const file = this.files[0];
            if (!file) return;
            
            try {
                const jsonData = await RecipeIO.loadFromFile(file);
                RecipeData.loadFromJson(jsonData);
                RecipeUI.renderRecipePreview();
                RecipeUI.updateIngredientDatalist();
                RecipeUI.populateMainRecipeList();
            } catch (error) {
                alert(error.message);
            }
        });

        // Load JSON from Database
        document.getElementById("loadFromDbButton")?.addEventListener("click", async function() {
            try {
                const data = await RecipeIO.loadFromDatabase();
                RecipeData.loadFromJson(data);
                RecipeUI.renderRecipePreview();
                RecipeUI.updateIngredientDatalist();
                RecipeUI.populateMainRecipeList();
            } catch (error) {
                alert(error.message);
            }
        });

        // Parse JSON button
        document.getElementById("parseJsonButton")?.addEventListener("click", function() {
            try {
                const jsonData = JSON.parse(document.getElementById("jsonEditor").value);
                RecipeData.loadFromJson(jsonData);
                RecipeUI.renderRecipePreview();
                RecipeUI.populateMainRecipeList();
            } catch (error) {
                alert("Invalid JSON: " + error.message);
            }
        });

        // Format JSON button
        document.getElementById("formatJsonButton")?.addEventListener("click", function() {
            try {
                const jsonData = JSON.parse(document.getElementById("jsonEditor").value);
                RecipeData.loadFromJson(jsonData);
            } catch (error) {
                alert("Invalid JSON: " + error.message);
            }
        });

        // Download JSON Button
        document.getElementById("downloadJsonButton")?.addEventListener("click", function() {
            try {
                RecipeIO.downloadJson(RecipeData.data);
            } catch (error) {
                alert(error.message);
            }
        });

        // Content Management Navigation
        document.getElementById("uploadToContentMgmtButton")?.addEventListener("click", function() {
            window.location.href = '/admin/content';
        });

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
                    RecipeUI.renderRecipePreview();
                }
            });
        });

        // Initialize UI components
        RecipeUI.updateIngredientDatalist();
        RecipeUI.populateMainRecipeList();
    }
});