package pro.pantrypilot.db.classes.ingredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientUnitsDatabase {
    private static final Logger logger = LoggerFactory.getLogger(IngredientUnitsDatabase.class);

    public static void initializeIngredientUnitsDatabase() {
        Connection connection = DatabaseConnectionManager.getConnection();
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS ingredient_units (" +
                "ingredientID BIGINT UNSIGNED NOT NULL, " +
                "unitID INT NOT NULL, " +
                "conversionFactor FLOAT NOT NULL, " +
                "PRIMARY KEY (ingredientID, unitID), " +
                "CONSTRAINT ingredient_units_ibfk_1 FOREIGN KEY (ingredientID) REFERENCES ingredients (id) ON DELETE CASCADE, " +
                "CONSTRAINT ingredient_units_ibfk_2 FOREIGN KEY (unitID) REFERENCES units (unitID) ON DELETE CASCADE" +
                ")"
            );
            stmt.close();
            logger.info("Ingredient_units table created or already exists");
        } catch (SQLException e) {
            logger.error("Error creating ingredient_units table", e);
        }
    }

    public static List<IngredientUnit> getAllIngredientUnits() {
        Connection connection = DatabaseConnectionManager.getConnection();
        List<IngredientUnit> ingredientUnits = new ArrayList<>();
        
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM ingredient_units");
            
            while (rs.next()) {
                ingredientUnits.add(new IngredientUnit(rs));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            logger.error("Error retrieving all ingredient units", e);
        }
        
        return ingredientUnits;
    }

    public static boolean loadAllIngredientUnits(List<IngredientUnit> ingredientUnits) {
        Connection connection = DatabaseConnectionManager.getConnection();
        String sql = "INSERT INTO ingredient_units (ingredientID, unitID, conversionFactor) VALUES (?, ?, ?)";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            
            for (IngredientUnit ingredientUnit : ingredientUnits) {
                pstmt.setLong(1, ingredientUnit.getIngredientID());
                pstmt.setInt(2, ingredientUnit.getUnitID());
                pstmt.setFloat(3, ingredientUnit.getConversionFactor());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            pstmt.close();
            return true;
        } catch (SQLException e) {
            logger.error("Error loading ingredient units", e);
            return false;
        }
    }
}
