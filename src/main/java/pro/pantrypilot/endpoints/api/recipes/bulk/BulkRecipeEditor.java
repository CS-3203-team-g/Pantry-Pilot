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
import pro.pantrypilot.db.classes.ingredient.Ingredient;
import pro.pantrypilot.db.classes.ingredient.IngredientUnit;
import pro.pantrypilot.db.classes.ingredient.IngredientUnitsDatabase;
import pro.pantrypilot.db.classes.ingredient.IngredientsDatabase;
import pro.pantrypilot.db.classes.ingredient.NutritionFacts;
import pro.pantrypilot.db.classes.ingredient.NutritionFactsDatabase;
import pro.pantrypilot.db.classes.recipe.Recipe;
import pro.pantrypilot.db.classes.recipe.RecipeDatabase;
import pro.pantrypilot.db.classes.recipe.RecipeIngredient;
import pro.pantrypilot.db.classes.recipe.RecipeIngredientsDatabase;
import pro.pantrypilot.db.classes.unit.Unit;
import pro.pantrypilot.db.classes.unit.UnitsDatabase;

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

    public static String getRecipeIngredientsDatabaseAsJSON() {
        Gson gson = new Gson();
        return gson.toJson(RecipeIngredientsDatabase.getAllRecipeIngredients());
    }

    public static String getIngredientsDatabaseAsJSON() {
        Gson gson = new Gson();
        return gson.toJson(IngredientsDatabase.getAllIngredients());
    }
    
    public static String getUnitsDatabaseAsJSON() {
        Gson gson = new Gson();
        return gson.toJson(UnitsDatabase.getAllUnits());
    }
    
    public static String getIngredientUnitsDatabaseAsJSON() {
        Gson gson = new Gson();
        return gson.toJson(IngredientUnitsDatabase.getAllIngredientUnits());
    }
    
    public static String getNutritionFactsDatabaseAsJSON() {
        Gson gson = new Gson();
        return gson.toJson(NutritionFactsDatabase.getAllNutritionFacts());
    }

    public static String getAllRecipeDatabasesAsJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("recipes", new JSONArray(getRecipeDatabaseAsJSON()));
            json.put("recipeIngredients", new JSONArray(getRecipeIngredientsDatabaseAsJSON()));
            json.put("ingredients", new JSONArray(getIngredientsDatabaseAsJSON()));
            json.put("units", new JSONArray(getUnitsDatabaseAsJSON()));
            json.put("ingredientUnits", new JSONArray(getIngredientUnitsDatabaseAsJSON()));
            json.put("nutritionFacts", new JSONArray(getNutritionFactsDatabaseAsJSON()));
        }
        catch (JSONException e) {
            logger.error("Error creating JSON object", e);
            return null;
        }
        return json.toString(4);
    }

    public static boolean loadRecipeDatabasesFromJSON(String json) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        if (jsonObject == null || 
            !jsonObject.has("recipes") || 
            !jsonObject.has("recipeIngredients") || 
            !jsonObject.has("ingredients") || 
            !jsonObject.has("units")) {
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
        
        // Load units from JSON
        Type unitListType = new TypeToken<List<Unit>>(){}.getType();
        List<Unit> units = gson.fromJson(jsonObject.get("units"), unitListType);
        
        // Load ingredient units from JSON if present
        List<IngredientUnit> ingredientUnits = new ArrayList<>();
        if (jsonObject.has("ingredientUnits")) {
            Type ingredientUnitListType = new TypeToken<List<IngredientUnit>>(){}.getType();
            ingredientUnits = gson.fromJson(jsonObject.get("ingredientUnits"), ingredientUnitListType);
        }
        
        // Load nutrition facts from JSON if present
        List<NutritionFacts> nutritionFacts = new ArrayList<>();
        if (jsonObject.has("nutritionFacts")) {
            Type nutritionFactsListType = new TypeToken<List<NutritionFacts>>(){}.getType();
            nutritionFacts = gson.fromJson(jsonObject.get("nutritionFacts"), nutritionFactsListType);
        }

        Connection connection = DatabaseConnectionManager.getConnection();

        try {
            // Drop all tables with foreign key checks disabled
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
            stmt.executeUpdate("DROP TABLE IF EXISTS nutrition_facts, ingredient_units, recipe_ingredients, shopping_list_ingredients, ingredients, recipes, units;");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
            stmt.close();

            // Recreate all tables in the correct order
            UnitsDatabase.initializeUnitsDatabase();
            RecipeDatabase.initializeRecipeDatabase();
            IngredientsDatabase.initializeIngredientsDatabase();
            IngredientUnitsDatabase.initializeIngredientUnitsDatabase();
            NutritionFactsDatabase.initializeNutritionFactsDatabase();
            RecipeIngredientsDatabase.initializeRecipeIngredientsDatabase();

            // Load data into tables in the correct order (respect foreign key constraints)
            if (!UnitsDatabase.loadAllUnits(units)) {
                return false;
            }
            
            if (!RecipeDatabase.loadAllRecipes(recipes)) {
                return false;
            }

            if (!IngredientsDatabase.loadAllIngredients(ingredients)) {
                return false;
            }
            
            if (!ingredientUnits.isEmpty() && !IngredientUnitsDatabase.loadAllIngredientUnits(ingredientUnits)) {
                return false;
            }
            
            if (!nutritionFacts.isEmpty() && !NutritionFactsDatabase.loadAllNutritionFacts(nutritionFacts)) {
                return false;
            }

            if (!RecipeIngredientsDatabase.loadAllRecipeIngredients(recipeIngredients)) {
                return false;
            }

        } catch (SQLException e) {
            logger.error("Error loading databases", e);
            return false;
        }
        return true;
    }
}
