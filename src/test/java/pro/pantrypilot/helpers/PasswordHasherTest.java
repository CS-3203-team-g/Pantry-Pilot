package pro.pantrypilot.helpers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    @Test
    public void testPasswordGeneration() {
        String password = "testpassword123";

        String hashedPassword = PasswordHasher.generatePassword(password);

        assertNotNull(hashedPassword);
        assertFalse(hashedPassword.isEmpty());
        // BCrypt hashes should start with $2a$ or $2b$
        assertTrue(hashedPassword.startsWith("$2a$")
                || hashedPassword.startsWith("$2b$"));
    }

    @Test
    public void testPasswordVerification() {
        String correctPassword = "testpassword123";
        String wrongPassword = "wrongpassword123";

        String storedHash = PasswordHasher.generatePassword(correctPassword);

        assertTrue(PasswordHasher.verifyPassword(correctPassword, storedHash));
        assertFalse(PasswordHasher.verifyPassword(wrongPassword, storedHash));
    }

    @Test
    public void testSamePasswordDifferentHashes() {
        String password = "testpassword123";

        String hashedPassword1 = PasswordHasher.generatePassword(password);
        String hashedPassword2 = PasswordHasher.generatePassword(password);

        // With BCrypt, even the same password should produce different hashes due to different salts
        assertNotEquals(hashedPassword1, hashedPassword2);
    }
}