package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Ingredient {

    private static final Logger logger = LoggerFactory.getLogger(Ingredient.class);

    private final int ingredientID;
    private final String ingredientName;

    public Ingredient(int ingredientID, String ingredientName) {
        this.ingredientID = ingredientID;
        this.ingredientName = ingredientName;
    }

    public Ingredient(ResultSet rs) {
        try {
            this.ingredientID = rs.getInt("ingredientID");
            this.ingredientName = rs.getString("ingredientName");
        } catch (SQLException e) {
            logger.error("Error creating ingredient from ResultSet", e);
            throw new RuntimeException(e);
        }
    }

    public int getIngredientID() {
        return ingredientID;
    }

    public String getIngredientName() {
        return ingredientName;
    }

}
