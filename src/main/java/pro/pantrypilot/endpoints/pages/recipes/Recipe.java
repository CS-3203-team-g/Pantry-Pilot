package pro.pantrypilot.endpoints.pages.recipes;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.helpers.FileHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Recipe implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(Recipe.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] responseBytes;
        try {
            // Read all template files
            String baseTemplate = new String(FileHelper.readFile("static/templates/recipe-base.html"), StandardCharsets.UTF_8);
            String content = new String(FileHelper.readFile("static/templates/recipe.html"), StandardCharsets.UTF_8);
            String modals = new String(FileHelper.readFile("static/templates/recipe-modals.html"), StandardCharsets.UTF_8);
            String scripts = new String(FileHelper.readFile("static/templates/recipe-scripts.html"), StandardCharsets.UTF_8);

            // Replace template placeholders
            String response = baseTemplate
                .replace("{{PAGE_TITLE}}", "Recipe Details")
                .replace("{{PAGE_STYLES}}", "")
                .replace("{{CONTENT}}", content)
                .replace("{{PAGE_MODALS}}", modals)
                .replace("{{PAGE_SCRIPTS}}", scripts);

            responseBytes = response.getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Error reading recipe template files", e);
            responseBytes = "Error: Unable to load recipe templates".getBytes(StandardCharsets.UTF_8);
        }

        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}