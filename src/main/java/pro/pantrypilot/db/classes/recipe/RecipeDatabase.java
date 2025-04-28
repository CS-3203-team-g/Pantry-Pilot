package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.ai.RecipeAiTagger;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Import Gson for serializing ingredients list to JSON
import com.google.gson.Gson;

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
        // Updated CREATE TABLE statement to include all new fields
        String createRecipeTableSQL = "CREATE TABLE IF NOT EXISTS recipes (\n"
                + "    recipeID INT AUTO_INCREMENT PRIMARY KEY,\n"
                + "    title VARCHAR(255) NOT NULL,\n"
                + "    instructions TEXT NOT NULL,\n"
                + "    thumbnailUrl VARCHAR(255),\n"
                + "    rating FLOAT CHECK (rating >= 0 AND rating <= 5),\n"
                + "    tags TEXT NULL,\n" // Existing tags
                + "    prep_time_minutes INT NULL,\n" // New field
                + "    cook_time_minutes INT NULL,\n" // New field
                + "    servings INT NULL,\n" // New field
                + "    ingredients_json TEXT NULL\n" // New field for AI ingredients
                + ");";
        try (Connection conn = getPersistentConnection(); // Or DatabaseConnectionManager.getConnection()
             Statement stmt = conn.createStatement()) {
            logger.info("Ensuring 'recipes' table exists with all columns...");
            stmt.execute(createRecipeTableSQL);
            logger.info("'recipes' table check/creation complete.");
        } catch (SQLException e) {
            logger.error("Error ensuring 'recipes' table exists", e);
            throw new RuntimeException(e); // Rethrow critical error during init
        }

        // Step 2: Check and add individual columns if they are missing (for existing tables)
        // This handles cases where the table existed but lacked some columns.
        List<String> columnsToCheck = Arrays.asList(
                "tags", "prep_time_minutes", "cook_time_minutes", "servings", "ingredients_json"
        );
        String[] columnTypes = {
                "TEXT NULL", "INT NULL", "INT NULL", "INT NULL", "TEXT NULL"
        };

        try (Connection conn = DatabaseConnectionManager.getConnection()) { // Use standard connection for checks
            String dbName = System.getenv("DB_NAME");
            if (dbName == null || dbName.trim().isEmpty()) {
                logger.warn("DB_NAME environment variable not set, cannot reliably check for column existence. Skipping column checks.");
                return;
            }

            for (int i = 0; i < columnsToCheck.size(); i++) {
                String columnName = columnsToCheck.get(i);
                String columnDefinition = columnTypes[i];
                checkAndAddColumn(conn, dbName, "recipes", columnName, columnDefinition);
            }

        } catch (SQLException e) {
            logger.error("SQL error during column check/addition in 'recipes' table.", e);
        } catch (Exception e) {
            logger.error("Unexpected error during column check/addition in 'recipes' table.", e);
        }
    }

    // Helper method to check and add a column if it doesn't exist
    private static void checkAndAddColumn(Connection conn, String dbName, String tableName, String columnName, String columnDefinition) throws SQLException {
        String checkColumnSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?";
        boolean columnExists = false;

        logger.debug("Checking for '{}' column in table '{}' within schema '{}'...", columnName, tableName, dbName);
        try (PreparedStatement pstmtCheck = conn.prepareStatement(checkColumnSql)) {
            pstmtCheck.setString(1, dbName);
            pstmtCheck.setString(2, tableName);
            pstmtCheck.setString(3, columnName);
            try (ResultSet rs = pstmtCheck.executeQuery()) {
                if (rs.next()) {
                    columnExists = rs.getInt(1) > 0;
                }
            }
        }

        if (!columnExists) {
            logger.info("'{}' column not found in '{}' table. Attempting to add it...", columnName, tableName);
            String addColumnSql = String.format("ALTER TABLE %s ADD COLUMN %s %s", tableName, columnName, columnDefinition);
            try (Statement stmtAlter = conn.createStatement()) {
                stmtAlter.execute(addColumnSql);
                logger.info("Successfully added '{}' column to '{}' table.", columnName, tableName);
            } catch (SQLException alterEx) {
                // MariaDB error 1060 is 'Duplicate column name', which we can ignore here.
                if (alterEx.getErrorCode() == 1060) {
                    logger.warn("Attempted to add '{}' column, but it already exists (Error Code {}).", columnName, alterEx.getErrorCode());
                } else {
                    logger.error("Error adding '{}' column to '{}' table.", columnName, tableName, alterEx);
                    throw alterEx; // Re-throw unexpected errors
                }
            }
        } else {
            logger.debug("'{}' column already exists in '{}' table.", columnName, tableName);
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
        String sql = "SELECT r.recipeID, r.title AS recipe_name, r.instructions, r.rating, r.thumbnailUrl, r.tags, r.ingredients_json, " +
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
                            recipe = new Recipe(rs);
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

    // New method to insert a recipe generated by AI
    public static int insertGeneratedRecipe(Recipe recipe) {
        String sql = "INSERT INTO recipes (title, instructions, thumbnailUrl, rating, tags, " +
                     "prep_time_minutes, cook_time_minutes, servings, ingredients_json) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int generatedRecipeId = -1; // Default to -1 or throw exception on failure

        if (recipe == null) {
            logger.error("Attempted to insert a null recipe.");
            return generatedRecipeId;
        }

        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, recipe.getTitle());
            pstmt.setString(2, recipe.getInstructions());
            pstmt.setString(3, recipe.getThumbnailUrl()); // Can be null
            pstmt.setFloat(4, recipe.getRating()); // Assuming AI provides a rating, or default (e.g., 0)
            pstmt.setString(5, recipe.getTagsString()); // Comma-separated string

            // Handle nullable Integer fields for time and servings
            if (recipe.getPrepTimeMinutes() != null) {
                pstmt.setInt(6, recipe.getPrepTimeMinutes());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            if (recipe.getCookTimeMinutes() != null) {
                pstmt.setInt(7, recipe.getCookTimeMinutes());
            } else {
                pstmt.setNull(7, Types.INTEGER);
            }
            if (recipe.getServings() != null) {
                pstmt.setInt(8, recipe.getServings());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }

            pstmt.setString(9, recipe.getIngredientsJson()); // The JSON string of ingredients

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedRecipeId = generatedKeys.getInt(1);
                        logger.info("Successfully inserted generated recipe '{}' with ID: {}", recipe.getTitle(), generatedRecipeId);
                    } else {
                        logger.error("Failed to retrieve generated ID after inserting recipe: {}", recipe.getTitle());
                    }
                } catch (SQLException keyEx) {
                    logger.error("Error retrieving generated key for recipe: {}", recipe.getTitle(), keyEx);
                }
            } else {
                logger.error("Failed to insert generated recipe, no rows affected: {}", recipe.getTitle());
            }

        } catch (SQLException e) {
            logger.error("Error inserting generated recipe: {}", recipe.getTitle(), e);
            // Depending on requirements, might want to throw a custom exception here
        }

        return generatedRecipeId;
    }
}
