package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDatabase {

    private static final Logger logger = LoggerFactory.getLogger(RecipeDatabase.class);

    // Persistent connection that remains open for the lifetime of the application.
    private static Connection persistentConnection;

    // Initialize or retrieve the persistent connection.
    private static Connection getPersistentConnection() throws SQLException {
        if (persistentConnection == null || persistentConnection.isClosed()) {
            persistentConnection = DatabaseConnectionManager.getConnection();
        }
        return persistentConnection;
    }

    // You can similarly modify other methods to use the persistent connection.
    public static void initializeRecipeDatabase() {
        String createRecipeTableSQL = "CREATE TABLE IF NOT EXISTS recipes (\n"
                + "    recipeID INT AUTO_INCREMENT PRIMARY KEY,\n"
                + "    title VARCHAR(255) NOT NULL,\n"
                + "    instructions TEXT NOT NULL,\n"
                + "    thumbnailUrl VARCHAR(255),\n"
                + "    rating FLOAT CHECK (rating >= 0 AND rating <= 5)\n"
                + ");";
        try {
            // Using the persistent connection here as well.
            Connection conn = getPersistentConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createRecipeTableSQL);
            }
        } catch (SQLException e) {
            logger.error("Error creating recipes table", e);
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Recipe> getRecipesNoIngredients() {
        String getAllRecipesSQL = "SELECT * FROM recipes;";
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            Connection conn = getPersistentConnection();
            try (Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(getAllRecipesSQL)) {

                while (resultSet.next()) {
                    int recipeID = resultSet.getInt("recipeID");
                    String title = resultSet.getString("title");
                    String instructions = resultSet.getString("instructions");
                    String thumbnailUrl = resultSet.getString("thumbnailUrl");
                    float rating = resultSet.getFloat("rating");

                    Recipe recipe = new Recipe(recipeID, title, thumbnailUrl, instructions, new ArrayList<>(), rating);
                    recipes.add(recipe);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recipes", e);
        }
        return recipes;
    }

    public static List<Recipe> getRecipesWithIngredients() {
        String sql = "SELECT r.recipeID, r.title AS recipe_name, r.instructions, r.rating, r.thumbnailUrl, " +
                "i.ingredientID, i.ingredientName AS ingredient_name, " +
                "ri.quantity, ri.unit " +
                "FROM recipes r " +
                "JOIN recipe_ingredients ri ON r.recipeID = ri.recipeID " +
                "JOIN ingredients i ON ri.ingredientID = i.ingredientID";

        List<Recipe> recipes = new ArrayList<>();
        Map<Integer, Recipe> recipeMap = new HashMap<>();

        try {
            Connection conn = getPersistentConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    int recipeID = rs.getInt("recipeID");
                    Recipe recipe = recipeMap.get(recipeID);

                    if (recipe == null) {
                        String title = rs.getString("recipe_name");
                        String instructions = rs.getString("instructions");
                        String thumbnailUrl = rs.getString("thumbnailUrl");
                        float rating = rs.getFloat("rating");
                        recipe = new Recipe(recipeID, title, thumbnailUrl, instructions, new ArrayList<>(), rating);
                        recipeMap.put(recipeID, recipe);
                        recipes.add(recipe);
                    }

                    int ingredientID = rs.getInt("ingredientID");
                    String ingredientName = rs.getString("ingredient_name");
                    int quantity = rs.getInt("quantity");
                    String unit = rs.getString("unit");

                    RecipeIngredient ingredient = new RecipeIngredient(recipeID, ingredientID, quantity, unit, ingredientName);
                    recipe.getIngredients().add(ingredient);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recipes with ingredients", e);
        }

        return recipes;
    }

    public static Recipe getRecipeWithIngredients(int recipeID) {
        String sql = "SELECT r.recipeID, r.title AS recipe_name, r.instructions, r.rating, r.thumbnailUrl, " +
                "i.ingredientID, i.ingredientName AS ingredient_name, " +
                "ri.quantity, ri.unit " +
                "FROM recipes r " +
                "JOIN recipe_ingredients ri ON r.recipeID = ri.recipeID " +
                "JOIN ingredients i ON ri.ingredientID = i.ingredientID " +
                "WHERE r.recipeID = ?";

        Recipe recipe = null;
        try {
            // Use the persistent connection (do not include it in try-with-resources to avoid closing it)
            Connection conn = getPersistentConnection();

            // Use try-with-resources for PreparedStatement and ResultSet to ensure they are closed.
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, recipeID);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        if (recipe == null) {
                            String title = rs.getString("recipe_name");
                            String instructions = rs.getString("instructions");
                            String thumbnailUrl = rs.getString("thumbnailUrl");
                            float rating = rs.getFloat("rating");
                            recipe = new Recipe(recipeID, title, thumbnailUrl, instructions, new ArrayList<>(), rating);
                        }
                        int ingredientID = rs.getInt("ingredientID");
                        String ingredientName = rs.getString("ingredient_name");
                        int quantity = rs.getInt("quantity");
                        String unit = rs.getString("unit");
                        RecipeIngredient ingredient = new RecipeIngredient(recipeID, ingredientID, quantity, unit, ingredientName);
                        recipe.getIngredients().add(ingredient);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recipe with ingredients for recipeID: " + recipeID, e);
        }
        return recipe;
    }

    public static int getTotalRecipeCount() {
        String sql = "SELECT COUNT(*) FROM recipes";
        try {
            Connection conn = getPersistentConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving total recipe count", e);
        }
        return 0;
    }

    public static ArrayList<Recipe> getAllRecipes() {
        String getAllRecipesSQL = "SELECT * FROM recipes;";
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            Connection conn = getPersistentConnection();
            try (Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(getAllRecipesSQL)) {

                while (resultSet.next()) {
                    Recipe recipe = new Recipe(resultSet);
                    recipes.add(recipe);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recipes", e);
        }
        return recipes;
    }

    public static boolean loadAllRecipes(ArrayList<Recipe> recipes) {
        String insertRecipeSQL = "INSERT INTO recipes (recipeID, title, instructions, thumbnailUrl, rating) VALUES (?, ?, ?, ?, ?)";

        try {
            Connection conn = getPersistentConnection();
            try (PreparedStatement preparedStatement = conn.prepareStatement(insertRecipeSQL)) {
                for (Recipe recipe : recipes) {
                    preparedStatement.setInt(1, recipe.getRecipeID());
                    preparedStatement.setString(2, recipe.getTitle());
                    preparedStatement.setString(3, recipe.getInstructions());
                    preparedStatement.setString(4, recipe.getThumbnailUrl());
                    preparedStatement.setFloat(5, recipe.getRating());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        } catch (SQLException e) {
            logger.error("Error replacing all recipes", e);
            return false;
        }
        return true;
    }
}
