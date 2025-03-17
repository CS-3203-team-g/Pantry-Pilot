package pro.pantrypilot.db.classes.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Unit {
    private static final Logger logger = LoggerFactory.getLogger(Unit.class);
    
    private final int unitID;
    private final String unitName;
    
    public Unit(int unitID, String unitName) {
        this.unitID = unitID;
        this.unitName = unitName;
    }
    
    public Unit(ResultSet rs) {
        try {
            this.unitID = rs.getInt("unitID");
            this.unitName = rs.getString("unitName");
        } catch (SQLException e) {
            logger.error("Error creating unit from ResultSet", e);
            throw new RuntimeException(e);
        }
    }
    
    public int getUnitID() {
        return unitID;
    }
    
    public String getUnitName() {
        return unitName;
    }
    
    @Override
    public String toString() {
        return "Unit{" +
                "unitID=" + unitID +
                ", unitName='" + unitName + '\'' +
                '}';
    }
}