package pro.pantrypilot.endpoints.api.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.session.Session;
import pro.pantrypilot.db.classes.session.SessionsDatabase;
import pro.pantrypilot.db.classes.user.User;
import pro.pantrypilot.endpoints.api.recipes.bulk.BulkRecipeEditor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class GetRecipeDatabasesJSON implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(GetRecipeDatabasesJSON.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Extract sessionID from cookies
        String sessionID = null;
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");

        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                cookie = cookie.trim();
                if (cookie.startsWith("sessionID=")) {
                    sessionID = cookie.substring("sessionID=".length());
                    break;
                }
            }
        }

        Session session = SessionsDatabase.getSession(sessionID);
        if(!session.isValid()) {
            logger.debug("Session not found, redirecting to login");
            exchange.getResponseHeaders().add("Location", "/login");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        User user = session.getUser();
        if(user == null) {
            logger.error("User not found for sessionID: {}", sessionID);
            exchange.sendResponseHeaders(500, -1);
            return;
        }

        if(!user.isAdmin()){
            logger.debug("User is not an admin, redirecting to index");
            exchange.getResponseHeaders().add("Location", "/");
            exchange.sendResponseHeaders(302, -1);
            return;
        }

        logger.debug("Getting recipe databases as JSON");
        String response = BulkRecipeEditor.getAllRecipeDatabasesAsJSON();

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);
        logger.debug("Response sent with {} bytes", responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
