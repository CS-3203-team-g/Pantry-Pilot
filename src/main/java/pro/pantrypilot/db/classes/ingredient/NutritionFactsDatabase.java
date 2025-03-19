package pro.pantrypilot.db.classes.ingredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        String sql = "INSERT INTO nutrition_facts (ingredientID, unitID, calories, fat, carbohydrates, protein) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "calories = VALUES(calories), " +
                    "fat = VALUES(fat), " +
                    "carbohydrates = VALUES(carbohydrates), " +
                    "protein = VALUES(protein)";
        
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

    public static NutritionFacts getNutritionFactsForIngredient(long ingredientID, Integer unitID) {
        if (unitID == null) return null;

        Connection connection = DatabaseConnectionManager.getConnection();
        String sql = "SELECT nf.* " +
                    "FROM nutrition_facts nf " +
                    "JOIN ingredients i ON nf.ingredientID = i.id " +
                    "WHERE nf.ingredientID = ? AND (nf.unitID = ? OR nf.unitID = i.default_unit_id) " +
                    "ORDER BY CASE WHEN nf.unitID = ? THEN 0 ELSE 1 END " +
                    "LIMIT 1";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, ingredientID);
            pstmt.setInt(2, unitID);
            pstmt.setInt(3, unitID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new NutritionFacts(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting nutrition facts for ingredient {} and unit {}", ingredientID, unitID, e);
        }

        return null;
    }

    /**
     * Calculates nutritional information with proper unit conversion.
     * 
     * @param ingredientID The ID of the ingredient
     * @param quantity The quantity of the ingredient
     * @param unitID The unit ID of the ingredient
     * @return A NutritionFacts object with nutritional values adjusted by conversion factor, or null if data is not available
     */
    public static NutritionFacts calculateNutritionFacts(long ingredientID, float quantity, Integer unitID) {
        if (unitID == null) return null;
        
        // Get reference nutrition facts (usually stored per gram)
        NutritionFacts baseFacts = getNutritionFactsForIngredient(ingredientID, unitID);
        if (baseFacts == null) {
            logger.debug("No nutrition facts found for ingredient {} with unit {}", ingredientID, unitID);
            return null;
        }
        
        // Get conversion factor from ingredient_units table
        float conversionFactor = getConversionFactor(ingredientID, unitID);
        
        // Calculate adjusted nutritional values
        Float calories = baseFacts.getCalories() != null ? baseFacts.getCalories() * quantity * conversionFactor : null;
        Float fat = baseFacts.getFat() != null ? baseFacts.getFat() * quantity * conversionFactor : null;
        Float carbohydrates = baseFacts.getCarbohydrates() != null ? baseFacts.getCarbohydrates() * quantity * conversionFactor : null;
        Float protein = baseFacts.getProtein() != null ? baseFacts.getProtein() * quantity * conversionFactor : null;
        
        return new NutritionFacts(ingredientID, unitID, calories, fat, carbohydrates, protein);
    }
    
    /**
     * Get the conversion factor for an ingredient-unit combination
     * 
     * @param ingredientID The ingredient ID
     * @param unitID The unit ID
     * @return The conversion factor, or 1.0 if not found
     */
    private static float getConversionFactor(long ingredientID, int unitID) {
        Connection connection = DatabaseConnectionManager.getConnection();
        String sql = "SELECT conversionFactor FROM ingredient_units WHERE ingredientID = ? AND unitID = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, ingredientID);
            pstmt.setInt(2, unitID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("conversionFactor");
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting conversion factor for ingredient {} and unit {}", ingredientID, unitID, e);
        }
        
        // Default to 1.0 if no conversion factor found
        return 1.0f;
    }
    
    /**
     * Calculate nutrition facts for a list of ingredients with their quantities and units.
     * 
     * @param ingredientIDs List of ingredient IDs
     * @param quantities List of quantities (same order as ingredientIDs)
     * @param unitIDs List of unit IDs (same order as ingredientIDs)
     * @return Map of nutrition facts by ingredient ID
     */
    public static Map<String, NutritionFacts> calculateNutritionFactsForIngredients(
            List<Long> ingredientIDs, List<Float> quantities, List<Integer> unitIDs) {
            
        if (ingredientIDs == null || ingredientIDs.isEmpty() || 
            quantities == null || quantities.isEmpty() || 
            unitIDs == null || unitIDs.isEmpty() ||
            ingredientIDs.size() != quantities.size() || ingredientIDs.size() != unitIDs.size()) {
            return new HashMap<>();
        }
        
        Map<String, NutritionFacts> nutritionMap = new HashMap<>();
        
        for (int i = 0; i < ingredientIDs.size(); i++) {
            Long ingredientID = ingredientIDs.get(i);
            Float quantity = quantities.get(i);
            Integer unitID = unitIDs.get(i);
            
            if (ingredientID == null || quantity == null || unitID == null) continue;
            
            NutritionFacts facts = calculateNutritionFacts(ingredientID, quantity, unitID);
            if (facts != null) {
                // Use composite key of ingredientID-unitID
                nutritionMap.put(ingredientID + "-" + unitID, facts);
            }
        }
        
        return nutritionMap;
    }

    public static Map<String, NutritionFacts> getNutritionFactsForIngredients(List<Long> ingredientIDs, List<Integer> unitIDs) {
        if (ingredientIDs == null || ingredientIDs.isEmpty() || unitIDs == null || unitIDs.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, NutritionFacts> nutritionMap = new HashMap<>();
        Connection connection = DatabaseConnectionManager.getConnection();
        
        // Build the IN clause for the query
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < ingredientIDs.size(); i++) {
            if (i > 0) placeholders.append(" OR ");
            placeholders.append("(ingredientID = ? AND unitID = ?)");
        }

        String sql = "SELECT * FROM nutrition_facts WHERE " + placeholders.toString();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Set parameters for the IN clause
            int paramIndex = 1;
            for (int i = 0; i < ingredientIDs.size(); i++) {
                pstmt.setLong(paramIndex++, ingredientIDs.get(i));
                pstmt.setInt(paramIndex++, unitIDs.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    NutritionFacts facts = new NutritionFacts(rs);
                    // Use composite key of ingredientID-unitID
                    nutritionMap.put(facts.getIngredientID() + "-" + facts.getUnitID(), facts);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting nutrition facts for ingredients: {}", e.getMessage());
        }

        return nutritionMap;
    }

    /**
     * Calculate nutrition facts for a recipe using SQL aggregation
     * 
     * @param recipeID The ID of the recipe
     * @return A NutritionFacts object containing the total nutritional information for the recipe
     */
    public static NutritionFacts calculateRecipeNutrition(int recipeID) {
        Connection connection = DatabaseConnectionManager.getConnection();
        String sql = "SELECT recipeID, title, " +
                "SUM(total_calories) AS total_calories, " +
                "SUM(total_fat) AS total_fat, " +
                "SUM(total_carbohydrates) AS total_carbohydrates, " +
                "SUM(total_protein) AS total_protein " +
                "FROM ( " +
                "    SELECT r.recipeID, r.title, " +
                "    ((ri.quantity * " +
                "      COALESCE( " +
                "         (SELECT iu1.conversionFactor FROM ingredient_units iu1 " +
                "           WHERE iu1.ingredientID = ri.ingredientID AND iu1.unitID = ri.unitID), " +
                "         (SELECT iu2.conversionFactor FROM ingredient_units iu2 " +
                "           WHERE iu2.ingredientID = ri.ingredientID AND iu2.unitID = i.default_unit_id), " +
                "         1 " +
                "      )) / 100) * " +
                "      COALESCE( " +
                "         (SELECT nf1.calories FROM nutrition_facts nf1 " +
                "           WHERE nf1.ingredientID = ri.ingredientID AND nf1.unitID = ri.unitID), " +
                "         (SELECT nf2.calories FROM nutrition_facts nf2 " +
                "           WHERE nf2.ingredientID = ri.ingredientID AND nf2.unitID = i.default_unit_id), " +
                "         0 " +
                "      ) AS total_calories, " +
                "    ((ri.quantity * " +
                "      COALESCE( " +
                "         (SELECT iu1.conversionFactor FROM ingredient_units iu1 " +
                "           WHERE iu1.ingredientID = ri.ingredientID AND iu1.unitID = ri.unitID), " +
                "         (SELECT iu2.conversionFactor FROM ingredient_units iu2 " +
                "           WHERE iu2.ingredientID = ri.ingredientID AND iu2.unitID = i.default_unit_id), " +
                "         1 " +
                "      )) / 100) * " +
                "      COALESCE( " +
                "         (SELECT nf1.fat FROM nutrition_facts nf1 " +
                "           WHERE nf1.ingredientID = ri.ingredientID AND nf1.unitID = ri.unitID), " +
                "         (SELECT nf2.fat FROM nutrition_facts nf2 " +
                "           WHERE nf2.ingredientID = ri.ingredientID AND nf2.unitID = i.default_unit_id), " +
                "         0 " +
                "      ) AS total_fat, " +
                "    ((ri.quantity * " +
                "      COALESCE( " +
                "         (SELECT iu1.conversionFactor FROM ingredient_units iu1 " +
                "           WHERE iu1.ingredientID = ri.ingredientID AND iu1.unitID = ri.unitID), " +
                "         (SELECT iu2.conversionFactor FROM ingredient_units iu2 " +
                "           WHERE iu2.ingredientID = ri.ingredientID AND iu2.unitID = i.default_unit_id), " +
                "         1 " +
                "      )) / 100) * " +
                "      COALESCE( " +
                "         (SELECT nf1.carbohydrates FROM nutrition_facts nf1 " +
                "           WHERE nf1.ingredientID = ri.ingredientID AND nf1.unitID = ri.unitID), " +
                "         (SELECT nf2.carbohydrates FROM nutrition_facts nf2 " +
                "           WHERE nf2.ingredientID = ri.ingredientID AND nf2.unitID = i.default_unit_id), " +
                "         0 " +
                "      ) AS total_carbohydrates, " +
                "    ((ri.quantity * " +
                "      COALESCE( " +
                "         (SELECT iu1.conversionFactor FROM ingredient_units iu1 " +
                "           WHERE iu1.ingredientID = ri.ingredientID AND iu1.unitID = ri.unitID), " +
                "         (SELECT iu2.conversionFactor FROM ingredient_units iu2 " +
                "           WHERE iu2.ingredientID = ri.ingredientID AND iu2.unitID = i.default_unit_id), " +
                "         1 " +
                "      )) / 100) * " +
                "      COALESCE( " +
                "         (SELECT nf1.protein FROM nutrition_facts nf1 " +
                "           WHERE nf1.ingredientID = ri.ingredientID AND nf1.unitID = ri.unitID), " +
                "         (SELECT nf2.protein FROM nutrition_facts nf2 " +
                "           WHERE nf2.ingredientID = ri.ingredientID AND nf2.unitID = i.default_unit_id), " +
                "         0 " +
                "      ) AS total_protein " +
                "    FROM recipes r " +
                "    JOIN recipe_ingredients ri ON r.recipeID = ri.recipeID " +
                "    JOIN ingredients i ON ri.ingredientID = i.id " +
                "    WHERE r.recipeID = ? " +
                ") sub " +
                "GROUP BY recipeID, title";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, recipeID);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Using -1 for ingredientID and unitID since this represents recipe totals
                    return new NutritionFacts(
                        -1L, 
                        -1,
                        rs.getFloat("total_calories"),
                        rs.getFloat("total_fat"),
                        rs.getFloat("total_carbohydrates"),
                        rs.getFloat("total_protein")
                    );
                }
            }
        } catch (SQLException e) {
            logger.error("Error calculating nutrition facts for recipe {}: {}", recipeID, e.getMessage());
        }
        
        // Return zeros if calculation fails
        return new NutritionFacts(-1L, -1, 0f, 0f, 0f, 0f);
    }

    // Method for calculating multiple recipes at once for better performance
    public static Map<Integer, NutritionFacts> calculateRecipesNutrition(List<Integer> recipeIDs) {
        if (recipeIDs == null || recipeIDs.isEmpty()) {
            return new HashMap<>();
        }

        Map<Integer, NutritionFacts> nutritionMap = new HashMap<>();
        Connection connection = DatabaseConnectionManager.getConnection();
        
        // Build the IN clause
        String placeholders = String.join(",", Collections.nCopies(recipeIDs.size(), "?"));
        String sql = "SELECT " +
                "r.recipeID, " +
                "r.title, " +
                "COALESCE(SUM(((ri.quantity * " +
                "COALESCE((SELECT iu1.conversionFactor FROM ingredient_units iu1 " +
                "WHERE iu1.ingredientID = ri.ingredientID AND iu1.unitID = ri.unitID), " +
                "(SELECT iu2.conversionFactor FROM ingredient_units iu2 " +
                "WHERE iu2.ingredientID = ri.ingredientID AND iu2.unitID = i.default_unit_id), " +
                "1)) / 100) * " +
                "COALESCE((SELECT nf1.calories FROM nutrition_facts nf1 " +
                "WHERE nf1.ingredientID = ri.ingredientID AND nf1.unitID = ri.unitID), " +
                "(SELECT nf2.calories FROM nutrition_facts nf2 " +
                "WHERE nf2.ingredientID = ri.ingredientID AND nf2.unitID = i.default_unit_id), " +
                "0)), 0) AS total_calories, " +
                "COALESCE(SUM(((ri.quantity * " +
                "COALESCE((SELECT iu1.conversionFactor FROM ingredient_units iu1 " +
                "WHERE iu1.ingredientID = ri.ingredientID AND iu1.unitID = ri.unitID), " +
                "(SELECT iu2.conversionFactor FROM ingredient_units iu2 " +
                "WHERE iu2.ingredientID = ri.ingredientID AND iu2.unitID = i.default_unit_id), " +
                "1)) / 100) * " +
                "COALESCE((SELECT nf1.fat FROM nutrition_facts nf1 " +
                "WHERE nf1.ingredientID = ri.ingredientID AND nf1.unitID = ri.unitID), " +
                "(SELECT nf2.fat FROM nutrition_facts nf2 " +
                "WHERE nf2.ingredientID = ri.ingredientID AND nf2.unitID = i.default_unit_id), " +
                "0)), 0) AS total_fat, " +
                "COALESCE(SUM(((ri.quantity * " +
                "COALESCE((SELECT iu1.conversionFactor FROM ingredient_units iu1 " +
                "WHERE iu1.ingredientID = ri.ingredientID AND iu1.unitID = ri.unitID), " +
                "(SELECT iu2.conversionFactor FROM ingredient_units iu2 " +
                "WHERE iu2.ingredientID = ri.ingredientID AND iu2.unitID = i.default_unit_id), " +
                "1)) / 100) * " +
                "COALESCE((SELECT nf1.carbohydrates FROM nutrition_facts nf1 " +
                "WHERE nf1.ingredientID = ri.ingredientID AND nf1.unitID = ri.unitID), " +
                "(SELECT nf2.carbohydrates FROM nutrition_facts nf2 " +
                "WHERE nf2.ingredientID = ri.ingredientID AND nf2.unitID = i.default_unit_id), " +
                "0)), 0) AS total_carbohydrates, " +
                "COALESCE(SUM(((ri.quantity * " +
                "COALESCE((SELECT iu1.conversionFactor FROM ingredient_units iu1 " +
                "WHERE iu1.ingredientID = ri.ingredientID AND iu1.unitID = ri.unitID), " +
                "(SELECT iu2.conversionFactor FROM ingredient_units iu2 " +
                "WHERE iu2.ingredientID = ri.ingredientID AND iu2.unitID = i.default_unit_id), " +
                "1)) / 100) * " +
                "COALESCE((SELECT nf1.protein FROM nutrition_facts nf1 " +
                "WHERE nf1.ingredientID = ri.ingredientID AND nf1.unitID = ri.unitID), " +
                "(SELECT nf2.protein FROM nutrition_facts nf2 " +
                "WHERE nf2.ingredientID = ri.ingredientID AND nf2.unitID = i.default_unit_id), " +
                "0)), 0) AS total_protein " +
                "FROM recipes AS r " +
                "LEFT JOIN recipe_ingredients AS ri ON r.recipeID = ri.recipeID " +
                "LEFT JOIN ingredients AS i ON ri.ingredientID = i.id " +
                "WHERE r.recipeID IN (" + placeholders + ") " +
                "GROUP BY r.recipeID, r.title";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Set all recipe IDs as parameters
            for (int i = 0; i < recipeIDs.size(); i++) {
                pstmt.setInt(i + 1, recipeIDs.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int recipeID = rs.getInt("recipeID");
                    nutritionMap.put(recipeID, new NutritionFacts(
                        -1L,
                        -1,
                        rs.getFloat("total_calories"),
                        rs.getFloat("total_fat"),
                        rs.getFloat("total_carbohydrates"),
                        rs.getFloat("total_protein")
                    ));
                }
            }
        } catch (SQLException e) {
            logger.error("Error calculating nutrition facts for recipes: {}", e.getMessage());
        }

        return nutritionMap;
    }
}