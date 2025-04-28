package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.ai.RecipeAiTagger;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // Step 1: Ensure table exists (using CREATE TABLE IF NOT EXISTS)
        // The existing CREATE TABLE statement already includes the 'tags' column
        // for cases where the table is newly created.
        String createRecipeTableSQL = "CREATE TABLE IF NOT EXISTS recipes (\n"
                + "    recipeID INT AUTO_INCREMENT PRIMARY KEY,\n"
                + "    title VARCHAR(255) NOT NULL,\n"
                + "    instructions TEXT NOT NULL,\n"
                + "    thumbnailUrl VARCHAR(255),\n"
                + "    rating FLOAT CHECK (rating >= 0 AND rating <= 5)\n"
                + ");";
        try (Connection conn = getPersistentConnection(); // Or DatabaseConnectionManager.getConnection()
             Statement stmt = conn.createStatement()) {
            logger.info("Ensuring 'recipes' table exists...");
            stmt.execute(createRecipeTableSQL);
            logger.info("'recipes' table check complete.");
        } catch (SQLException e) {
            logger.error("Error ensuring 'recipes' table exists", e);
            throw new RuntimeException(e); // Rethrow critical error during init
        }

        // Step 2: Check if the 'tags' column exists and add it if not.
        // This handles cases where the table already existed but without the 'tags' column.
        try (Connection conn = DatabaseConnectionManager.getConnection()) { // Use standard connection potentially pooled
            String dbName = System.getenv("DB_NAME"); // Get DB name for schema query
            if (dbName == null || dbName.trim().isEmpty()) {
                logger.warn("DB_NAME environment variable not set, cannot reliably check for 'tags' column existence. Skipping check.");
                return; // Skip check if DB name is unknown
            }

            String checkColumnSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                                    "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?";
            boolean columnExists = false;

            logger.debug("Checking for 'tags' column in table 'recipes' within schema '{}'...", dbName);
            try (PreparedStatement pstmtCheck = conn.prepareStatement(checkColumnSql)) {
                pstmtCheck.setString(1, dbName);
                pstmtCheck.setString(2, "recipes");
                pstmtCheck.setString(3, "tags");
                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        columnExists = rs.getInt(1) > 0;
                    }
                }
            }

            if (!columnExists) {
                logger.info("'tags' column not found in 'recipes' table. Attempting to add it...");
                String addColumnSql = "ALTER TABLE recipes ADD COLUMN tags TEXT NULL";
                try (Statement stmtAlter = conn.createStatement()) {
                    stmtAlter.execute(addColumnSql);
                    logger.info("Successfully added 'tags' column to 'recipes' table.");
                } catch (SQLException alterEx) {
                     // Log error if ALTER TABLE fails for reasons other than column already existing (e.g., lack of permissions)
                     // MariaDB error 1060 is 'Duplicate column name', which we can ignore here.
                     if (alterEx.getErrorCode() == 1060) { 
                         logger.warn("Attempted to add 'tags' column, but it already exists (Error Code {}).", alterEx.getErrorCode());
                     } else {
                         logger.error("Error adding 'tags' column to 'recipes' table.", alterEx);
                     } 
                     // Depending on strictness, you might re-throw non-1060 errors
                }
            } else {
                logger.debug("'tags' column already exists in 'recipes' table.");
            }

        } catch (SQLException e) {
            logger.error("SQL error during 'tags' column check/addition in 'recipes' table.", e);
            // Decide if this error is critical enough to stop initialization
        } catch (Exception e) {
             logger.error("Unexpected error during 'tags' column check/addition in 'recipes' table.", e);
        }
    }

    public static ArrayList<Recipe> getRecipesNoIngredients() {
        String getAllRecipesSQL = "SELECT recipeID, title, thumbnailUrl, instructions, rating, tags FROM recipes;";
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            Connection conn = getPersistentConnection();
            try (Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(getAllRecipesSQL)) {

                while (resultSet.next()) {
                    recipes.add(new Recipe(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recipes", e);
        }
        return recipes;
    }

    public static List<Recipe> getRecipesWithIngredients() {
        String sql = "SELECT r.recipeID, r.title AS recipe_name, r.instructions, r.rating, r.thumbnailUrl, r.tags, " +
                "i.ingredientID, i.ingredientName AS ingredient_name, " +
                "ri.quantity, ri.unit " +
                "FROM recipes r " +
                "LEFT JOIN recipe_ingredients ri ON r.recipeID = ri.recipeID " +
                "LEFT JOIN ingredients i ON ri.ingredientID = i.ingredientID";

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
                        String tags = rs.getString("tags");
                        recipe = new Recipe(recipeID, title, thumbnailUrl, instructions, rating, tags);
                        recipeMap.put(recipeID, recipe);
                        recipes.add(recipe);
                    }

                    int ingredientID = rs.getInt("ingredientID");
                    if (!rs.wasNull()) {
                        String ingredientName = rs.getString("ingredient_name");
                        int quantity = rs.getInt("quantity");
                        String unit = rs.getString("unit");
                        RecipeIngredient ingredient = new RecipeIngredient(recipeID, ingredientID, quantity, unit, ingredientName);
                        recipe.getIngredients().add(ingredient);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recipes with ingredients", e);
        }

        return recipes;
    }

    public static Recipe getRecipeWithIngredients(int recipeID) {
        String sql = "SELECT r.recipeID, r.title AS recipe_name, r.instructions, r.rating, r.thumbnailUrl, r.tags, " +
                "i.ingredientID, i.ingredientName AS ingredient_name, " +
                "ri.quantity, ri.unit " +
                "FROM recipes r " +
                "LEFT JOIN recipe_ingredients ri ON r.recipeID = ri.recipeID " +
                "LEFT JOIN ingredients i ON ri.ingredientID = i.ingredientID " +
                "WHERE r.recipeID = ?";

        Recipe recipe = null;
        String initialTags = null;
        boolean tagsWereGenerated = false;

        try {
            Connection conn = getPersistentConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, recipeID);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        if (recipe == null) {
                            String title = rs.getString("recipe_name");
                            String instructions = rs.getString("instructions");
                            String thumbnailUrl = rs.getString("thumbnailUrl");
                            float rating = rs.getFloat("rating");
                            initialTags = rs.getString("tags");

                            recipe = new Recipe(recipeID, title, thumbnailUrl, instructions, rating, initialTags);
                        }
                        int ingredientID = rs.getInt("ingredientID");
                        if (!rs.wasNull()) {
                            String ingredientName = rs.getString("ingredient_name");
                            int quantity = rs.getInt("quantity");
                            String unit = rs.getString("unit");
                            RecipeIngredient ingredient = new RecipeIngredient(recipeID, ingredientID, quantity, unit, ingredientName);
                            if (recipe != null) {
                                recipe.getIngredients().add(ingredient);
                            }
                        }
                    }
                }
            }

            if (recipe != null && (initialTags == null || initialTags.trim().isEmpty())) {
                logger.info("Tags missing for recipe ID {}. Generating...", recipeID);
                List<String> generatedTags = RecipeAiTagger.generateTags(recipe.getTitle(), recipe.getInstructions());
                if (generatedTags != null && !generatedTags.isEmpty()) {
                    String tagsToSave = generatedTags.stream().collect(Collectors.joining(","));
                    updateRecipeTags(recipeID, tagsToSave);
                    recipe.setTagsString(tagsToSave);
                    tagsWereGenerated = true;
                    logger.info("Generated and saved tags for recipe ID {}: {}", recipeID, tagsToSave);
                } else {
                    logger.warn("AI tag generation returned no tags for recipe ID {}.", recipeID);
                }
            }

        } catch (SQLException e) {
            logger.error("Error retrieving recipe with ingredients for recipeID: " + recipeID, e);
        }

        return recipe;
    }

    private static void updateRecipeTags(int recipeID, String tags) {
        String sql = "UPDATE recipes SET tags = ? WHERE recipeID = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tags);
            pstmt.setInt(2, recipeID);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                logger.warn("Attempted to update tags for recipe ID {}, but no rows were affected.", recipeID);
            }
        } catch (SQLException e) {
            logger.error("Error updating tags for recipe ID: " + recipeID, e);
        }
    }

    public static boolean loadAllRecipes(ArrayList<Recipe> recipes) {
        String insertRecipeSQL = "INSERT INTO recipes (recipeID, title, instructions, thumbnailUrl, rating, tags) VALUES (?, ?, ?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE title=VALUES(title), instructions=VALUES(instructions), thumbnailUrl=VALUES(thumbnailUrl), rating=VALUES(rating), tags=VALUES(tags)";

        try {
            Connection conn = getPersistentConnection();
            try (PreparedStatement preparedStatement = conn.prepareStatement(insertRecipeSQL)) {
                for (Recipe recipe : recipes) {
                    String tagsToSave = recipe.getTagsString();

                    preparedStatement.setInt(1, recipe.getRecipeID());
                    preparedStatement.setString(2, recipe.getTitle());
                    preparedStatement.setString(3, recipe.getInstructions());
                    preparedStatement.setString(4, recipe.getThumbnailUrl());
                    preparedStatement.setFloat(5, recipe.getRating());
                    preparedStatement.setString(6, tagsToSave);

                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        } catch (SQLException e) {
            logger.error("Error loading/replacing all recipes", e);
            return false;
        }
        return true;
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
        String getAllRecipesSQL = "SELECT recipeID, title, thumbnailUrl, instructions, rating, tags FROM recipes;";
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            Connection conn = getPersistentConnection();
            try (Statement statement = conn.createStatement();
                 ResultSet resultSet = statement.executeQuery(getAllRecipesSQL)) {

                while (resultSet.next()) {
                    recipes.add(new Recipe(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving recipes", e);
        }
        return recipes;
    }
}
