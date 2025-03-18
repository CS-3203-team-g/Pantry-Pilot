// Recipe IO (Input/Output) event handlers
const RecipeIOHandlers = {
    initIOHandlers() {
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
                RecipeRenderUI.renderRecipePreview();
                RecipeIngredientsUI.updateIngredientDatalist();
                RecipeRenderUI.populateMainRecipeList();
            } catch (error) {
                alert(error.message);
            }
        });

        // Load JSON from Database
        document.getElementById("loadFromDbButton")?.addEventListener("click", async function() {
            try {
                const data = await RecipeIO.loadFromDatabase();
                RecipeData.loadFromJson(data);
                RecipeRenderUI.renderRecipePreview();
                RecipeIngredientsUI.updateIngredientDatalist();
                RecipeRenderUI.populateMainRecipeList();
            } catch (error) {
                alert(error.message);
            }
        });

        // Parse JSON button
        document.getElementById("parseJsonButton")?.addEventListener("click", function() {
            try {
                const jsonData = JSON.parse(document.getElementById("jsonEditor").value);
                RecipeData.loadFromJson(jsonData);
                RecipeRenderUI.renderRecipePreview();
                RecipeRenderUI.populateMainRecipeList();
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
    }
};

// Export for use in other modules
window.RecipeIOHandlers = RecipeIOHandlers;