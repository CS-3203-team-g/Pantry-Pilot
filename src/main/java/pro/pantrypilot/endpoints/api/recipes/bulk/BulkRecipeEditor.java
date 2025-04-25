package pro.pantrypilot.endpoints.api.recipes.bulk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;
import pro.pantrypilot.db.classes.recipe.IngredientsDatabase;
import pro.pantrypilot.db.classes.recipe.RecipeDatabase;
import pro.pantrypilot.db.classes.recipe.RecipeIngredientsDatabase;
import pro.pantrypilot.db.classes.recipe.Recipe;          // Assuming this exists
import pro.pantrypilot.db.classes.recipe.RecipeIngredient; // Assuming this exists
import pro.pantrypilot.db.classes.recipe.Ingredient;       // Assuming this exists
import pro.pantrypilot.endpoints.api.admin.LoadRecipeDatabasesJSON;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BulkRecipeEditor {

    private static final Logger logger = LoggerFactory.getLogger(BulkRecipeEditor.class);


    public static String getRecipeDatabaseAsJSON() {
        Gson gson = new Gson();
        return gson.toJson(RecipeDatabase.getAllRecipes());
    }

    public static String getRecipeIngredientsDatabaseAsJSON(){
        Gson gson = new Gson();
        return gson.toJson(RecipeIngredientsDatabase.getAllRecipeIngredients());
    }

    public static String getIngredientsDatabaseAsJSON(){
        Gson gson = new Gson();
        return gson.toJson(IngredientsDatabase.getAllIngredients());
    }

    public static String getAllRecipeDatabasesAsJSON() {
        JSONObject json = new JSONObject();

        try {
            json.put("recipes", new JSONArray(getRecipeDatabaseAsJSON())); // Use JSONArray here
            json.put("recipeIngredients", new JSONArray(getRecipeIngredientsDatabaseAsJSON()));
            json.put("ingredients", new JSONArray(getIngredientsDatabaseAsJSON()));
        }
        catch (JSONException e) {
            logger.error("Error creating JSON object", e);
            return null;
        }

        return json.toString(4);
    }


    public static boolean loadRecipeDatabasesFromJSON(String json){
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        if(jsonObject == null || !jsonObject.has("recipes") || !jsonObject.has("recipeIngredients") || !jsonObject.has("ingredients")){
            return false;
        }

        // Load recipes from JSON
        Type recipeListType = new TypeToken<List<Recipe>>(){}.getType();
        ArrayList<Recipe> recipes = gson.fromJson(jsonObject.get("recipes"), recipeListType);

        // Load recipe ingredients from JSON
        Type recipeIngredientListType = new TypeToken<List<RecipeIngredient>>(){}.getType();
        List<RecipeIngredient> recipeIngredients = gson.fromJson(jsonObject.get("recipeIngredients"), recipeIngredientListType);

        // Load ingredients from JSON
        Type ingredientListType = new TypeToken<List<Ingredient>>(){}.getType();
        List<Ingredient> ingredients = gson.fromJson(jsonObject.get("ingredients"), ingredientListType);

        Connection connection = DatabaseConnectionManager.getConnection();

        try {
            // Drop all 3 tables
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.executeUpdate("DROP TABLE IF EXISTS recipe_ingredients, recipes, ingredients;");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
            stmt.close();

            // Recreate all tables
            RecipeDatabase.initializeRecipeDatabase();
            IngredientsDatabase.initializeIngredientsDatabase();
            RecipeIngredientsDatabase.initializeRecipeIngredientsDatabase();

            // Load data into tables
            if(!RecipeDatabase.loadAllRecipes(recipes)){
                return false;
            }

            if(!IngredientsDatabase.loadAllIngredients(ingredients)){
                return false;
            }

            if(!RecipeIngredientsDatabase.loadAllRecipeIngredients(recipeIngredients)){
                return false;
            }

        } catch (SQLException e) {
            logger.error("Error loading recipes", e);
            return false;
        }

        return true;
    }
}
