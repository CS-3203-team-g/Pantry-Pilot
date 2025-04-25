package pro.pantrypilot.endpoints.api.userStats;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.session.Session;
import pro.pantrypilot.db.classes.session.SessionsDatabase;
import pro.pantrypilot.db.classes.userHealthInfo.UserHealthInfo;
import pro.pantrypilot.db.classes.userHealthInfo.UserHealthInfoDatabase;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class GetHealthStats implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(UpdateHealthStats.class);

    private static final Gson gson = new Gson(); // you need GSON for JSON parsing

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Request received at /api/getHealthStats with method: " + exchange.getRequestMethod());

        if (!"GET".equals(exchange.getRequestMethod())) {
            logger.debug("Invalid request received: {}", exchange.getRequestMethod());
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }


        String sessionID = null;
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");

        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split("; ");
            for (String cookie : cookies) {
                if (cookie.startsWith("sessionID=")) {
                    sessionID = cookie.substring("sessionID=".length());
                    break;
                }
            }
        }

        if (sessionID == null) {
            exchange.sendResponseHeaders(401, -1); // No sessionID, unauthorized
            return;
        }

        // 🔥 Lookup userID using sessionID (you must have a session system somewhere)
        String userID = getUserIDFromSessionID(sessionID, exchange);
        if (userID == null) {
            exchange.sendResponseHeaders(401, -1); // 401 Unauthorized
            return;
        }

        // 🔥 Now fetch user health info

        UserHealthInfo healthInfo = UserHealthInfoDatabase.getUserHealthInfo(userID);
        if (healthInfo == null) {
            exchange.sendResponseHeaders(404, -1); // 404 Not Found
            return;
        }

        // 🔥 Otherwise, return JSON of the user's health info
        String responseJson = gson.toJson(healthInfo);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseJson.getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(responseJson.getBytes());
        os.close();
    }

    private String getUserIDFromSessionID(String sessionID, HttpExchange exchange) throws IOException {

        Session session = SessionsDatabase.getSession(sessionID);
        if(session == null) {
            logger.debug("No session found for id {}", sessionID);
            exchange.sendResponseHeaders(404, -1);
            return null;
        }

        return session.getUserID();
    }
}
