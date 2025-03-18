// UI core functionality
const RecipeUICore = {
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
                                RecipeIngredientsUI.updateUnitSuggestions();
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
                            RecipeIngredientsUI.updateUnitSuggestions();
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
    clearForm() {
        document.getElementById("recipeTitle").value = '';
        document.getElementById("instructions").value = '';
        document.getElementById("thumbnailUrl").value = '';
        document.getElementById("rating").value = '';
        document.getElementById("ingredientsList").innerHTML = '';
    },

    // UI helpers
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
    }
};

// Export for use in other modules
window.RecipeUICore = RecipeUICore;