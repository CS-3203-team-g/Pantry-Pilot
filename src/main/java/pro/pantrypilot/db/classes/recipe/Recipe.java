package pro.pantrypilot.db.classes.recipe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
