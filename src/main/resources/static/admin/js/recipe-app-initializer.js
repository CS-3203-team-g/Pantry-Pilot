// Main application initializer
const RecipeAppInitializer = {
    initializeApp() {
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
                RecipeEventHandlers.initializeEventHandlers(); // Initialize event handlers after Bootstrap loads
            };
        } else {
            // Bootstrap is already loaded
            RecipeEventHandlers.initializeEventHandlers();
        }
    }
};

// Export for use in other modules
window.RecipeAppInitializer = RecipeAppInitializer;