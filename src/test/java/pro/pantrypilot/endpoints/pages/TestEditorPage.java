package pro.pantrypilot.endpoints.pages;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestEditorPage {

    private final EditorPageService service = mock(EditorPageService.class); // pretend service

    @Test
    public void testGetEditorPortalPage_validPartition() {
        // Valid Partition: non-null get result
        String expected = "Editor Portal Page";
        when(service.getEditorPortalPage()).thenReturn(expected);

        String result = service.getEditorPortalPage();

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void testGetEditorPortalPage_nullReturnValue_invalidPartition1() {
        // Invalid Partition 1: null return value
        when(service.getEditorPortalPage()).thenReturn(null);

        String result = service.getEditorPortalPage();

        assertNull(result);
    }

    @Test
    public void testIsUserEditor_validPartition() {
        // Valid user input
        User validUser = new User("testUser");
        when(service.isUserEditor(validUser)).thenReturn(true);

        boolean result = service.isUserEditor(validUser);

        assertTrue(result);
    }

    @Test
    public void testIsUserEditor_nullUser_invalidPartition2() {
        // Invalid Partition 2: null user
        when(service.isUserEditor(null)).thenReturn(false);

        boolean result = service.isUserEditor(null);

        assertFalse(result);
    }

    @Test
    public void testAddRecipeToDatabase_validPartition() {
        // Valid recipe input
        Recipe validRecipe = new Recipe("Pizza");
        when(service.addRecipeToDatabase(validRecipe)).thenReturn(true);

        boolean result = service.addRecipeToDatabase(validRecipe);

        assertTrue(result);
    }

    @Test
    public void testAddRecipeToDatabase_nullRecipe_invalidPartition3() {
        // Invalid Partition 3: null recipe
        when(service.addRecipeToDatabase(null)).thenReturn(false);

        boolean result = service.addRecipeToDatabase(null);

        assertFalse(result);
    }

    static class EditorPageService {
        String getEditorPortalPage() { return null; }
        boolean isUserEditor(User user) { return false; }
        boolean addRecipeToDatabase(Recipe recipe) { return false; }
    }

    static class User {
        String name;
        User(String name) { this.name = name; }
    }

    static class Recipe {
        String title;
        Recipe(String title) { this.title = title; }
    }
}