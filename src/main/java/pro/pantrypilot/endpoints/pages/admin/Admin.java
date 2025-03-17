package pro.pantrypilot.endpoints.pages.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.pantrypilot.db.classes.session.Session;
import pro.pantrypilot.db.classes.session.SessionsDatabase;
import pro.pantrypilot.db.classes.user.User;
import pro.pantrypilot.helpers.FileHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Admin implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(Admin.class);
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    
    static {
        MIME_TYPES.put("js", "application/javascript; charset=UTF-8");
        MIME_TYPES.put("css", "text/css; charset=UTF-8");
        MIME_TYPES.put("html", "text/html; charset=UTF-8");
    }

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

        // Parse the request path
        String path = exchange.getRequestURI().getPath();
        
        // Check if this is a request for a static file
        if (path.matches(".+\\.(js|css)$")) {
            serveStaticFile(exchange, path);
            return;
        }

        // Handle admin page templates
        String templateName = "dashboard"; // default template
        String pageScripts = "<script src=\"/admin/js/dashboard.js\"></script>";
        
        if (path.equals("/admin/content") || path.equals("/admin/content/")) {
            templateName = "content-management";
            pageScripts = "<script src=\"/admin/js/content-management.js\"></script>";
        } else if (path.equals("/admin/recipe-editor") || path.equals("/admin/recipe-editor/")) {
            templateName = "recipe-editor";
            pageScripts = "<script src=\"/admin/js/recipe-editor.js\"></script>";
        } else if (!path.equals("/admin") && !path.equals("/admin/") && !path.equals("/admin/dashboard") && !path.equals("/admin/dashboard/")) {
            // Any other unrecognized path under /admin should redirect to /admin
            exchange.getResponseHeaders().add("Location", "/admin");
            exchange.sendResponseHeaders(302, -1);
            return;
        }
        
        logger.debug("Serving admin page: {}", templateName);

        try {
            // Read the base template
            String baseTemplate = new String(FileHelper.readFile("static/admin/templates/admin-base.html"), StandardCharsets.UTF_8);
            
            // Read the specific content template
            String contentTemplate = new String(FileHelper.readFile("static/admin/templates/" + templateName + ".html"), StandardCharsets.UTF_8);
            
            // Replace placeholder with content
            String fullPage = baseTemplate.replace("{{CONTENT}}", contentTemplate)
                                        .replace("{{PAGE_SCRIPTS}}", pageScripts);
            
            byte[] responseBytes = fullPage.getBytes(StandardCharsets.UTF_8);
            
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            logger.debug("Response sent with {} bytes", responseBytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
            
        } catch (IOException e) {
            logger.error("Error reading template files", e);
            String errorMessage = "Error: Unable to load admin page";
            byte[] errorBytes = errorMessage.getBytes(StandardCharsets.UTF_8);
            
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(500, errorBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorBytes);
            }
        }
    }

    private void serveStaticFile(HttpExchange exchange, String requestPath) throws IOException {
        // Remove leading slash and admin/ to get relative path
        String relativePath = requestPath.replaceFirst("^/admin/", "");
        String filePath = "static/admin/" + relativePath;

        try {
            byte[] content = FileHelper.readFile(filePath);
            
            // Get file extension to determine content type
            String extension = requestPath.substring(requestPath.lastIndexOf('.') + 1).toLowerCase();
            String contentType = MIME_TYPES.getOrDefault(extension, "application/octet-stream");
            
            exchange.getResponseHeaders().add("Content-Type", contentType);
            exchange.sendResponseHeaders(200, content.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
            
            logger.debug("Served static file: {} ({} bytes)", filePath, content.length);
        } catch (IOException e) {
            logger.error("Error serving static file: {}", filePath, e);
            String errorMessage = "Error: File not found";
            byte[] errorBytes = errorMessage.getBytes(StandardCharsets.UTF_8);
            
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(404, errorBytes.length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(errorBytes);
            }
        }
    }
}
