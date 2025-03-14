package pro.pantrypilot.db.classes.userHealthInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.SQLException;

public class UserHealthInfoDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserHealthInfoDatabase.class)

    public static void initializeUserHealthInfoDatabase() {
        String createUserHealthInfoTableSQL =
                "CREATE TABLE user_health_info (\n" +
                "    healthInfoID VARCHAR(36) PRIMARY KEY,\n" +
                "    userID VARCHAR(36) NOT NULL,\n" +
                "    weight DOUBLE NOT NULL,\n" +
                "    height DOUBLE NOT NULL,\n" +
                "    age INT NOT NULL,\n" +
                "    gender VARCHAR(20) NOT NULL,\n" +
                "    dailyCalorieGoal INT NOT NULL,\n" +
                "    dietaryPreferences VARCHAR(255),\n" +
                "    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                "    FOREIGN KEY (userID) REFERENCES user(userID) ON DELETE CASCADE\n" +
                ");\n";
        try{
            DatabaseConnectionManager.getConnection().createStatement().executeUpdate(createUserHealthInfoTableSQL);
        } catch(SQLException e) {
            LOGGER.error("Error creating user health info table", e);
            throw new RuntimeException(e);
        }
    }
    public static boolean createUserHealthInfo(UserHealthInfo userHealthInfo) {
        String createUserHealthInfoSQL = "INSERT INTO user_health_info (userID, weight, height"
        return false;
    }
}

