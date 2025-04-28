package pro.pantrypilot.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.recipe.IngredientsDatabase;
import pro.pantrypilot.db.classes.recipe.RecipeDatabase;
import pro.pantrypilot.db.classes.recipe.RecipeIngredientsDatabase;
import pro.pantrypilot.db.classes.session.SessionsDatabase;
import pro.pantrypilot.db.classes.shoppingList.ShoppingListIngredientsDatabase;
import pro.pantrypilot.db.classes.shoppingList.ShoppingListsDatabase;
import pro.pantrypilot.db.classes.user.UsersDatabase;
import pro.pantrypilot.db.classes.userHealthInfo.UserHealthInfoDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);

    private static Connection conn;

    // Replace the single DB_URL with individual components
    private static final String DB_HOST = System.getenv("DB_HOST");
    private static final String DB_NAME = System.getenv("DB_NAME");
    private static final String DB_USERNAME = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    // Construct the URL when needed
    private static String getDbUrl() {
        return String.format("jdbc:mariadb://%s:3306/%s", DB_HOST, DB_NAME);
    }

    public static void connectToDatabase() {
        try {
            logger.info("Loading Database Driver");
            Class.forName("org.mariadb.jdbc.Driver");
            logger.info("Successfully loaded database driver");
        } catch (ClassNotFoundException e) {
            logger.error("Error loading database driver", e);
        }

        try {
            logger.info("Connecting to Database");
            conn = DriverManager.getConnection(getDbUrl(), DB_USERNAME, DB_PASSWORD);
            logger.info("Successfully connected to database");
        } catch (SQLException e) {
            logger.error("Error connecting to database", e);
            throw new RuntimeException("Error connecting to database", e);
        }
    }

    public static Connection getConnection() {
        try {
            if(conn == null || conn.isClosed()) {
                logger.info("Reconnecting to Database");
                connectToDatabase();
            }
        } catch (SQLException e) {
            logger.error("Error checking connection", e);
        }
        return conn;
    }

    public static void initializeDatabase() {

        UsersDatabase.initializeUserDatabase();
        SessionsDatabase.initializeSessionsDatabase();
        IngredientsDatabase.initializeIngredientsDatabase();
        RecipeDatabase.initializeRecipeDatabase();
        RecipeIngredientsDatabase.initializeRecipeIngredientsDatabase();
        ShoppingListsDatabase.initializeShoppingListsDatabase();
        ShoppingListIngredientsDatabase.initializeShoppingListIngredientsDatabase();
        UserHealthInfoDatabase.initializeUserHealthInfoDatabase(); //might need to move this to the front

    }
}
