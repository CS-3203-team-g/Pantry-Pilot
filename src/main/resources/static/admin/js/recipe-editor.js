document.addEventListener('DOMContentLoaded', function() {
    // Recipe Editor functionality
    document.getElementById("loadFromFileButton").addEventListener("click", function() {
        document.getElementById("recipeFileInput").click();
    });

    document.getElementById("recipeFileInput").addEventListener("change", function() {
        const file = this.files[0];
        if (!file) return;
        
        const reader = new FileReader();
        reader.onload = function(event) {
            try {
                const content = event.target.result;
                const parsedJson = JSON.parse(content);
                const formattedJson = JSON.stringify(parsedJson, null, 2);
                document.getElementById("jsonEditor").value = formattedJson;
                renderRecipePreview(parsedJson);
            } catch (e) {
                alert("Invalid JSON file: " + e.message);
            }
        };
        reader.readAsText(file);
    });

    document.getElementById("loadFromDbButton").addEventListener("click", function() {
        $.ajax({
            url: "/api/admin/getRecipeDatabasesJSON",
            type: "GET",
            dataType: "json",
            success: function(data) {
                const formattedJson = JSON.stringify(data, null, 2);
                document.getElementById("jsonEditor").value = formattedJson;
                renderRecipePreview(data);
            },
            error: function(xhr, status, error) {
                alert("Error loading recipes: " + error);
            }
        });
    });

    document.getElementById("parseJsonButton").addEventListener("click", function() {
        try {
            const jsonText = document.getElementById("jsonEditor").value;
            const parsedJson = JSON.parse(jsonText);
            renderRecipePreview(parsedJson);
        } catch (e) {
            alert("Invalid JSON: " + e.message);
        }
    });

    document.getElementById("formatJsonButton").addEventListener("click", function() {
        try {
            const jsonText = document.getElementById("jsonEditor").value;
            const parsedJson = JSON.parse(jsonText);
            const formattedJson = JSON.stringify(parsedJson, null, 2);
            document.getElementById("jsonEditor").value = formattedJson;
        } catch (e) {
            alert("Invalid JSON: " + e.message);
        }
    });

    // Download JSON button functionality
    document.getElementById("downloadJsonButton").addEventListener("click", function() {
        try {
            const jsonText = document.getElementById("jsonEditor").value;
            const parsedJson = JSON.parse(jsonText); // Validate JSON
            
            // Create a Blob with the JSON content
            const blob = new Blob([jsonText], { type: 'application/json' });
            
            // Create a URL for the blob
            const url = URL.createObjectURL(blob);
            
            // Create a download link and trigger click
            const link = document.createElement('a');
            link.href = url;
            link.download = 'recipes_modified.json';
            document.body.appendChild(link);
            link.click();
            
            // Clean up
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
        } catch (e) {
            alert("Invalid JSON: " + e.message);
        }
    });

    // Go to Content Management section
    document.getElementById("uploadToContentMgmtButton").addEventListener("click", function() {
        window.location.href = '/admin/content';
    });
    
    // Function to render recipe preview
    function renderRecipePreview(data) {
        const previewContainer = document.getElementById("recipePreview");
        previewContainer.innerHTML = '';
        
        if (!data.recipes || data.recipes.length === 0) {
            previewContainer.innerHTML = '<div class="alert alert-warning">No recipes found in the JSON data</div>';
            return;
        }
        
        let recipesHtml = '';
        data.recipes.forEach((recipe, index) => {
            let ingredientsHtml = '';
            if (recipe.ingredients && recipe.ingredients.length > 0) {
                ingredientsHtml = '<div class="mt-3"><h6>Ingredients:</h6><div class="ingredients-list">';
                recipe.ingredients.forEach(ingredient => {
                    ingredientsHtml += `
                        <div class="ingredient-item">
                            <strong>${ingredient.quantity} ${ingredient.unitName || ''}</strong> of 
                            ${ingredient.ingredientName}
                        </div>`;
                });
                ingredientsHtml += '</div></div>';
            }
            
            recipesHtml += `
                <div class="recipe-card p-3 mb-3">
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
        });
        
        previewContainer.innerHTML = recipesHtml;
    }
    
    // Display a recipe schema template when the page loads
    const schemaTemplate = {
        "recipes": [
            {
                "title": "Sample Recipe",
                "instructions": "Step-by-step instructions go here",
                "rating": 4.5,
                "ingredients": [
                    {
                        "ingredientName": "Sample Ingredient",
                        "quantity": 2,
                        "unitName": "cups"
                    }
                ]
            }
        ]
    };
    
    document.getElementById("jsonEditor").value = JSON.stringify(schemaTemplate, null, 2);
});