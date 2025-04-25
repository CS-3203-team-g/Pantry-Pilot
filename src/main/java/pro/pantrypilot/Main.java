package pro.pantrypilot;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.DatabaseConnectionManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {

        logger.info("Starting Pantry Pilot");

        logger.info("Loading Config");
        logger.info("Config Loaded");

        logger.info("Connecting to Database");
        DatabaseConnectionManager.connectToDatabase();
        logger.info("Connected to Database");
        logger.info("Initializing Database");
        DatabaseConnectionManager.initializeDatabase();
        logger.info("Database Initialized");

        logger.info("Building HttpServer Server");
        HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(System.getenv("SERVER_PORT"))), 0);

//        static webpages
        logger.info("Creating Static Contexts");
        server.createContext("/", new pro.pantrypilot.endpoints.pages.Index());
        server.createContext("/login", new pro.pantrypilot.endpoints.pages.login.Login());
        server.createContext("/signup", new pro.pantrypilot.endpoints.pages.signup.SignUp());
        server.createContext("/settings", new pro.pantrypilot.endpoints.pages.settings.Settings());
        server.createContext("/logout", new pro.pantrypilot.endpoints.pages.logout.Logout());
        server.createContext("/recipes", new pro.pantrypilot.endpoints.pages.recipes.Recipes());
        server.createContext("/recipe", new pro.pantrypilot.endpoints.pages.recipes.Recipe());
        server.createContext("/lists", new pro.pantrypilot.endpoints.pages.lists.Lists());
        server.createContext("/list", new pro.pantrypilot.endpoints.pages.lists.List());

//        API endpoints
        logger.info("Creating API Contexts");
        server.createContext("/api/createUser", new pro.pantrypilot.endpoints.api.signup.CreateUser());
        server.createContext("/api/login", new pro.pantrypilot.endpoints.api.login.Login());
        server.createContext("/api/logout", new pro.pantrypilot.endpoints.api.login.Logout());
        server.createContext("/api/changePassword", new pro.pantrypilot.endpoints.api.settings.ChangePassword());
        server.createContext("/api/getRecipesWithoutIngredients", new pro.pantrypilot.endpoints.api.recipes.GetRecipesWithoutIngredients());
        server.createContext("/api/getRecipesWithIngredients", new pro.pantrypilot.endpoints.api.recipes.GetRecipesWithIngredients());
        server.createContext("/api/getRecipeWithIngredients", new pro.pantrypilot.endpoints.api.recipes.GetRecipeWithIngredients());
        server.createContext("/api/getShoppingLists", new pro.pantrypilot.endpoints.api.shoppingLists.GetShoppingLists());
        server.createContext("/api/getShoppingListWithIngredients", new pro.pantrypilot.endpoints.api.shoppingLists.GetShoppingListWithIngredients());
        server.createContext("/api/createShoppingList", new pro.pantrypilot.endpoints.api.shoppingLists.CreateShoppingList());
        server.createContext("/api/getIngredients", new pro.pantrypilot.endpoints.api.recipes.GetIngredients());
        server.createContext("/api/addIngredientToShoppingList", new pro.pantrypilot.endpoints.api.shoppingLists.AddIngredientToShoppingList());
        server.createContext("/api/addRecipeIngredientsToShoppingList", new pro.pantrypilot.endpoints.api.shoppingLists.AddRecipeIngredientsToShoppingList());
        server.createContext("/api/removeIngredientFromShoppingList", new pro.pantrypilot.endpoints.api.shoppingLists.RemoveIngredientFromShoppingList());

//      Admin pages
        server.createContext("/admin", new pro.pantrypilot.endpoints.pages.admin.Admin());
        server.createContext("/api/admin/getTotalUsers", new pro.pantrypilot.endpoints.api.admin.GetTotalUsers());
        server.createContext("/api/admin/getTotalRecipes", new pro.pantrypilot.endpoints.api.admin.GetTotalRecipes());
        server.createContext("/api/admin/getActiveSessions", new pro.pantrypilot.endpoints.api.admin.GetActiveSessions());
        server.createContext("/api/admin/getRecipeDatabases", new pro.pantrypilot.endpoints.api.admin.GetRecipeDatabasesJSON());
        server.createContext("/api/admin/loadRecipeDatabases", new pro.pantrypilot.endpoints.api.admin.LoadRecipeDatabasesJSON());


        logger.info("Starting HttpServer");
        server.start();
        logger.info("HttpServer Started");

    }

}
