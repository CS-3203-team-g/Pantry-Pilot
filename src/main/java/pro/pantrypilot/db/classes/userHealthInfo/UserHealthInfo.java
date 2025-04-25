package pro.pantrypilot.db.classes.userHealthInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class UserHealthInfo {
    private final String userHealthInfoID;
    private final String userID;
    private String gender;
    private String dietaryPreference;
    private String activityLevel;
    private double currWeight;
    private double height;
    private double goalWeight;
    private int age;
    private Timestamp updatedAt;

    public UserHealthInfo(String userID, double currWeight, double goalWeight, double height, int age, String gender, String dietaryPreferences, String activityLevel) {
        this.userHealthInfoID = UUID.randomUUID().toString(); // Auto-generate unique ID
        this.userID = userID;
        this.currWeight = currWeight;
        this.goalWeight = goalWeight;
        this.height = height;
        this.age = age;
        this.gender = gender;
        this.dietaryPreference = dietaryPreferences;
        this.activityLevel = activityLevel;
        this.updatedAt = new Timestamp(System.currentTimeMillis()); // Set update timestam
    }

    // Constructor for loading existing user health info from database
    public UserHealthInfo(String healthInfoID, String userID, double currWeight, double goalWeight, double height, int age, String dietaryPreferences, String activityLevel, Timestamp updatedAt) {
        this.userHealthInfoID = healthInfoID;
        this.userID = userID;
        this.currWeight = currWeight;
        this.goalWeight = goalWeight;
        this.height = height;
        this.age = age;
        this.dietaryPreference = dietaryPreferences;
        this.activityLevel = activityLevel;
        this.updatedAt = updatedAt;
    }

    // Constructor for creating from ResultSet (database query)
    public UserHealthInfo(ResultSet resultSet) {
        try {
            this.userHealthInfoID = resultSet.getString("healthInfoID");
            this.userID = resultSet.getString("userID");
            this.currWeight = resultSet.getDouble("currWeight");
            this.goalWeight = resultSet.getDouble("goalWeight");
            this.height = resultSet.getDouble("height");
            this.age = resultSet.getInt("age");
            this.dietaryPreference = resultSet.getString("dietaryPreferences");
            this.activityLevel = resultSet.getString("activityLevel");
            this.updatedAt = resultSet.getTimestamp("updatedAt");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error constructing UserHealthInfo from ResultSet", e);
        }
    }

    // Getters and Setters
    public String getHealthInfoID() {
        return userHealthInfoID;
    }

    public String getUserID() {
        return userID;
    }

    public double getCurrWeight() {
        return currWeight;
    }

    public void setWeight(double currWeight) {
        this.currWeight = currWeight;
        updateTimestamp();
    }

    public double getGoalWeight() {
        return goalWeight;
    }

    public void setGoalWeight(double goalWeight) {
        this.goalWeight = goalWeight;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
        updateTimestamp();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
        updateTimestamp();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
        updateTimestamp();
    }

    public String getDietaryPreferences() {
        return dietaryPreference;
    }

    public void setDietaryPreferences(String dietaryPreferences) {
        this.dietaryPreference = dietaryPreferences;
        updateTimestamp();
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
        updateTimestamp();
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    private void updateTimestamp() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
    @Override
    public String toString() {
        return "UserHealthInfo{" +
                "healthInfoID='" + userHealthInfoID + '\'' +
                ", userID='" + userID + '\'' +
                ", currWeight=" + currWeight +
                ", goalWeight=" + goalWeight +
                ", height=" + height +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", dietaryPreferences='" + dietaryPreference + '\'' +
                ", activityLevel='" + activityLevel + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}