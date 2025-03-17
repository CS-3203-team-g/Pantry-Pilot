package pro.pantrypilot.db.classes.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.SQLException;
import java.sql.Statement;

public class UnitsDatabase {

    private static final Logger logger = LoggerFactory.getLogger(UnitsDatabase.class);

    public static void initializeUnitsDatabase() {
        String createIngredientsTableSQL = "CREATE TABLE IF NOT EXISTS units (\n"
                + "    unitID INT AUTO_INCREMENT PRIMARY KEY,\n"
                + "    unitName VARCHAR(50) NOT NULL UNIQUE\n"
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
