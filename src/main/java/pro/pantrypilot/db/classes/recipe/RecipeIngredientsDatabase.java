package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        String getAllRecipeIngredientsSQL = "SELECT ri.recipeID, ri.ingredientID, ri.quantity, u.unitName AS unit " +
                "FROM recipe_ingredients ri " +
                "LEFT JOIN units u ON ri.unitID = u.unitID;";
        ArrayList<RecipeIngredient> recipeIngredients = new ArrayList<>();
        try {
            Statement stmt = DatabaseConnectionManager.getConnection().createStatement();
            ResultSet resultSet = stmt.executeQuery(getAllRecipeIngredientsSQL);
            while (resultSet.next()) {
                int recipeID = resultSet.getInt("recipeID");
                int ingredientID = resultSet.getInt("ingredientID");
                int quantity = resultSet.getInt("quantity");
                String unit = resultSet.getString("unit"); // may be null if unitID is not set
                recipeIngredients.add(new RecipeIngredient(recipeID, ingredientID, quantity, unit));
            }
        } catch (SQLException e) {
            logger.error("Error getting all recipe ingredients", e);
            throw new RuntimeException(e);
        }
        return recipeIngredients;
    }

    public static boolean loadAllRecipeIngredients(List<RecipeIngredient> recipeIngredients) {
        String insertRecipeIngredientSQL = "INSERT INTO recipe_ingredients (recipeID, ingredientID, quantity, unit) VALUES ";
        for (RecipeIngredient recipeIngredient : recipeIngredients) {
            insertRecipeIngredientSQL += "(" + recipeIngredient.getRecipeID() + ", " + recipeIngredient.getIngredientID() + ", " + recipeIngredient.getQuantity() + ", '" + recipeIngredient.getUnit() + "'),";
        }
        insertRecipeIngredientSQL = insertRecipeIngredientSQL.substring(0, insertRecipeIngredientSQL.length() - 1) + ";";
        try {
            Statement stmt = DatabaseConnectionManager.getConnection().createStatement();
            stmt.execute(insertRecipeIngredientSQL);
        } catch (SQLException e) {
            logger.error("Error inserting recipe ingredients", e);
            throw new RuntimeException(e);
        }
        return true;
    }
}