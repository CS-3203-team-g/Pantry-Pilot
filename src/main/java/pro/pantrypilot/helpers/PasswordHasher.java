package pro.pantrypilot.helpers;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordHasher {
    private static final Logger logger = LoggerFactory.getLogger(PasswordHasher.class);
    private static final int BCRYPT_COST = 12; // Work factor (complexity) for BCrypt - adjust based on your security needs

    public static class Password {
        private final String hashedValue;
        
        /**
         * Creates a Password object with the hashed value
         * BCrypt combines the salt with the hash, so we don't need to store them separately
         */
        public Password(String hashedValue) {
            this.hashedValue = hashedValue;
        }

        public String getHashedValue() {
            return hashedValue;
        }
    }

    /**
     * Generates a BCrypt hashed password with a secure random salt
     * Uses a work factor of 12 by default (can be adjusted as computing power increases)
     * 
     * @param plaintextPassword The password to hash
     * @return Password object containing the BCrypt hashed password (with the salt embedded)
     */
    public static String generatePassword(String plaintextPassword) {
        try {
            // BCrypt automatically generates a secure random salt and incorporates it into the hash
            return BCrypt.hashpw(plaintextPassword, BCrypt.gensalt(BCRYPT_COST));
        } catch (Exception e) {
            logger.error("Error generating password hash with BCrypt", e);
            throw new RuntimeException("Error generating password hash", e);
        }
    }

    /**
     * Verifies a plaintext password against a BCrypt hashed password
     * 
     * @param plaintextPassword The plaintext password to verify
     * @param hashedPassword The BCrypt hashed password (which includes the salt)
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plaintextPassword, String hashedPassword) {
        try {
            // BCrypt.checkpw handles all the salt extraction and hashing internally
            return BCrypt.checkpw(plaintextPassword, hashedPassword);
        } catch (Exception e) {
            logger.error("Error verifying password with BCrypt", e);
            return false;
        }
    }
}
