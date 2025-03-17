package pro.pantrypilot.db.classes.ingredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NutritionFactsDatabase {
    private static final Logger logger = LoggerFactory.getLogger(NutritionFactsDatabase.class);

    public static void initializeNutritionFactsDatabase() {
        Connection connection = DatabaseConnectionManager.getConnection();
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS nutrition_facts (" +
                "ingredientID BIGINT UNSIGNED NOT NULL, " +
                "unitID INT NOT NULL, " +
                "calories FLOAT NULL, " +
                "fat FLOAT NULL, " +
                "carbohydrates FLOAT NULL, " +
                "protein FLOAT NULL, " +
                "PRIMARY KEY (ingredientID, unitID), " +
                "CONSTRAINT nutrition_facts_ibfk_1 FOREIGN KEY (ingredientID) REFERENCES ingredients (id) ON DELETE CASCADE, " +
                "CONSTRAINT nutrition_facts_ibfk_2 FOREIGN KEY (unitID) REFERENCES units (unitID) ON DELETE CASCADE" +
                ")"
            );
            stmt.close();
            logger.info("Nutrition_facts table created or already exists");
        } catch (SQLException e) {
            logger.error("Error creating nutrition_facts table", e);
        }
    }

    public static List<NutritionFacts> getAllNutritionFacts() {
        Connection connection = DatabaseConnectionManager.getConnection();
        List<NutritionFacts> nutritionFacts = new ArrayList<>();
        
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM nutrition_facts");
            
            while (rs.next()) {
                nutritionFacts.add(new NutritionFacts(rs));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            logger.error("Error retrieving all nutrition facts", e);
        }
        
        return nutritionFacts;
    }

    public static boolean loadAllNutritionFacts(List<NutritionFacts> nutritionFacts) {
        Connection connection = DatabaseConnectionManager.getConnection();
        String sql = "INSERT INTO nutrition_facts (ingredientID, unitID, calories, fat, carbohydrates, protein) VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            
            for (NutritionFacts fact : nutritionFacts) {
                pstmt.setLong(1, fact.getIngredientID());
                pstmt.setInt(2, fact.getUnitID());
                
                if (fact.getCalories() != null) {
                    pstmt.setFloat(3, fact.getCalories());
                } else {
                    pstmt.setNull(3, Types.FLOAT);
                }
                
                if (fact.getFat() != null) {
                    pstmt.setFloat(4, fact.getFat());
                } else {
                    pstmt.setNull(4, Types.FLOAT);
                }
                
                if (fact.getCarbohydrates() != null) {
                    pstmt.setFloat(5, fact.getCarbohydrates());
                } else {
                    pstmt.setNull(5, Types.FLOAT);
                }
                
                if (fact.getProtein() != null) {
                    pstmt.setFloat(6, fact.getProtein());
                } else {
                    pstmt.setNull(6, Types.FLOAT);
                }
                
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            pstmt.close();
            return true;
        } catch (SQLException e) {
            logger.error("Error loading nutrition facts", e);
            return false;
        }
    }
}