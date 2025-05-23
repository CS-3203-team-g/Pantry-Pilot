package pro.pantrypilot.db.classes.userHealthInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;
import pro.pantrypilot.db.classes.session.Session;
import pro.pantrypilot.db.classes.session.SessionsDatabase;
import pro.pantrypilot.db.classes.user.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UserHealthInfoDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserHealthInfoDatabase.class);

    public static void initializeUserHealthInfoDatabase() {
        String createUserHealthInfoTableSQL =
                "CREATE TABLE IF NOT EXISTS user_health_info (\n" +
                        "    healthInfoID VARCHAR(36) NOT NULL UNIQUE,\n" +
                        "    userID CHAR(36) PRIMARY KEY ,\n" +
                        "    gender VARCHAR(36) NOT NULL,\n" +
                        "    currWeight DOUBLE NOT NULL,\n" +
                        "    goalWeight DOUBLE NOT NULL,\n" +
                        "    height DOUBLE NOT NULL,\n" +
                        "    age INT NOT NULL,\n" +
                        "    activityLevel VARCHAR(30) NOT NULL,\n" +
                        "    dietaryPreferences VARCHAR(255),\n" +
                        "    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
                        "    FOREIGN KEY (userID) REFERENCES users(userID) ON DELETE CASCADE\n" +
                        ");\n";
        try {
            DatabaseConnectionManager.getConnection().createStatement().executeUpdate(createUserHealthInfoTableSQL);
        } catch (SQLException e) {
            LOGGER.error("Error creating user health info table", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean createUserHealthInfo(UserHealthInfo userHealthInfo) {

        String createUserHealthInfoSQL = "INSERT INTO user_health_info (healthInfoID, userID, gender, currWeight, goalWeight, height, age, activityLevel, dietaryPreferences, updatedAt) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(createUserHealthInfoSQL)) {
            preparedStatement.setString(1, userHealthInfo.getHealthInfoID());
            preparedStatement.setString(2, userHealthInfo.getUserID());
            preparedStatement.setString(3, userHealthInfo.getGender());
            preparedStatement.setDouble(4, userHealthInfo.getCurrWeight());
            preparedStatement.setDouble(5, userHealthInfo.getGoalWeight());
            preparedStatement.setDouble(6, userHealthInfo.getHeight());
            preparedStatement.setInt(7, userHealthInfo.getAge());
            preparedStatement.setString(8, userHealthInfo.getActivityLevel());
            preparedStatement.setString(9, userHealthInfo.getDietaryPreferences());
            preparedStatement.setTimestamp(10, userHealthInfo.getUpdatedAt());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.error("Error creating user health info", e);
            return false;
        }
    }
    public static UserHealthInfo getUserHealthInfo(String userID) {

        String getUserHealthInfoSQL = "SELECT * FROM user_health_info WHERE userID = ?";
        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(getUserHealthInfoSQL)) {
            preparedStatement.setString(1, userID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {  // Move cursor to the first row
                    return new UserHealthInfo(resultSet);
                } else {
                    return null; // No user found with the given username
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving user", e);
            return null;
        }
    }

    public static boolean editUserHealthInfo(UserHealthInfo userHealthInfo) {
        String updateSQL = "UPDATE user_health_info SET " +
                "gender = ?, " +
                "currWeight = ?, " +
                "goalWeight = ?, " +
                "height = ?, " +
                "age = ?, " +
                "activityLevel = ?, " +
                "dietaryPreferences = ?, " +
                "updatedAt = CURRENT_TIMESTAMP " +
                "WHERE userID = ?";

        try (PreparedStatement preparedStatement = DatabaseConnectionManager.getConnection().prepareStatement(updateSQL)) {
            preparedStatement.setString(1, userHealthInfo.getGender());
            preparedStatement.setDouble(2, userHealthInfo.getCurrWeight());
            preparedStatement.setDouble(3, userHealthInfo.getGoalWeight());
            preparedStatement.setDouble(4, userHealthInfo.getHeight());
            preparedStatement.setInt(5, userHealthInfo.getAge());
            preparedStatement.setString(6, userHealthInfo.getActivityLevel());
            preparedStatement.setString(7, userHealthInfo.getDietaryPreferences());
            preparedStatement.setString(8, userHealthInfo.getUserID());

            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.error("Error updating user health info", e);
            return false;
        }
    }

}

