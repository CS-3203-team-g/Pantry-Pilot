// File and API operations
const RecipeIO = {
    async loadFromFile(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = function(event) {
                try {
                    const parsedJson = JSON.parse(event.target.result);
                    resolve(parsedJson);
                } catch (e) {
                    reject(new Error("Invalid JSON file: " + e.message));
                }
            };
            reader.onerror = reject;
            reader.readAsText(file);
        });
    },

    async loadFromDatabase() {
        try {
            const response = await fetch("/api/admin/getRecipeDatabasesJSON");
            if (!response.ok) throw new Error('Failed to load from database');
            return await response.json();
        } catch (error) {
            throw new Error("Error loading recipes: " + error.message);
        }
    },

    downloadJson(jsonData) {
        try {
            const jsonText = JSON.stringify(jsonData, null, 2);
            const blob = new Blob([jsonText], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
            link.download = 'recipes_modified.json';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
        } catch (e) {
            throw new Error("Error downloading JSON: " + e.message);
        }
    }
};

// Export for use in other modules
window.RecipeIO = RecipeIO;