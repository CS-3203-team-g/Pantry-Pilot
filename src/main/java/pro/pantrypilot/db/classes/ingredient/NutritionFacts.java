package pro.pantrypilot.db.classes.ingredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class NutritionFacts {
    private static final Logger logger = LoggerFactory.getLogger(NutritionFacts.class);
    
    private final long ingredientID;
    private final int unitID;
    private final Float calories;
    private final Float fat;
    private final Float carbohydrates;
    private final Float protein;
    
    public NutritionFacts(long ingredientID, int unitID, Float calories, Float fat, Float carbohydrates, Float protein) {
        this.ingredientID = ingredientID;
        this.unitID = unitID;
        this.calories = calories;
        this.fat = fat;
        this.carbohydrates = carbohydrates;
        this.protein = protein;
    }
    
    public NutritionFacts(ResultSet rs) throws SQLException {
        try {
            this.ingredientID = rs.getLong("ingredientID");
            this.unitID = rs.getInt("unitID");
            this.calories = rs.getObject("calories") != null ? rs.getFloat("calories") : null;
            this.fat = rs.getObject("fat") != null ? rs.getFloat("fat") : null;
            this.carbohydrates = rs.getObject("carbohydrates") != null ? rs.getFloat("carbohydrates") : null;
            this.protein = rs.getObject("protein") != null ? rs.getFloat("protein") : null;
        } catch (SQLException e) {
            logger.error("Error creating nutrition facts from ResultSet", e);
            throw e;
        }
    }
    
    public long getIngredientID() {
        return ingredientID;
    }
    
    public int getUnitID() {
        return unitID;
    }
    
    public Float getCalories() {
        return calories;
    }
    
    public Float getFat() {
        return fat;
    }
    
    public Float getCarbohydrates() {
        return carbohydrates;
    }
    
    public Float getProtein() {
        return protein;
    }
    
    @Override
    public String toString() {
        return "NutritionFacts{" +
                "ingredientID=" + ingredientID +
                ", unitID=" + unitID +
                ", calories=" + calories +
                ", fat=" + fat +
                ", carbohydrates=" + carbohydrates +
                ", protein=" + protein +
                '}';
    }
}