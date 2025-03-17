document.addEventListener('DOMContentLoaded', function() {
    loadDashboardData();
    
    // Refresh data when refresh button is clicked
    document.getElementById('refreshStatsBtn').addEventListener('click', function() {
        loadDashboardData();
    });

    // Function to load all dashboard data
    async function loadDashboardData() {
        try {
            // Fetch total users
            const usersResponse = await fetch("/api/admin/getTotalUsers");
            if (usersResponse.ok) {
                const userData = await usersResponse.text();
                document.getElementById('totalUsersCount').textContent = userData;
            }

            // Fetch total recipes
            const recipesResponse = await fetch("/api/admin/getTotalRecipes");
            if (recipesResponse.ok) {
                const recipeData = await recipesResponse.text();
                document.getElementById('totalRecipesCount').textContent = recipeData;
            }

            // Fetch active sessions
            const sessionsResponse = await fetch("/api/admin/getActiveSessions");
            if (sessionsResponse.ok) {
                const sessionData = await sessionsResponse.text();
                document.getElementById('activeSessionsCount').textContent = sessionData;
            }
        } catch (error) {
            console.error("Error loading dashboard data:", error);
            // Show error in stats cards
            document.getElementById('totalUsersCount').textContent = "Error";
            document.getElementById('totalRecipesCount').textContent = "Error";
            document.getElementById('activeSessionsCount').textContent = "Error";
        }
    }
});