package pro.pantrypilot.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class to interact with an AI model (e.g., Google Gemini)
 * to generate descriptive tags for recipes using Apache HttpClient (Java 8 compatible).
 */
public class RecipeAiTagger {

    private static final Logger logger = LoggerFactory.getLogger(RecipeAiTagger.class);
    // IMPORTANT: Store your API Key securely as an environment variable
    private static final String API_KEY = System.getenv("GEMINI_API_KEY");
    // Using Gemini 1.5 Flash - check for latest models if needed
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=";

    // Apache HttpClient configuration
    private static final int CONNECT_TIMEOUT_MS = 15 * 1000;
    private static final int SOCKET_TIMEOUT_MS = 20 * 1000;
    private static final RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(CONNECT_TIMEOUT_MS)
            .setSocketTimeout(SOCKET_TIMEOUT_MS)
            .build();
    // Create a single HttpClient instance (thread-safe)
    private static final CloseableHttpClient httpClient = HttpClients.custom()
            .setDefaultRequestConfig(requestConfig)
            .build();

    private static final Gson gson = new Gson();

    /**
     * Generates a list of descriptive tags for a recipe using an AI model.
     *
     * @param title        The title of the recipe.
     * @param instructions The instructions for the recipe.
     * @return A List of generated tags, or an empty list if generation fails or is skipped.
     */
    public static List<String> generateTags(String title, String instructions) {
        if (API_KEY == null || API_KEY.isEmpty()) {
            logger.warn("GEMINI_API_KEY environment variable not set. Skipping AI tagging.");
            return new ArrayList<>();
        }
        if ((title == null || title.trim().isEmpty()) && (instructions == null || instructions.trim().isEmpty())) {
             logger.warn("Recipe title and instructions are empty. Skipping AI tagging.");
             return new ArrayList<>();
        }

        // Construct the prompt asking for JSON output
        String prompt = String.format(
                "Analyze the following recipe title and instructions. Generate a list of 5-7 relevant, single-word or two-word descriptive tags (e.g., Dinner, Quick, Easy, Vegetarian, Italian, Dessert, Baking, Chicken, Pasta, Healthy, Spicy, Gluten-Free). Focus on cuisine type, meal type, main ingredients, cooking method, or dietary restrictions. Return ONLY a JSON array of strings, like [\"Tag1\", \"Tag2\", \"Tag3\"].\n\nTitle: %s\nInstructions: %s",
                title,
                instructions != null && instructions.length() > 500 ? instructions.substring(0, 500) + "..." : instructions // Limit instruction length for API call
        );

        // --- Construct Gemini API Request Body ---
        JsonObject contentPart = new JsonObject();
        contentPart.addProperty("text", prompt);
        JsonArray partsArray = new JsonArray();
        partsArray.add(contentPart);
        JsonObject contents = new JsonObject();
        contents.add("parts", partsArray);
        JsonArray contentsArray = new JsonArray();
        contentsArray.add(contents);

        // Configure for JSON output
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("response_mime_type", "application/json");

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.add("contents", contentsArray);
        requestBodyJson.add("generationConfig", generationConfig);
        String requestBodyString = gson.toJson(requestBodyJson);
        // --- End Request Body Construction ---

        // Build the Apache HttpClient POST request
        HttpPost httpPost = new HttpPost(API_URL + API_KEY);
        httpPost.setHeader("Content-Type", "application/json");

        CloseableHttpResponse response = null; // Declare outside try for finally block
        try {
            // Set the request body entity
            StringEntity requestEntity = new StringEntity(requestBodyString, StandardCharsets.UTF_8);
            httpPost.setEntity(requestEntity);

            logger.info("Requesting recipe tags from AI for title: {}", title);
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity responseEntity = response.getEntity();
            String responseBody = (responseEntity != null) ? EntityUtils.toString(responseEntity, StandardCharsets.UTF_8) : null;

            if (statusCode == 200) {
                logger.debug("AI Response Body: {}", responseBody);
                return parseTagsFromResponse(responseBody, title);
            } else {
                logger.error("Error calling AI API: Status Code: {} - Response: {}", statusCode, responseBody);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            // Catch specific exceptions like IOException, InterruptedException if needed
            logger.error("Exception occurred during AI API call or processing", e);
            return new ArrayList<>();
        } finally {
            // Ensure response is closed to release connection
            if (response != null) {
                try {
                    response.close();
                } catch (Exception e) {
                    logger.error("Error closing HTTP response", e);
                }
            }
            // Note: Don't close the shared httpClient instance here
        }
    }

    /**
     * Parses the expected JSON array of tags from the Gemini API response.
     *
     * @param responseBody The JSON response string from the API.
     * @param title        The recipe title (for logging purposes).
     * @return A list of tags, or an empty list if parsing fails.
     */
    private static List<String> parseTagsFromResponse(String responseBody, String title) {
        try {
            JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
            JsonArray candidates = responseJson.getAsJsonArray("candidates");

            if (candidates != null && !candidates.isEmpty()) {
                JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                // Check for safety ratings or finish reason if necessary
                // String finishReason = firstCandidate.get("finishReason").getAsString();
                // if (!"STOP".equals(finishReason)) { ... handle blocked content ... }

                JsonObject content = firstCandidate.getAsJsonObject("content");
                if (content != null) {
                    JsonArray parts = content.getAsJsonArray("parts");
                    if (parts != null && !parts.isEmpty()) {
                        // Expecting the text part to contain the JSON array string
                        String textPart = parts.get(0).getAsJsonObject().get("text").getAsString();
                        JsonArray tagsJsonArray = JsonParser.parseString(textPart).getAsJsonArray();
                        List<String> tags = new ArrayList<>();
                        tagsJsonArray.forEach(tagElement -> {
                            String tag = tagElement.getAsString().trim();
                            if (!tag.isEmpty()) { // Add only non-empty tags
                                tags.add(tag);
                            }
                        });
                        logger.info("Successfully parsed tags for title '{}': {}", title, tags);
                        return tags;
                    }
                }
            }
            logger.warn("Could not parse tags from AI response structure for title: {}", title);
            return new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error parsing JSON response from AI for title '{}': {}", title, responseBody, e);
            return new ArrayList<>();
        }
    }
} 