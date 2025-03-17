package pro.pantrypilot.db.classes.ingredient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.SQLException;
import java.sql.Statement;

public class IngredientUnitsDatabase {

    private static final Logger logger = LoggerFactory.getLogger(IngredientUnitsDatabase.class);

    public static void initializeIngredientUnitsDatabase() {
        String createIngredientsTableSQL = "CREATE TABLE IF NOT EXISTS pantry_pilot.ingredient_units (\n"
                + "    ingredientID BIGINT UNSIGNED NOT NULL,\n"
                + "    unitID INT NOT NULL,\n"
                + "    conversionFactor FLOAT NOT NULL,\n"
                + "    PRIMARY KEY (ingredientID, unitID),\n"
                + "    FOREIGN KEY (ingredientID) REFERENCES pantry_pilot.ingredients(id) ON DELETE CASCADE,\n"
                + "    FOREIGN KEY (unitID) REFERENCES pantry_pilot.units(unitID) ON DELETE CASCADE\n"
                + ");";
        try {
            Statement stmt = DatabaseConnectionManager.getConnection().createStatement();
            stmt.execute(createIngredientsTableSQL);
        } catch (SQLException e) {
            logger.error("Error creating ingredients table", e);
            throw new RuntimeException(e);
        }
    }

}
