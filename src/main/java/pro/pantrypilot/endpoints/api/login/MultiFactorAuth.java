package pro.pantrypilot.endpoints.api.login;

import java.security.MessageDigest;
import java.util.Random;

public class MultiFactorAuth {
    public static void main(String[] args) throws Exception {
        // User's password input
        String plainText = "user_password";
        // Password hashing and validation
        MessageDigest encoder = MessageDigest.getInstance("SHA");
        encoder.update(plainText.getBytes());
        byte[] digest = encoder.digest();
        // Simulated stored hash for the correct password
        byte[] storedHash = getStoredHash();
        if (equal(digest, storedHash)) {
            System.out.println("Password validated. Proceeding with MFA...");
            // Generate and send OTP
            String generatedOTP = sendOTP();
            // Simulated OTP input from user
            String userOTPInput = "123456"; // Replace with real input in production
            if (userOTPInput.equals(generatedOTP)) {
                login_user();
            } else {
                System.out.println("MFA Failed. Access denied.");
            }
        } else {
            System.out.println("Password validation failed. Access denied.");
        }
    }
    private static byte[] getStoredHash() throws Exception {
        // Simulated stored hash for "user_password"
        MessageDigest encoder = MessageDigest.getInstance("SHA");
        encoder.update("user_password".getBytes());
        return encoder.digest();
    }
    private static boolean equal(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }
    private static String sendOTP() {
        // Generate a 6-digit OTP
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        System.out.println("Generated OTP (sent to user): " + otp); // Simulate OTP sent
        return String.valueOf(otp); //Temporarily disabled return statement for testing
        /*For sake of Test successful login
        int otpForTest = 123456;
         return String.valueOf(otpForTest);//*/
    }
    private static void login_user() {
        System.out.println("Login successful.");
    }
}
