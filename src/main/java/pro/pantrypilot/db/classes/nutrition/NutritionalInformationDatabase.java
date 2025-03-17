package pro.pantrypilot.db.classes.nutrition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.SQLException;
import java.sql.Statement;

public class NutritionalInformationDatabase {

    private static Logger logger = LoggerFactory.getLogger(NutritionalInformationDatabase.class);

    public static void initializeNutritionalInformationDatabase() {
        String createNutritionFactsTableSQL = "CREATE TABLE IF NOT EXISTS pantry_pilot.nutrition_facts (\n"
                + "    ingredientID BIGINT UNSIGNED NOT NULL,\n"
                + "    unitID INT NOT NULL,\n"
                + "    calories FLOAT,\n"
                + "    fat FLOAT,\n"
                + "    carbohydrates FLOAT,\n"
                + "    protein FLOAT,\n"
                + "    PRIMARY KEY (ingredientID, unitID),\n"
                + "    FOREIGN KEY (ingredientID) REFERENCES pantry_pilot.ingredients(id) ON DELETE CASCADE,\n"
                + "    FOREIGN KEY (unitID) REFERENCES pantry_pilot.units(unitID) ON DELETE CASCADE\n"
                + ");";
        try {
            Statement stmt = DatabaseConnectionManager.getConnection().createStatement();
            stmt.execute(createNutritionFactsTableSQL);
        } catch (SQLException e) {
            logger.error("Error creating nutrition facts table", e);
            throw new RuntimeException(e);
        }
    }

}
