package pro.pantrypilot.db.classes.userHealthInfo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class UserHealthInfo {
    private String userHealthInfoID;
    private String userID;
    private String gender;
    private String dietaryPreference;
    private double weight;
    private double height;
    private double goalWeight;
    private int dailyCalorieGoal;
    private int age;
    private Timestamp updatedAt;

    public UserHealthInfo(String userID, double weight, double goalWeight, double height, int age, String gender, int dailyCalorieGoal, String dietaryPreferences) {
        this.userHealthInfoID = UUID.randomUUID().toString(); // Auto-generate unique ID
        this.userID = userID;
        this.weight = weight;
        this.goalWeight = goalWeight;
        this.height = height;
        this.age = age;
        this.gender = gender;
        this.dailyCalorieGoal = dailyCalorieGoal;
        this.dietaryPreference = dietaryPreferences;
        this.updatedAt = new Timestamp(System.currentTimeMillis()); // Set update timestam
    }

    // Constructor for loading existing user health info from database
    public UserHealthInfo(String healthInfoID, String userID, double weight, double goalWeight, double height, int age, String gender, int dailyCalorieGoal, String dietaryPreferences, Timestamp updatedAt) {
        this.userHealthInfoID = healthInfoID;
        this.userID = userID;
        this.weight = weight;
        this.goalWeight = goalWeight;
        this.height = height;
        this.age = age;
        this.gender = gender;
        this.dailyCalorieGoal = dailyCalorieGoal;
        this.dietaryPreference = dietaryPreferences;
        this.updatedAt = updatedAt;
    }

    // Constructor for creating from ResultSet (database query)
    public UserHealthInfo(ResultSet resultSet) {
        try {
            this.userHealthInfoID = resultSet.getString("healthInfoID");
            this.userID = resultSet.getString("userID");
            this.weight = resultSet.getDouble("weight");
            this.goalWeight = resultSet.getDouble("goalWeight");
            this.height = resultSet.getDouble("height");
            this.age = resultSet.getInt("age");
            this.gender = resultSet.getString("gender");
            this.dailyCalorieGoal = resultSet.getInt("dailyCalorieGoal");
            this.dietaryPreference = resultSet.getString("dietaryPreferences");
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

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
        updateTimestamp();
    }

    public double getGoalWeight(double goalWeight) {
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

    public int getDailyCalorieGoal() {
        return dailyCalorieGoal;
    }

    public void setDailyCalorieGoal(int dailyCalorieGoal) {
        this.dailyCalorieGoal = dailyCalorieGoal;
        updateTimestamp();
    }

    public String getDietaryPreferences() {
        return dietaryPreference;
    }

    public void setDietaryPreferences(String dietaryPreferences) {
        this.dietaryPreference = dietaryPreferences;
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
                ", weight=" + weight +
                ", height=" + height +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", dailyCalorieGoal=" + dailyCalorieGoal +
                ", dietaryPreferences='" + dietaryPreference + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }
}