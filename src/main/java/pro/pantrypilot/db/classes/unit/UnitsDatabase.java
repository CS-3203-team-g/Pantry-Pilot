package pro.pantrypilot.db.classes.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UnitsDatabase {
    private static final Logger logger = LoggerFactory.getLogger(UnitsDatabase.class);

    public static void initializeUnitsDatabase() {
        Connection connection = DatabaseConnectionManager.getConnection();
        try {
            Statement stmt = connection.createStatement();
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS units (" +
                "unitID INT AUTO_INCREMENT PRIMARY KEY, " +
                "unitName VARCHAR(50) NOT NULL, " +
                "CONSTRAINT unitName UNIQUE (unitName)" +
                ")"
            );
            stmt.close();
            logger.info("Units table created or already exists");
        } catch (SQLException e) {
            logger.error("Error creating units table", e);
        }
    }

    public static List<Unit> getAllUnits() {
        Connection connection = DatabaseConnectionManager.getConnection();
        List<Unit> units = new ArrayList<>();
        
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM units");
            
            while (rs.next()) {
                units.add(new Unit(rs));
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            logger.error("Error retrieving all units", e);
        }
        
        return units;
    }

    public static boolean loadAllUnits(List<Unit> units) {
        Connection connection = DatabaseConnectionManager.getConnection();
        String sql = "INSERT INTO units (unitID, unitName) VALUES (?, ?)";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            
            for (Unit unit : units) {
                pstmt.setInt(1, unit.getUnitID());
                pstmt.setString(2, unit.getUnitName());
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
            pstmt.close();
            return true;
        } catch (SQLException e) {
            logger.error("Error loading units", e);
            return false;
        }
    }
}
