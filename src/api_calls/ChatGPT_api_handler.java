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
 * ChatGPT API call handler to manage API requests, responses, and error handling
 */
public class ChatGPT_api_handler {
    private static final String API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private String apiKey;
    private final ExecutorService executor;
    
    /**
     * Constructor initializes thread pool for API requests
     */
    public ChatGPT_api_handler() {
        // Initialize an empty API key - this will be set later through API settings
        this.apiKey = "";
        // Create a thread pool for managing concurrent API requests
        this.executor = Executors.newFixedThreadPool(2);
    }
    
    /**
     * Sets the API key for Open AI API authentication
     * @param apiKey: OpenAI API key
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
     * @param prompt: User's input prompt
     * @return CompletableFuture containing the API response
     */
    public CompletableFuture<String> makeAsyncApiCall(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return makeApiCallWithRetry(prompt);
            } catch (Exception e) {
                return "Error calling ChatGPT API: " + e.getMessage();
            }
        }, executor);
    }
    
    /**
     * Makes a synchronous call to the ChatGPT API with retry mechanism,
     * which attempts up to 3 retries with exponential backoff
     * @param prompt: User's input prompt
     * @return API response in string
     * @throws Exception if all retry attempts fail
     */
    public String makeApiCallWithRetry(String prompt) throws Exception {
        int maxRetries = 3;
        int retryCount = 0;
        int retryDelayMs = 1000; // Initial delay of 1 second
        
        while (retryCount < maxRetries) {
            try {
                return makeApiCall(prompt);
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new Exception("Maximum retry attempts reached: " + e.getMessage());
                }
                
                System.out.println("API call failed, retrying in " + retryDelayMs/1000 + " seconds... (" + retryCount + "/" + maxRetries + ")");
                
                try {
                    Thread.sleep(retryDelayMs);
                    // Exponential backoff: double the delay for next retry
                    retryDelayMs *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new Exception("API call interrupted: " + e.getMessage());
                }
            }
        }
        
        // This should never be reached due to the exception in the retry loop
        return "Error: Failed to get response after multiple retries";
    }
    
    /**
     * Makes a synchronous call to the ChatGPT API
     * @param prompt: User's input prompt
     * @return API response in string
     * @throws Exception if the API call fails
     */
    public String makeApiCall(String prompt) throws Exception {
        if (!hasValidApiKey()) {
            return "Error: API key not set. Please configure your OpenAI API key in API Settings.";
        }
        
        // Create connection to the API endpoint
        URL url = new URL(API_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000); // 10 seconds connection timeout
        connection.setReadTimeout(30000);    // 30 seconds read timeout
        
        // Add system message to improve response formatting
        String systemPrompt = "Be precise and concise. Do not use LaTeX, markdown formatting, or symbols like [1][2] for references. " +
                            "Use plain, conversational language as if speaking directly to a person. " +
                            "Format information clearly with regular bullet points for lists. " +
                            "Use everyday language and avoid academic or technical jargon when possible. " +
                            "Return only the actual answer content, without any metadata, json, or citations. " +
                            "Do not use any markdown formatting, especially no asterisks (**) for bold text. " +
                            "Do not include any special Unicode characters like \\u2022.";
                            
        String jsonInputString = String.format(
            "{\"model\": \"gpt-4.1-nano\", \"messages\": [{\"role\": \"system\", \"content\": \"%s\"}, {\"role\": \"user\", \"content\": \"%s\"}]}",
            systemPrompt.replace("\"", "\\\""),
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
        
        // Return the parsed JSON response, which the message content is extract
        return parseJsonResponse(response.toString());
    }
    
    /**
     * Parses the JSON response from the ChatGPT API to extract message content
     * @param jsonResponse: The raw JSON response from the API
     * @return The extracted message content in string
     */
    private String parseJsonResponse(String jsonResponse) {
        try {
            // Extract content from choices[0].message.content
            int choicesIndex = jsonResponse.indexOf("\"choices\"");
            if (choicesIndex >= 0) {
                int messageIndex = jsonResponse.indexOf("\"message\"", choicesIndex);
                if (messageIndex >= 0) {
                    int contentIndex = jsonResponse.indexOf("\"content\"", messageIndex);
                    if (contentIndex >= 0) {
                        int startQuote = jsonResponse.indexOf("\"", contentIndex + "\"content\"".length());
                        if (startQuote >= 0) {
                            startQuote++; // Move past the opening quote
                            int endQuote = -1;
                            // Find the closing quote (avoiding escaped quotes)
                            boolean foundEndQuote = false;
                            for (int i = startQuote; i < jsonResponse.length(); i++) {
                                // Check for escape character
                                if (jsonResponse.charAt(i) == '\\') {
                                    // Skip the next character
                                    i++;
                                    continue;
                                }
                                // Found an unescaped quote
                                if (jsonResponse.charAt(i) == '"') {
                                    endQuote = i;
                                    foundEndQuote = true;
                                    break;
                                }
                            }
                            
                            if (foundEndQuote) {
                                // Process JSON escapes and clean formatting
                                String content = jsonResponse.substring(startQuote, endQuote);
                                content = cleanResponse(content);
                                return content;
                            }
                        }
                    }
                }
            }
            
            // Return error message if parsing fails
            return "ChatGPT response parsing failed. Please try again.";
        } catch (Exception e) {
            e.printStackTrace();
            return "JSON parsing error: " + e.getMessage();
        }
    }
    
    /**
     * Cleans the response text to remove LaTeX, markdown, and special formatting
     * @param text: The text to clean
     * @return The cleaned text in string
     */
    private String cleanResponse(String text) {
        if (text == null) {
            return "";
        }
        
        // Handle escaped characters
        String result = text.replace("\\\"", "\"")
                           .replace("\\n", "\n")
                           .replace("\\r", "\r")
                           .replace("\\t", "\t")
                           .replace("\\\\", "\\")
                           .replace("\\/", "/");
        
        // Replace LaTeX equations (both inline and block)
        result = result.replaceAll("\\$\\$(.*?)\\$\\$", "");
        result = result.replaceAll("\\$(.*?)\\$", "");
        
        // Remove markdown headers
        result = result.replaceAll("#+\\s+", "");
        
        // Remove markdown bold/italic
        result = result.replaceAll("\\*\\*(.*?)\\*\\*", "$1"); // Bold (**text**)
        result = result.replaceAll("__(.*?)__", "$1");         // Underline (__text__)
        result = result.replaceAll("\\*(.*?)\\*", "$1");       // Italic (*text*)
        result = result.replaceAll("_(.*?)_", "$1");           // Underline (_text_)
        
        // Remove markdown code blocks
        result = result.replaceAll("```[\\s\\S]*?```", "");
        result = result.replaceAll("`(.*?)`", "$1");
        
        // Remove bullet points (both * and • Unicode character)
        result = result.replaceAll("^\\s*[\\*•]\\s+", "");
        
        // Remove numbered lists
        result = result.replaceAll("^\\s*\\d+\\.\\s+", "");
        
        // Replace Unicode bullet points with regular bullet points
        result = result.replaceAll("\\\\u2022", "•");          // \u2022 -> •
        result = result.replaceAll("/u2022", "•");             // /u2022 -> •
        
        // Fix any other problematic characters 
        result = result.replaceAll("\\\\u([0-9a-fA-F]{4})", ""); // Remove other Unicode escapes
        
        // Fix multiple consecutive newlines
        result = result.replaceAll("\\n{3,}", "\n\n");
        
        return result.trim();
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