package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.ingredient.NutritionFacts;
import pro.pantrypilot.db.classes.ingredient.NutritionFactsDatabase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Recipe {

    private static final Logger logger = LoggerFactory.getLogger(Recipe.class);

    private final int recipeID;
    private final String title;
    private final String thumbnailUrl;
    private final String instructions;
    private ArrayList<RecipeIngredient> ingredients;
    private final float rating;


    public Recipe(int recipeID, String title, String thumbnailUrl, String instructions, ArrayList<RecipeIngredient> ingredients, float rating) {
        this.recipeID = recipeID;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.instructions = instructions;
        this.ingredients = ingredients;
        this.rating = rating;
    }

    public Recipe(int recipeID, String title, String thumbnailUrl, String instructions, float rating) {
        this.recipeID = recipeID;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.instructions = instructions;
        this.rating = rating;
    }

    public Recipe(ResultSet resultSet){
        try {
            this.recipeID = resultSet.getInt("recipeID");
            this.title = resultSet.getString("title");
            this.thumbnailUrl = resultSet.getString("thumbnailUrl");
            this.instructions = resultSet.getString("instructions");
            this.rating = resultSet.getFloat("rating");
        } catch (SQLException e) {
            logger.error("Error creating recipe from ResultSet", e);
            throw new RuntimeException(e);
        }
    }

    public int getRecipeID() {
        return recipeID;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getInstructions() {
        return instructions;
    }

    public ArrayList<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public float getRating() {
        return rating;
    }
    
    /**
     * Calculate the total nutritional information for the entire recipe
     * using efficient SQL-based calculation.
     * 
     * @return A NutritionFacts object containing the total nutritional information for the recipe
     */
    public NutritionFacts calculateTotalNutrition() {
        return NutritionFactsDatabase.calculateRecipeNutrition(this.recipeID);
    }

    /**
     * Calculate the nutritional information for each ingredient in the recipe.
     * Note: This method is kept for backwards compatibility but should be avoided
     * for performance reasons. Use calculateTotalNutrition() instead.
     * 
     * @return A list of nutritional facts for each ingredient
     */
    @Deprecated
    public List<NutritionFacts> calculateIngredientNutrition() {
        if (this.ingredients == null || this.ingredients.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> ingredientIDs = new ArrayList<>();
        List<Float> quantities = new ArrayList<>();
        List<Integer> unitIDs = new ArrayList<>();
        
        for (RecipeIngredient ingredient : this.ingredients) {
            if (ingredient.getUnitID() != null) {
                ingredientIDs.add(ingredient.getIngredientID());
                quantities.add((float) ingredient.getQuantity());
                unitIDs.add(ingredient.getUnitID());
            }
        }
        
        Map<String, NutritionFacts> nutritionMap = NutritionFactsDatabase.calculateNutritionFactsForIngredients(
            ingredientIDs, quantities, unitIDs);
        
        List<NutritionFacts> nutritionList = new ArrayList<>();
        for (int i = 0; i < ingredientIDs.size(); i++) {
            String key = ingredientIDs.get(i) + "-" + unitIDs.get(i);
            NutritionFacts facts = nutritionMap.get(key);
            if (facts != null) {
                nutritionList.add(facts);
            }
        }
        
        return nutritionList;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "recipeID=" + recipeID +
                ", title='" + title + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", instructions='" + instructions + '\'' +
                ", ingredients=" + ingredients +
                ", rating=" + rating +
                '}';
    }
}
