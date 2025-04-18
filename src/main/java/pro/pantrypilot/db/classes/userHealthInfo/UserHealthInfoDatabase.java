package pro.pantrypilot.db.classes.userHealthInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.sql.SQLException;
import java.util.UUID;

public class UserHealthInfoDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserHealthInfoDatabase.class);

    public static void initializeUserHealthInfoDatabase() {
        String createUserHealthInfoTableSQL =
                "CREATE TABLE IF NOT EXISTS user_health_info (\n" +
                "    healthInfoID VARCHAR(36) PRIMARY KEY,\n" +
                "    userID CHAR(36) NOT NULL,\n" +
                "    weight DOUBLE NOT NULL,\n" +
                "    height DOUBLE NOT NULL,\n" +
                "    age INT NOT NULL,\n" +
                "    activityLevel VARCHAR(30) NOT NULL,\n" +
                "    gender VARCHAR(20) NOT NULL,\n" +
                "    dailyCalorieGoal INT NOT NULL,\n" +
                "    dietaryPreferences VARCHAR(255),\n" +
                "    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                "    FOREIGN KEY (userID) REFERENCES users(userID) ON DELETE CASCADE\n" +
                ");\n";
        try{
            DatabaseConnectionManager.getConnection().createStatement().executeUpdate(createUserHealthInfoTableSQL);
        } catch(SQLException e) {
            LOGGER.error("Error creating user health info table", e);
            throw new RuntimeException(e);
        }
    }
    public static boolean createUserHealthInfo(UserHealthInfo userHealthInfo) {

        String createUserHealthInfoSQL = "INSERT INTO user_health_info (userID, weight, height, age, activityLevel, gender, dietaryPreferences, updatedAt) VALUES ('"
                + userHealthInfo.getHealthInfoID() + "', '"
                + userHealthInfo.getUserID() + "', '"
                + userHealthInfo.getWeight() + "', '"
                + userHealthInfo.getHeight() + "', '"
                + userHealthInfo.getAge() + "', '"
                + userHealthInfo.getActivityLevel() + "', '"
                + userHealthInfo.getGender() + "', '"
                + userHealthInfo.getDietaryPreferences() + "', '"
                + userHealthInfo.getUpdatedAt() + "');";
        try {
            int rowsAffected = DatabaseConnectionManager.getConnection()
                    .createStatement()
                    .executeUpdate(createUserHealthInfoSQL);
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.error("Error creating user health info", e);
            return false;
        }
    }
}

