// UI rendering and management
const RecipeUI = {
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
        this.enhanceInputWithAutocomplete(ingredientNameInput, "existingIngredients");
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
        this.enhanceInputWithAutocomplete(unitInput, "unitSuggestions");
    },
    
    // Helper function to enhance input fields with better autocomplete behavior
    enhanceInputWithAutocomplete(inputElement, datalistId) {
        if (!inputElement) return;
        
        // Clone and replace to remove existing listeners
        const newInput = inputElement.cloneNode(true);
        
        // Store a reference to the datalist for later use, but remove the list attribute
        // to prevent the browser's native dropdown from appearing
        const datalistReference = newInput.getAttribute('list');
        newInput.removeAttribute('list');
        
        // Replace the old input with the new one
        inputElement.parentNode.replaceChild(newInput, inputElement);
        
        // Create a custom list element to show instead of relying on the browser's datalist
        let customDropdown = document.getElementById(`custom-dropdown-${datalistId}`);
        if (!customDropdown) {
            customDropdown = document.createElement('div');
            customDropdown.id = `custom-dropdown-${datalistId}`;
            customDropdown.className = 'custom-datalist-dropdown';
            customDropdown.style.display = 'none';
            customDropdown.style.position = 'absolute';
            customDropdown.style.zIndex = '1000';
            customDropdown.style.backgroundColor = 'white';
            customDropdown.style.border = '1px solid #ced4da';
            customDropdown.style.borderRadius = '0.25rem';
            customDropdown.style.padding = '0.25rem 0';
            customDropdown.style.maxHeight = '200px';
            customDropdown.style.overflowY = 'auto';
            customDropdown.style.boxShadow = '0 2px 5px rgba(0,0,0,0.2)';
            newInput.parentNode.appendChild(customDropdown);
        }
        
        // Function to show dropdown with options
        const showDropdown = (filterValue = '') => {
            // Position the dropdown
            const rect = newInput.getBoundingClientRect();
            customDropdown.style.width = `${newInput.offsetWidth}px`;
            customDropdown.style.left = `${newInput.offsetLeft}px`;
            customDropdown.style.top = `${newInput.offsetTop + newInput.offsetHeight}px`;
            
            // Get options from datalist
            const datalist = document.getElementById(datalistId);
            if (!datalist) return;
            
            // Populate custom dropdown with filtered options
            customDropdown.innerHTML = '';
            const options = datalist.querySelectorAll('option');
            
            let matchFound = false;
            
            if (options.length > 0) {
                options.forEach(option => {
                    // Only show options that match the filter (if provided)
                    if (!filterValue || option.value.toLowerCase().includes(filterValue.toLowerCase())) {
                        matchFound = true;
                        const item = document.createElement('div');
                        item.className = 'custom-datalist-item';
                        item.style.padding = '0.25rem 0.5rem';
                        item.style.cursor = 'pointer';
                        item.textContent = option.value;
                        
                        // Highlight the matching part if filtering
                        if (filterValue) {
                            const lowerValue = option.value.toLowerCase();
                            const lowerFilter = filterValue.toLowerCase();
                            const startIndex = lowerValue.indexOf(lowerFilter);
                            
                            if (startIndex >= 0) {
                                const endIndex = startIndex + filterValue.length;
                                const before = option.value.substring(0, startIndex);
                                const match = option.value.substring(startIndex, endIndex);
                                const after = option.value.substring(endIndex);
                                
                                item.innerHTML = `${before}<strong>${match}</strong>${after}`;
                            }
                        }
                        
                        item.addEventListener('mouseenter', function() {
                            this.style.backgroundColor = '#f8f9fa';
                        });
                        item.addEventListener('mouseleave', function() {
                            this.style.backgroundColor = 'transparent';
                        });
                        item.addEventListener('click', function(e) {
                            e.stopPropagation();
                            newInput.value = option.value; // Use the original non-highlighted value
                            customDropdown.style.display = 'none';
                            
                            // If this is the ingredient input, update unit suggestions
                            if (datalistId === 'existingIngredients') {
                                RecipeUI.updateUnitSuggestions();
                            }
                            
                            // Trigger input event to ensure any listeners are notified
                            const event = new Event('input', { bubbles: true });
                            newInput.dispatchEvent(event);
                        });
                        customDropdown.appendChild(item);
                    }
                });
                
                // Show the dropdown if we have matches
                if (matchFound) {
                    customDropdown.style.display = 'block';
                } else {
                    customDropdown.style.display = 'none';
                }
                
                // Hide dropdown when clicking outside
                document.addEventListener('click', function hideDropdown(e) {
                    if (e.target !== newInput && e.target !== customDropdown) {
                        customDropdown.style.display = 'none';
                        document.removeEventListener('click', hideDropdown);
                    }
                });
            }
        };
        
        // Handle click event
        newInput.addEventListener('click', function() {
            showDropdown(this.value);
        });
        
        // Also handle focus event the same way
        newInput.addEventListener('focus', function() {
            showDropdown(this.value);
        });
        
        // Handle typing with our custom dropdown
        newInput.addEventListener('input', function() {
            showDropdown(this.value);
        });
        
        // Handle keyboard navigation
        newInput.addEventListener('keydown', function(e) {
            if (customDropdown.style.display === 'none') return;
            
            const items = customDropdown.querySelectorAll('.custom-datalist-item');
            let activeItem = customDropdown.querySelector('.custom-datalist-item.active');
            let activeIndex = -1;
            
            // Find the index of the current active item
            if (activeItem) {
                items.forEach((item, idx) => {
                    if (item === activeItem) activeIndex = idx;
                });
            }
            
            switch (e.key) {
                case 'ArrowDown':
                    e.preventDefault();
                    if (activeItem) {
                        activeItem.classList.remove('active');
                        activeItem.style.backgroundColor = 'transparent';
                    }
                    
                    activeIndex++;
                    if (activeIndex >= items.length) activeIndex = 0;
                    
                    activeItem = items[activeIndex];
                    activeItem.classList.add('active');
                    activeItem.style.backgroundColor = '#e9ecef';
                    
                    // Ensure the active item is visible
                    customDropdown.scrollTop = activeItem.offsetTop - customDropdown.offsetTop;
                    break;
                    
                case 'ArrowUp':
                    e.preventDefault();
                    if (activeItem) {
                        activeItem.classList.remove('active');
                        activeItem.style.backgroundColor = 'transparent';
                    }
                    
                    activeIndex--;
                    if (activeIndex < 0) activeIndex = items.length - 1;
                    
                    activeItem = items[activeIndex];
                    activeItem.classList.add('active');
                    activeItem.style.backgroundColor = '#e9ecef';
                    
                    // Ensure the active item is visible
                    customDropdown.scrollTop = activeItem.offsetTop - customDropdown.offsetTop;
                    break;
                    
                case 'Enter':
                    if (activeItem) {
                        e.preventDefault();
                        this.value = activeItem.textContent.replace(/<\/?strong>/g, '');
                        customDropdown.style.display = 'none';
                        
                        // If this is the ingredient input, update unit suggestions
                        if (datalistId === 'existingIngredients') {
                            RecipeUI.updateUnitSuggestions();
                        }
                    }
                    break;
                    
                case 'Escape':
                    e.preventDefault();
                    customDropdown.style.display = 'none';
                    break;
            }
        });
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