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
 * Handler for making API calls to the OpenAI ChatGPT API
 * Manages API requests, responses, and error handling for ChatGPT interactions
 */
public class ChatGPT_api_handler {
    private static final String API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private String apiKey;
    private final ExecutorService executor;
    
    /**
     * Constructor initializes thread pool for API requests
     */
    public ChatGPT_api_handler() {
        // Initialize with an empty API key - should be set later through settings
        this.apiKey = "";
        // Create a thread pool for managing concurrent API requests
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    /**
     * Sets the API key for OpenAI API authentication
     * @param apiKey The OpenAI API key
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
     * Makes an asynchronous call to the ChatGPT API
     * @param prompt The user's input query
     * @return CompletableFuture containing the API response
     */
    public CompletableFuture<String> makeAsyncApiCall(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return makeApiCall(prompt);
            } catch (Exception e) {
                return "Error calling ChatGPT API: " + e.getMessage();
            }
        }, executor);
    }
    
    /**
     * Makes a synchronous call to the ChatGPT API
     * @param prompt The user's input query
     * @return The API response as a string
     * @throws Exception if the API call fails
     */
    public String makeApiCall(String prompt) throws Exception {
        if (!hasValidApiKey()) {
            return "Error: API key not set. Please configure your OpenAI API key in Settings.";
        }
        
        // Create connection to the API endpoint
        URL url = new URL(API_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        
        // Prepare the request payload
        // NOTE: In a real implementation, you would need to format this according to the OpenAI API specs
        String jsonInputString = String.format(
        		"{\"model\": \"gpt-4.1-nano\","
//              + " \"messages\": ["
              + " \"messages\": [{\"role\": \"system\", \"content\": \"Give me a response in a simple and concise string format with no Regex.\"},"
              + "{\"role\": \"user\", \"content\": \"%s\"}]}",
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
        return simulateResponse(response.toString());
    }
    
    /**
     * Simulates a ChatGPT response for demonstration purposes
     * In a real implementation, this would be replaced with actual API response parsing
     * @param prompt The input prompt
     * @return A simulated response
     */
    private String simulateResponse(String response) {
        // This is just a placeholder for demonstration
    	System.out.println(response);
    	String[] split_for_message = response.split("\"message\": ");
    	String[] split_for_col = split_for_message[1].split(": \"");
    	
    	String processed_response = split_for_col[2];
    	
    	if (processed_response.contains("\",\"refusal\"")) {
    		processed_response = split_for_col[2].replace("\",\"refusal\"", "");
    	}
    	if (processed_response.contains(": null,\"annotations\": []},\"logprobs\": null,\"finish_reason\"")) {
    		processed_response = processed_response.replace(": null,\"annotations\": []},\"logprobs\": null,\"finish_reason\"", "");
    	}
    	
        return processed_response;
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
