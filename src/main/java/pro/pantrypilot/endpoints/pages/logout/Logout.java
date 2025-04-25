package pro.pantrypilot.endpoints.pages.logout;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.helpers.FileHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Logout implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(Logout.class);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] responseBytes;
        try {
            // Read the file as bytes and then convert to String with UTF-8 encoding
            byte[] fileBytes = FileHelper.readFile("static/logout/logout.html");
            String response = new String(fileBytes, StandardCharsets.UTF_8);
            responseBytes = response.getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Error reading logout.html", e);
            responseBytes = "Error: Unable to load logout.html".getBytes(StandardCharsets.UTF_8);
        }

        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}