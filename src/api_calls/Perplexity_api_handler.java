package api_calls;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handler for making API calls to the Perplexity AI API
 * Manages API requests, responses, and error handling for Perplexity interactions
 */
public class Perplexity_api_handler {
    private static final String API_ENDPOINT = "https://api.perplexity.ai/chat/completions";
    private String apiKey;
    private final ExecutorService executor;
    
    /**
     * Constructor initializes thread pool for API requests
     */
    public Perplexity_api_handler() {
        // Initialize with an empty API key - should be set later through settings
        this.apiKey = "";
        // Create a thread pool for managing concurrent API requests
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    /**
     * Sets the API key for Perplexity API authentication
     * @param apiKey The Perplexity API key
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    /**
     * Checks if the API key has been set
     * @return true if API key is valid
     */
    public boolean hasValidApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Makes an asynchronous call to the Perplexity API
     * @param prompt The user's input query
     * @return CompletableFuture containing the API response
     */
    public CompletableFuture<String> makeAsyncApiCall(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return makeApiCall(prompt);
            } catch (Exception e) {
                return "Error calling Perplexity API: " + e.getMessage();
            }
        }, executor);
    }
    
    /**
     * Makes a synchronous call to the Perplexity API
     * @param prompt The user's input query
     * @return The API response as a string
     * @throws Exception if the API call fails
     */
    public String makeApiCall(String prompt) throws Exception {
        if (!hasValidApiKey()) {
            return "Error: API key not set. Please configure your Perplexity API key in Settings.";
        }
        
        // Create connection to the API endpoint
        URL url = new URL(API_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        
        // Prepare the request payload
        // NOTE: In a real implementation, you would need to format this according to the Perplexity API specs
        String jsonInputString = String.format(
            "{\"model\": \"sonar-medium-online\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"options\": {\"temperature\": 0.7}}",
            prompt.replace("\"", "\\\"") // Escape quotes in the prompt
        );
        
        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        
        // Read the response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (Exception e) {
            // If there was an error reading the input stream, try to read the error stream
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                String responseLine;
                StringBuilder errorResponse = new StringBuilder("API Error: ");
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine.trim());
                }
                return errorResponse.toString();
            }
        }
        
        // In a real implementation, you would parse the JSON response to extract the message content
        // For example: return parseJsonResponse(response.toString());
        
        // For demonstration, return a simplified version
        return "Perplexity response to: " + prompt + "\n\n" + simulateResponse(prompt);
    }
    
    /**
     * Simulates a Perplexity response for demonstration purposes
     * In a real implementation, this would be replaced with actual API response parsing
     * @param prompt The input prompt
     * @return A simulated response
     */
    private String simulateResponse(String prompt) {
        // This is just a placeholder for demonstration
        String[] responses = {
            "I've searched the web and found the following information...",
            "According to the latest information available online...",
            "Multiple sources suggest that...",
            "Based on my real-time search capabilities, I can tell you that...",
            "The most up-to-date information indicates that..."
        };
        
        // Simple simulation based on prompt length
        int index = Math.abs(prompt.hashCode() % responses.length);
        return responses[index] + " [Simulated Perplexity response with web-search capability]";
    }
    
    /**
     * Shuts down the executor service
     */
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
