package pro.pantrypilot.helpers;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileHelperTest {

    @Test
    void readFile() throws IOException {
        assertNotNull(FileHelper.readFile("static/index.html"), "index.html should not be null");
        assertNotNull(FileHelper.readFile("static/lists/list.html"), "list.html should not be null");
        assertNotNull(FileHelper.readFile("static/lists/lists.html"), "lists.html should not be null");
        assertNotNull(FileHelper.readFile("static/recipes/recipes.html"), "recipes.html should not be null");
        assertNotNull(FileHelper.readFile("static/login/login.html"), "login.html should not be null");
        assertNotNull(FileHelper.readFile("static/signup/signup.html"), "signup.html should not be null");
        assertNotNull(FileHelper.readFile("static/settings/settings.html"), "settings.html should not be null");
    }
}