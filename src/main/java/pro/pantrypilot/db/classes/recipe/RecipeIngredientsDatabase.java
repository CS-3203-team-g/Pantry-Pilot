package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class RecipeIngredientsDatabase {

    private static final Logger logger = LoggerFactory.getLogger(RecipeIngredientsDatabase.class);

    public static void initializeRecipeIngredientsDatabase() {
        String createRecipeIngredientsTableSQL = "CREATE TABLE IF NOT EXISTS pantry_pilot.recipe_ingredients (\n"
                + "    recipeID INT NOT NULL,\n"
                + "    ingredientID BIGINT UNSIGNED NOT NULL,\n"
                + "    quantity INT NOT NULL,\n"
                + "    unitID INT NULL,\n"
                + "    PRIMARY KEY (recipeID, ingredientID),\n"
                + "    FOREIGN KEY (recipeID) REFERENCES pantry_pilot.recipes(recipeID) ON DELETE CASCADE,\n"
                + "    FOREIGN KEY (ingredientID) REFERENCES pantry_pilot.ingredients(id) ON DELETE CASCADE,\n"
                + "    FOREIGN KEY (unitID) REFERENCES pantry_pilot.units(unitID)\n"
                + ");";
        try {
            Statement stmt = DatabaseConnectionManager.getConnection().createStatement();
            stmt.execute(createRecipeIngredientsTableSQL);
        } catch (SQLException e) {
            logger.error("Error creating recipe_ingredients table", e);
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<RecipeIngredient> getAllRecipeIngredients() {
        String getAllRecipeIngredientsSQL = "SELECT ri.recipeID, ri.ingredientID, ri.quantity, ri.unitID, u.unitName AS unit_name " +
                "FROM recipe_ingredients ri " +
                "LEFT JOIN units u ON ri.unitID = u.unitID;";
        ArrayList<RecipeIngredient> recipeIngredients = new ArrayList<>();
        try {
            Statement stmt = DatabaseConnectionManager.getConnection().createStatement();
            ResultSet resultSet = stmt.executeQuery(getAllRecipeIngredientsSQL);
            while (resultSet.next()) {
                int recipeID = resultSet.getInt("recipeID");
                long ingredientID = resultSet.getLong("ingredientID");
                int quantity = resultSet.getInt("quantity");
                
                // Handle null unitID
                Integer unitID = null;
                if (resultSet.getObject("unitID") != null) {
                    unitID = resultSet.getInt("unitID");
                }
                
                // Get unit name if available
                String unitName = resultSet.getString("unit_name");
                
                recipeIngredients.add(new RecipeIngredient(recipeID, ingredientID, quantity, unitID, null, unitName));
            }
        } catch (SQLException e) {
            logger.error("Error getting all recipe ingredients", e);
            throw new RuntimeException(e);
        }
        return recipeIngredients;
    }

    public static boolean loadAllRecipeIngredients(List<RecipeIngredient> recipeIngredients) {
        String insertRecipeIngredientSQL = "INSERT INTO recipe_ingredients (recipeID, ingredientID, quantity, unitID) VALUES (?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnectionManager.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(insertRecipeIngredientSQL)) {
                for (RecipeIngredient recipeIngredient : recipeIngredients) {
                    pstmt.setInt(1, recipeIngredient.getRecipeID());
                    pstmt.setLong(2, recipeIngredient.getIngredientID());
                    pstmt.setInt(3, recipeIngredient.getQuantity());
                    
                    // Handle null unitID
                    if (recipeIngredient.getUnitID() != null) {
                        pstmt.setInt(4, recipeIngredient.getUnitID());
                    } else {
                        pstmt.setNull(4, Types.INTEGER);
                    }
                    
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
        } catch (SQLException e) {
            logger.error("Error inserting recipe ingredients", e);
            return false;
        }
        return true;
    }
}