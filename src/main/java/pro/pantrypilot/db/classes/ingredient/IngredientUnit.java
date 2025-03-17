package pro.pantrypilot.db.classes.ingredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IngredientUnit {
    private static final Logger logger = LoggerFactory.getLogger(IngredientUnit.class);
    
    private final long ingredientID;
    private final int unitID;
    private final float conversionFactor;
    
    public IngredientUnit(long ingredientID, int unitID, float conversionFactor) {
        this.ingredientID = ingredientID;
        this.unitID = unitID;
        this.conversionFactor = conversionFactor;
    }
    
    public IngredientUnit(ResultSet rs) {
        try {
            this.ingredientID = rs.getLong("ingredientID");
            this.unitID = rs.getInt("unitID");
            this.conversionFactor = rs.getFloat("conversionFactor");
        } catch (SQLException e) {
            logger.error("Error creating ingredient unit from ResultSet", e);
            throw new RuntimeException(e);
        }
    }
    
    public long getIngredientID() {
        return ingredientID;
    }
    
    public int getUnitID() {
        return unitID;
    }
    
    public float getConversionFactor() {
        return conversionFactor;
    }
    
    @Override
    public String toString() {
        return "IngredientUnit{" +
                "ingredientID=" + ingredientID +
                ", unitID=" + unitID +
                ", conversionFactor=" + conversionFactor +
                '}';
    }
}