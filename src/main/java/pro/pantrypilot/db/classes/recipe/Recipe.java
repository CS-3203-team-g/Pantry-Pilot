package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Recipe {

    private static final Logger logger = LoggerFactory.getLogger(Recipe.class);

    private final int recipeID;
    private final String title;
    private final String thumbnailUrl;
    private final String instructions;
    private ArrayList<RecipeIngredient> ingredients;
    private String ingredientsJson;
    private final float rating;
    private String tags;
    private Integer prepTimeMinutes;
    private Integer cookTimeMinutes;
    private Integer servings;

    public Recipe(int recipeID, String title, String thumbnailUrl, String instructions, ArrayList<RecipeIngredient> ingredients, float rating, String tags) {
        this.recipeID = recipeID;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.instructions = instructions;
        this.ingredients = (ingredients != null) ? ingredients : new ArrayList<>();
        this.ingredientsJson = null;
        this.rating = rating;
        this.tags = tags;
        this.prepTimeMinutes = null;
        this.cookTimeMinutes = null;
        this.servings = null;
    }

    public Recipe(int recipeID, String title, String thumbnailUrl, String instructions, float rating, String tags) {
        this.recipeID = recipeID;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.instructions = instructions;
        this.rating = rating;
        this.tags = tags;
        this.ingredients = new ArrayList<>();
        this.ingredientsJson = null;
        this.prepTimeMinutes = null;
        this.cookTimeMinutes = null;
        this.servings = null;
    }

    public Recipe(ResultSet resultSet){
        try {
            this.recipeID = resultSet.getInt("recipeID");
            this.title = resultSet.getString("title");
            this.thumbnailUrl = resultSet.getString("thumbnailUrl");
            this.instructions = resultSet.getString("instructions");
            this.rating = resultSet.getFloat("rating");
            this.tags = resultSet.getString("tags");
            this.ingredients = new ArrayList<>();

            this.prepTimeMinutes = (Integer) resultSet.getObject("prep_time_minutes");
            this.cookTimeMinutes = (Integer) resultSet.getObject("cook_time_minutes");
            this.servings = (Integer) resultSet.getObject("servings");
            this.ingredientsJson = resultSet.getString("ingredients_json");

        } catch (SQLException e) {
            logger.error("Error creating recipe from ResultSet", e);
            throw new RuntimeException(e);
        }
    }

    // Constructor specifically for AI-generated recipes (without recipeID initially)
    public Recipe(String title, String thumbnailUrl, String instructions, String ingredientsJson, float rating, String tags, Integer prepTimeMinutes, Integer cookTimeMinutes, Integer servings) {
        this.recipeID = 0; // Default to 0 for new recipes before insertion
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.instructions = instructions;
        this.ingredients = new ArrayList<>(); // Initialize empty, primary ingredients are in JSON
        this.ingredientsJson = ingredientsJson;
        this.rating = rating; // Default rating for new recipes
        this.tags = tags;
        this.prepTimeMinutes = prepTimeMinutes;
        this.cookTimeMinutes = cookTimeMinutes;
        this.servings = servings;
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

    public String getTagsString() {
        return tags;
    }

    public List<String> getTagsList() {
        if (tags == null || tags.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(tags.split(","))
                     .map(String::trim)
                     .filter(tag -> !tag.isEmpty())
                     .collect(Collectors.toList());
    }

    public void setTags(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            this.tags = null;
        } else {
            this.tags = tagList.stream()
                               .map(String::trim)
                               .filter(tag -> !tag.isEmpty())
                               .distinct()
                               .collect(Collectors.joining(","));
        }
    }

    public void setTagsString(String tags) {
        this.tags = tags;
    }

    public String getIngredientsJson() {
        return ingredientsJson;
    }

    public void setIngredientsJson(String ingredientsJson) {
        this.ingredientsJson = ingredientsJson;
    }

    public Integer getPrepTimeMinutes() {
        return prepTimeMinutes;
    }

    public void setPrepTimeMinutes(Integer prepTimeMinutes) {
        this.prepTimeMinutes = prepTimeMinutes;
    }

    public Integer getCookTimeMinutes() {
        return cookTimeMinutes;
    }

    public void setCookTimeMinutes(Integer cookTimeMinutes) {
        this.cookTimeMinutes = cookTimeMinutes;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "recipeID=" + recipeID +
                ", title='" + title + '\'' +
                ", thumbnailUrl='" + thumbnailUrl + '\'' +
                ", instructions='" + instructions + '\'' +
                ", ingredients=" + ingredients +
                ", ingredientsJson='" + ingredientsJson + '\'' +
                ", rating=" + rating +
                ", tags='" + tags + '\'' +
                ", prepTimeMinutes=" + prepTimeMinutes +
                ", cookTimeMinutes=" + cookTimeMinutes +
                ", servings=" + servings +
                '}';
    }
}
