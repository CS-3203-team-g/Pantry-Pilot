package pro.pantrypilot.db.classes.shoppingList;

import java.math.BigDecimal;

public class ShoppingListIngredient {
    private final int shoppingListID;
    private final int ingredientID;
    private final BigDecimal quantity; // Changed from int to BigDecimal
    private final String unit;
    private String ingredientName;

    public ShoppingListIngredient(int shoppingListID, int ingredientID, BigDecimal quantity, String unit) {
        this.shoppingListID = shoppingListID;
        this.ingredientID = ingredientID;
        this.quantity = quantity;
        this.unit = unit;
    }

    public ShoppingListIngredient(int shoppingListID, int ingredientID, BigDecimal quantity, String unit, String ingredientName) {
        this.shoppingListID = shoppingListID;
        this.ingredientID = ingredientID;
        this.quantity = quantity;
        this.unit = unit;
        this.ingredientName = ingredientName;
    }
    
    // For backward compatibility
    public ShoppingListIngredient(int shoppingListID, int ingredientID, int quantity, String unit) {
        this(shoppingListID, ingredientID, new BigDecimal(quantity), unit);
    }
    
    // For backward compatibility
    public ShoppingListIngredient(int shoppingListID, int ingredientID, int quantity, String unit, String ingredientName) {
        this(shoppingListID, ingredientID, new BigDecimal(quantity), unit, ingredientName);
    }

    public int getShoppingListID() {
        return shoppingListID;
    }

    public int getIngredientID() {
        return ingredientID;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
    
    // For backward compatibility
    public int getQuantityAsInt() {
        return quantity.intValue();
    }

    public String getUnit() {
        return unit;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    @Override
    public String toString() {
        return "ShoppingListIngredient{" +
                "shoppingListID=" + shoppingListID +
                ", ingredientID=" + ingredientID +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", ingredientName='" + ingredientName + '\'' +
                '}';
    }
}