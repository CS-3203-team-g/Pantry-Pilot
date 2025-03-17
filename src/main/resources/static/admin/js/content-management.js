document.addEventListener('DOMContentLoaded', function() {
    // Recipe database download functionality
    document.getElementById("downloadRecipeButton").addEventListener("click", async function(e) {
        e.preventDefault();
        try {
            const response = await fetch("/api/admin/getRecipeDatabasesJSON");
            if (!response.ok) throw new Error('Failed to download recipe database');
            
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.setAttribute("download", "recipes.json");
            document.body.appendChild(link);
            link.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(link);
        } catch (error) {
            console.error("Error downloading recipe database:", error);
            alert("Error downloading recipe database. Please try again.");
        }
    });
    
    // Recipe database upload functionality
    document.getElementById("uploadRecipeButton").addEventListener("click", function() {
        document.getElementById("fileInput").click();
    });

    document.getElementById("fileInput").addEventListener("change", function() {
        const file = this.files[0];
        if (!file) {
            alert("No file selected!");
            return;
        }

        const reader = new FileReader();
        reader.onload = async function(event) {
            try {
                const jsonData = JSON.parse(event.target.result); // Validate JSON
                const response = await fetch("/api/admin/loadRecipeDatabasesJSON", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(jsonData)
                });

                if (!response.ok) throw new Error('Failed to upload recipe database');
                
                alert("Recipe database uploaded successfully!");
                // Clear the file input for next use
                document.getElementById("fileInput").value = "";
                
            } catch (error) {
                console.error("Error uploading recipe database:", error);
                if (error.name === "SyntaxError") {
                    alert("Invalid JSON file! Please check the file format.");
                } else {
                    alert("Error uploading recipe database. Please try again.");
                }
            }
        };
        reader.readAsText(file);
    });
});