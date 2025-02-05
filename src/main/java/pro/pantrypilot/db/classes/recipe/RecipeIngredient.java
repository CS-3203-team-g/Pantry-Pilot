package pro.pantrypilot.db.classes.recipe;

public class RecipeIngredient {

    private final int recipeID;
    private final int ingredientID;
    private final int quantity;
    private final String unit;
    private String ingredientName;


    public RecipeIngredient(int recipeID, int ingredientID, int quantity, String unit) {
        this.recipeID = recipeID;
        this.ingredientID = ingredientID;
        this.quantity = quantity;
        this.unit = unit;
    }

    public RecipeIngredient(int recipeID, int ingredientID, int quantity, String unit, String ingredientName) {
        this.recipeID = recipeID;
        this.ingredientID = ingredientID;
        this.quantity = quantity;
        this.unit = unit;
        this.ingredientName = ingredientName;
    }


    public int getRecipeID() {
        return recipeID;
    }

    public int getIngredientID() {
        return ingredientID;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    @Override
    public String toString() {
        return "RecipeIngredient{" +
                "recipeID=" + recipeID +
                ", ingredientID=" + ingredientID +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                '}';
    }
}
