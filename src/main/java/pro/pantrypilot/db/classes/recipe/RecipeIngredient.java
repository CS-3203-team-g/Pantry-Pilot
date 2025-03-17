package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RecipeIngredient {

    private static final Logger logger = LoggerFactory.getLogger(RecipeIngredient.class);

    private final int recipeID;
    private final long ingredientID;
    private final int quantity;
    private final Integer unitID; // Changed from String unit to Integer unitID
    private String ingredientName;
    private String unitName; // Added to store the unit name for display purposes

    public RecipeIngredient(int recipeID, long ingredientID, int quantity, Integer unitID) {
        this.recipeID = recipeID;
        this.ingredientID = ingredientID;
        this.quantity = quantity;
        this.unitID = unitID;
    }

    public RecipeIngredient(int recipeID, long ingredientID, int quantity, Integer unitID, String ingredientName) {
        this.recipeID = recipeID;
        this.ingredientID = ingredientID;
        this.quantity = quantity;
        this.unitID = unitID;
        this.ingredientName = ingredientName;
    }

    public RecipeIngredient(int recipeID, long ingredientID, int quantity, Integer unitID, String ingredientName, String unitName) {
        this.recipeID = recipeID;
        this.ingredientID = ingredientID;
        this.quantity = quantity;
        this.unitID = unitID;
        this.ingredientName = ingredientName;
        this.unitName = unitName;
    }

    public RecipeIngredient(ResultSet rs) {
        try {
            this.recipeID = rs.getInt("recipeID");
            this.ingredientID = rs.getLong("ingredientID");
            this.quantity = rs.getInt("quantity");
            // Handle null case for unitID
            Object unitIDObj = rs.getObject("unitID");
            this.unitID = unitIDObj != null ? rs.getInt("unitID") : null;

            // Try to get ingredient name if it's in the result set
            try {
                this.ingredientName = rs.getString("ingredient_name");
            } catch (SQLException e) {
                // Ignore if the column doesn't exist
            }

            // Try to get unit name if it's in the result set
            try {
                this.unitName = rs.getString("unit_name");
            } catch (SQLException e) {
                // Ignore if the column doesn't exist
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public int getRecipeID() {
        return recipeID;
    }

    public long getIngredientID() {
        return ingredientID;
    }

    public int getQuantity() {
        return quantity;
    }

    public Integer getUnitID() {
        return unitID;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @Override
    public String toString() {
        return "RecipeIngredient{" +
                "recipeID=" + recipeID +
                ", ingredientID=" + ingredientID +
                ", quantity=" + quantity +
                ", unitID=" + unitID +
                ", ingredientName='" + ingredientName + '\'' +
                ", unitName='" + unitName + '\'' +
                '}';
    }
}
