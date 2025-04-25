package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientsDatabase {

    private static final Logger logger = LoggerFactory.getLogger(IngredientsDatabase.class);

    public static void initializeIngredientsDatabase() {
        String createIngredientsTableSQL = "CREATE TABLE IF NOT EXISTS ingredients (\n"
                + "    ingredientID INT AUTO_INCREMENT PRIMARY KEY,\n"
                + "    ingredientName VARCHAR(255) NOT NULL UNIQUE\n"
                + ");";
        try {
            Statement stmt = DatabaseConnectionManager.getConnection().createStatement();
            stmt.execute(createIngredientsTableSQL);
        } catch (SQLException e) {
            logger.error("Error creating ingredients table", e);
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<String> getAllIngredientNames() {
        String getAllIngredientsSQL = "SELECT ingredientName FROM ingredients;";
        ArrayList<String> ingredientNames = new ArrayList<>();
        try (Statement statement = DatabaseConnectionManager.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(getAllIngredientsSQL)) {

            while (resultSet.next()) {
                ingredientNames.add(resultSet.getString("ingredientName"));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving ingredient names", e);
        }
        return ingredientNames;
    }

    public static ArrayList<Ingredient> getAllIngredients() {
        String getAllIngredientsSQL = "SELECT * FROM ingredients;";
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        try (Statement statement = DatabaseConnectionManager.getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(getAllIngredientsSQL)) {

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient(resultSet);
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            logger.error("Error retrieving ingredients", e);
        }
        return ingredients;
    }


    public static boolean loadAllIngredients(List<Ingredient> ingredients) {
        String insertRecipeSQL = "INSERT INTO ingredients (ingredientID, ingredientName) VALUES (?, ?);";

        try {
            Connection conn = DatabaseConnectionManager.getConnection();
            try (PreparedStatement preparedStatement = conn.prepareStatement(insertRecipeSQL)) {
                for (Ingredient ingredient : ingredients) {
                    preparedStatement.setInt(1, ingredient.getIngredientID());
                    preparedStatement.setString(2, ingredient.getIngredientName());
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