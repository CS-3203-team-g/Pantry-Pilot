package pro.pantrypilot.userHealthInfo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import pro.pantrypilot.db.classes.userHealthInfo.UserHealthInfo;

class UserHealthInfoTest {

    @Test
    void testConstructorNewUser() {
        String userID = "user123";
        double weight = 75.0;
        double goalWeight = 70.0;
        double height = 180.0;
        int age = 30;
        String gender = "Male";
        String dietaryPreference = "Vegetarian";
        String activityLevel = "Active";

        UserHealthInfo info = new UserHealthInfo(userID, weight, goalWeight, height, age, gender, dietaryPreference, activityLevel);

        // Verify that the generated healthInfoID is not null and appears to be a UUID.
        assertNotNull(info.getHealthInfoID());
        assertDoesNotThrow(() -> UUID.fromString(info.getHealthInfoID()));

        assertEquals(userID, info.getUserID());
        assertEquals(weight, info.getCurrWeight());
        // Note: the getGoalWeight method takes a parameter and returns it (per the provided code)
        assertEquals(goalWeight, info.getGoalWeight());
        assertEquals(height, info.getHeight());
        assertEquals(age, info.getAge());
        assertEquals(gender, info.getGender());
        assertEquals(dietaryPreference, info.getDietaryPreferences());
        assertEquals(activityLevel, info.getActivityLevel());
        assertNotNull(info.getUpdatedAt());
    }

    @Test
    void testConstructorExisting() {
        String healthInfoID = UUID.randomUUID().toString();
        String userID = "user123";
        double weight = 75.0;
        double goalWeight = 70.0;
        double height = 180.0;
        int age = 30;
        String gender = "Male";
        String dietaryPreference = "Vegan";
        String activityLevel = "Sedentary";
        Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

        UserHealthInfo info = new UserHealthInfo(healthInfoID, userID, weight, goalWeight, height, age, gender, dietaryPreference, activityLevel, updatedAt);

        assertEquals(healthInfoID, info.getHealthInfoID());
        assertEquals(userID, info.getUserID());
        assertEquals(weight, info.getCurrWeight());
        // Since getGoalWeight simply returns the passed parameter, we call it with goalWeight.
        assertEquals(goalWeight, info.getGoalWeight());
        assertEquals(height, info.getHeight());
        assertEquals(age, info.getAge());
        assertEquals(gender, info.getGender());
        assertEquals(dietaryPreference, info.getDietaryPreferences());
        assertEquals(activityLevel, info.getActivityLevel());
        assertEquals(updatedAt, info.getUpdatedAt());
    }

    @Test
    void testConstructorFromResultSet() throws SQLException {
        // Create a mocked ResultSet using Mockito
        ResultSet rs = mock(ResultSet.class);
        String healthInfoID = "healthInfoId1";
        String userID = "user123";
        double weight = 80.0;
        double goalWeight = 75.0;
        double height = 175.0;
        int age = 25;
        String gender = "Female";
        String dietaryPreference = "Omnivore";
        String activityLevel = "Moderate";
        Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

        when(rs.getString("healthInfoID")).thenReturn(healthInfoID);
        when(rs.getString("userID")).thenReturn(userID);
        when(rs.getDouble("weight")).thenReturn(weight);
        when(rs.getDouble("goalWeight")).thenReturn(goalWeight);
        when(rs.getDouble("height")).thenReturn(height);
        when(rs.getInt("age")).thenReturn(age);
        when(rs.getString("gender")).thenReturn(gender);
        when(rs.getString("dietaryPreferences")).thenReturn(dietaryPreference);
        when(rs.getString("activityLevel")).thenReturn(activityLevel);
        when(rs.getTimestamp("updatedAt")).thenReturn(updatedAt);

        UserHealthInfo info = new UserHealthInfo(rs);

        assertEquals(healthInfoID, info.getHealthInfoID());
        assertEquals(userID, info.getUserID());
        assertEquals(weight, info.getCurrWeight());
        // Call getGoalWeight with goalWeight (as implemented in the provided code)
        assertEquals(goalWeight, info.getGoalWeight());
        assertEquals(height, info.getHeight());
        assertEquals(age, info.getAge());
        assertEquals(gender, info.getGender());
        assertEquals(dietaryPreference, info.getDietaryPreferences());
        assertEquals(activityLevel, info.getActivityLevel());
        assertEquals(updatedAt, info.getUpdatedAt());
    }

    @Test
    void testSettersAndUpdateTimestamp() throws InterruptedException {
        UserHealthInfo info = new UserHealthInfo("user123", 75.0, 70.0, 180.0, 30, "Male", "Vegetarian", "Active");
        Timestamp initialTimestamp = info.getUpdatedAt();

        // Pause briefly to ensure a measurable time difference
        Thread.sleep(10);
        info.setWeight(80.0);
        Timestamp weightTimestamp = info.getUpdatedAt();
        assertTrue(weightTimestamp.getTime() >= initialTimestamp.getTime());

        Thread.sleep(10);
        info.setHeight(185.0);
        Timestamp heightTimestamp = info.getUpdatedAt();
        assertTrue(heightTimestamp.getTime() > weightTimestamp.getTime());

        Thread.sleep(10);
        info.setAge(35);
        Timestamp ageTimestamp = info.getUpdatedAt();
        assertTrue(ageTimestamp.getTime() > heightTimestamp.getTime());

        Thread.sleep(10);
        info.setGender("Female");
        Timestamp genderTimestamp = info.getUpdatedAt();
        assertTrue(genderTimestamp.getTime() > ageTimestamp.getTime());

        Thread.sleep(10);
        info.setDietaryPreferences("Vegan");
        Timestamp dietaryTimestamp = info.getUpdatedAt();
        assertTrue(dietaryTimestamp.getTime() > genderTimestamp.getTime());

        Thread.sleep(10);
        info.setActivityLevel("Sedentary");
        Timestamp activityTimestamp = info.getUpdatedAt();
        assertTrue(activityTimestamp.getTime() > dietaryTimestamp.getTime());
    }

    @Test
    void testToString() {
        String userID = "user123";
        double currWeight = 75.0;
        double goalWeight = 70.0;
        double height = 180.0;
        int age = 30;
        String gender = "Male";
        String dietaryPreference = "Vegetarian";
        String activityLevel = "Active";

        UserHealthInfo info = new UserHealthInfo(userID, currWeight, goalWeight, height, age, gender, dietaryPreference, activityLevel);
        String result = info.toString();

        assertTrue(result.contains("UserHealthInfo{"));
        assertTrue(result.contains("userID='" + userID + "'"));
        assertTrue(result.contains("currWeight=" + currWeight));
        assertTrue(result.contains("height=" + height));
        assertTrue(result.contains("age=" + age));
        assertTrue(result.contains("gender='" + gender + "'"));
        assertTrue(result.contains("dietaryPreferences='" + dietaryPreference + "'"));
        assertTrue(result.contains("activityLevel='" + activityLevel + "'"));
        assertNotNull(result);
    }
}

