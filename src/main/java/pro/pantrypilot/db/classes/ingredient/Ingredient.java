package pro.pantrypilot.db.classes.ingredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Ingredient {

    private static final Logger logger = LoggerFactory.getLogger(Ingredient.class);

    private final long ingredientID;
    private final String ingredientName;
    private final Integer defaultUnitID;

    public Ingredient(long ingredientID, String ingredientName, Integer defaultUnitID) {
        this.ingredientID = ingredientID;
        this.ingredientName = ingredientName;
        this.defaultUnitID = defaultUnitID;
    }

    public Ingredient(ResultSet rs) {
        try {
            this.ingredientID = rs.getLong("id");
            this.ingredientName = rs.getString("name");
            // Get default_unit_id and handle null case
            Object defaultUnitObj = rs.getObject("default_unit_id");
            this.defaultUnitID = defaultUnitObj != null ? rs.getInt("default_unit_id") : null;
        } catch (SQLException e) {
            logger.error("Error creating ingredient from ResultSet", e);
            throw new RuntimeException(e);
        }
    }

    public long getIngredientID() {
        return ingredientID;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public Integer getDefaultUnitID() {
        return defaultUnitID;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "ingredientID=" + ingredientID +
                ", ingredientName='" + ingredientName + '\'' +
                ", defaultUnitID=" + defaultUnitID +
                '}';
    }
}
