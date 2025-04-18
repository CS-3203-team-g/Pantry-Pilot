package pro.pantrypilot.endpoints.api.userStats;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.pantrypilot.db.classes.user.UsersDatabase;
import pro.pantrypilot.db.classes.userHealthInfo.UserHealthInfo;
import pro.pantrypilot.db.classes.userHealthInfo.UserHealthInfoDatabase;
import pro.pantrypilot.db.classes.session.Session;
import pro.pantrypilot.db.classes.session.SessionsDatabase;
import pro.pantrypilot.endpoints.api.shoppingLists.GetShoppingLists;
import pro.pantrypilot.endpoints.api.userStats.UpdateHealthStats;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class UpdateHealthStats implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateHealthStats.class);

    private static class UpdateHealthStatsRequest {
        double currWeight;
        double goalWeight;
        int age;
        double height;
        String activityLevel;
        String gender;
        String dietaryPreferences;

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Request received at /api/updateHealthStats with method: " + exchange.getRequestMethod());

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            logger.debug("Invalid request method: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String requestBody;
        try(InputStream is = exchange.getRequestBody();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            requestBody = reader.lines().collect(Collectors.joining("\n"));
        }
        catch (Exception ignore){
            logger.debug("Error reading request body");
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        String sessionId = new Gson().fromJson(requestBody, SessionRequest.class).sessionID;

        Session session = SessionsDatabase.getSession(sessionId);
        if(session == null) {
            logger.debug("No session found for id {}", sessionId);
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String userID = session.getUserID();

        // Initialize Gson and attempt to parse the JSON payload into a SignupRequest object
        Gson gson = new Gson();
        UpdateHealthStatsRequest updateHealthStatsRequest;
        try {
            updateHealthStatsRequest = gson.fromJson(requestBody, UpdateHealthStatsRequest.class);
        } catch(JsonSyntaxException e){
            // If parsing fails, return a 400 Bad Request response with an error message.
            String errorResponse = "{\"message\": \"Invalid JSON format\"}";
            logger.error("Invalid JSON format: {}", requestBody);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            byte[] errorBytes = errorResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(405, errorBytes.length);
            try(OutputStream os = exchange.getResponseBody()) {
                os.write(errorBytes);
            }
            return;
        }

        UserHealthInfo userHealthInfo = new UserHealthInfo(userID, updateHealthStatsRequest.currWeight,
                updateHealthStatsRequest.goalWeight, updateHealthStatsRequest.height, updateHealthStatsRequest.age,
                updateHealthStatsRequest.gender, updateHealthStatsRequest.dietaryPreferences, updateHealthStatsRequest.activityLevel);
        boolean success = UserHealthInfoDatabase.createUserHealthInfo(userHealthInfo);

        if (!success) {
            logger.debug("Error creating user health info");
        }
    }

    private static class SessionRequest {
        String sessionID;
    }
}
