package pro.pantrypilot.db.classes.shoppingList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListIngredientsDatabase {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingListIngredientsDatabase.class);

    public static void initializeShoppingListIngredientsDatabase() {
        String createShoppingListIngredientsTableSQL = "CREATE TABLE IF NOT EXISTS pantry_pilot.shopping_list_ingredients (\n"
                + "    shoppingListID INT NOT NULL,\n"
                + "    ingredientID BIGINT UNSIGNED NOT NULL,\n"
                + "    quantity DECIMAL(7,2) NOT NULL,\n"
                + "    unitID INT NULL,\n"
                + "    PRIMARY KEY (shoppingListID, ingredientID),\n"
                + "    FOREIGN KEY (shoppingListID) REFERENCES pantry_pilot.shopping_lists(shoppingListID) ON DELETE CASCADE,\n"
                + "    FOREIGN KEY (ingredientID) REFERENCES pantry_pilot.ingredients(id) ON DELETE CASCADE,\n"
                + "    FOREIGN KEY (unitID) REFERENCES pantry_pilot.units(unitID)\n"
                + ");";
        try {
            Statement stmt = DatabaseConnectionManager.getConnection().createStatement();
            stmt.execute(createShoppingListIngredientsTableSQL);
        } catch (SQLException e) {
            logger.error("Error creating shopping_list_ingredients table", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean addIngredientToShoppingList(int shoppingListID, int ingredientID, BigDecimal quantity, String unit) {
        String addIngredientSQL = "INSERT INTO shopping_list_ingredients (shoppingListID, ingredientID, quantity, unit) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = shopping_list_ingredients.quantity + VALUES(quantity)";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(addIngredientSQL)) {
            preparedStatement.setInt(1, shoppingListID);
            preparedStatement.setInt(2, ingredientID);
            preparedStatement.setBigDecimal(3, quantity);
            preparedStatement.setString(4, unit);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error adding ingredient to shopping list", e);
            return false;
        }
    }

    // For backward compatibility
    public static boolean addIngredientToShoppingList(int shoppingListID, int ingredientID, int quantity, String unit) {
        return addIngredientToShoppingList(shoppingListID, ingredientID, new BigDecimal(quantity), unit);
    }

    public static List<ShoppingListIngredient> getIngredientsForShoppingList(int shoppingListID) {
        String getIngredientsSQL = "SELECT * FROM shopping_list_ingredients WHERE shoppingListID = ?";
        List<ShoppingListIngredient> ingredients = new ArrayList<>();
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(getIngredientsSQL)) {
            preparedStatement.setInt(1, shoppingListID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    int ingredientID = resultSet.getInt("ingredientID");
                    BigDecimal quantity = resultSet.getBigDecimal("quantity");
                    String unit = resultSet.getString("unit");
                    ShoppingListIngredient ingredient = new ShoppingListIngredient(shoppingListID, ingredientID, quantity, unit);
                    ingredients.add(ingredient);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving ingredients for shoppingListID: " + shoppingListID, e);
        }
        return ingredients;
    }

    public static boolean removeIngredientFromShoppingList(int shoppingListID, int ingredientID, BigDecimal quantity, String unit) {
        String removeIngredientSQL = "DELETE FROM shopping_list_ingredients WHERE shoppingListID = ? AND ingredientID = ? AND quantity = ? AND unit = ? LIMIT 1";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(removeIngredientSQL)) {
            preparedStatement.setInt(1, shoppingListID);
            preparedStatement.setInt(2, ingredientID);
            preparedStatement.setBigDecimal(3, quantity);
            preparedStatement.setString(4, unit);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error removing ingredient from shopping list", e);
            return false;
        }
    }

    // For backward compatibility
    public static boolean removeIngredientFromShoppingList(int shoppingListID, int ingredientID, int quantity, String unit) {
        return removeIngredientFromShoppingList(shoppingListID, ingredientID, new BigDecimal(quantity), unit);
    }

    /**
     * Adds all ingredients from the given recipe into the shopping list.
     *
     * @param recipeId      the recipe ID to copy ingredients from
     * @param shoppingListId the shopping list ID to insert ingredients into
     */
    public static boolean addIngredientsToShoppingList(int recipeId, int shoppingListId) {
        String sql = "INSERT INTO pantry_pilot.shopping_list_ingredients (shoppingListID, ingredientID, quantity, unit) " +
                "SELECT ?, ingredientID, SUM(quantity), unit " +
                "FROM recipe_ingredients " +
                "WHERE recipeID = ? " +
                "GROUP BY ingredientID, unit " +
                "ON DUPLICATE KEY UPDATE quantity = shopping_list_ingredients.quantity + VALUES(quantity)";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the shoppingListID for the insert and recipeID for the SELECT clause
            pstmt.setInt(1, shoppingListId);
            pstmt.setInt(2, recipeId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            // Handle any SQL errors here
            e.printStackTrace();
        }
        return false;
    }
}