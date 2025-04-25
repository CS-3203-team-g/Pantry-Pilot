package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RecipeIngredient {

    private static final Logger logger = LoggerFactory.getLogger(RecipeIngredient.class);

    private final int recipeID;
    private final int ingredientID;
    private final int quantity;
    private final String unit;
    private String ingredientName;


    public RecipeIngredient(int recipeID, int ingredientID, int quantity, String unit) {
        this.recipeID = recipeID;
        this.ingredientID = ingredientID;
        this.quantity = quantity;
        this.unit = unit;
    }

    public RecipeIngredient(int recipeID, int ingredientID, int quantity, String unit, String ingredientName) {
        this.recipeID = recipeID;
        this.ingredientID = ingredientID;
        this.quantity = quantity;
        this.unit = unit;
        this.ingredientName = ingredientName;
    }

    public RecipeIngredient(ResultSet rs) {
        try {
            this.recipeID = rs.getInt("recipeID");
            this.ingredientID = rs.getInt("ingredientID");
            this.quantity = rs.getInt("quantity");
            this.unit = rs.getString("unit");
            this.ingredientName = rs.getString("ingredient_name");
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public int getRecipeID() {
        return recipeID;
    }

    public int getIngredientID() {
        return ingredientID;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    @Override
    public String toString() {
        return "RecipeIngredient{" +
                "recipeID=" + recipeID +
                ", ingredientID=" + ingredientID +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                '}';
    }
}
