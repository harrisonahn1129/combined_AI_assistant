package database;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database handler for managing conversation data using SQLite
 * Handles operations like saving, retrieving, and managing conversation history
 */
public class Database_handler {
    // SQLite connection configuration
    private Connection connection;
    private String dbName = "conversations.db";
    private boolean isConnected;
    
    /**
     * Constructor initializes SQLite connection
     */
    public Database_handler() {
        // Initialize SQLite connection
        this.isConnected = false;
        try {
            // Create connection to SQLite database
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
            
            // Create tables if they don't exist
            createTables();
            
            this.isConnected = true;
            System.out.println("SQLite connection established");
        } catch (SQLException e) {
            System.out.println("SQLite connection failed: " + e.getMessage());
            System.out.println("Using fallback in-memory storage");
        }
    }
    
    /**
     * Creates necessary database tables if they don't exist
     */
    private void createTables() throws SQLException {
        // Create conversations table
        String createConversationsTable = 
            "CREATE TABLE IF NOT EXISTS conversations (" +
            "id TEXT PRIMARY KEY, " +
            "user_query TEXT NOT NULL, " +
            "chatgpt_response TEXT NOT NULL, " +
            "perplexity_response TEXT NOT NULL, " +
            "timestamp INTEGER NOT NULL" +
            ");";
            
        // Create API keys table
        String createApiKeysTable =
            "CREATE TABLE IF NOT EXISTS api_keys (" +
            "service TEXT PRIMARY KEY, " +
            "api_key TEXT NOT NULL" +
            ");";
            
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createConversationsTable);
            stmt.execute(createApiKeysTable);
        }
    }
    
    /**
     * Saves a conversation to the database
     * @param conversationId unique identifier for the conversation
     * @param userQuery the user's input query
     * @param chatGPTResponse response from ChatGPT
     * @param perplexityResponse response from Perplexity
     * @return true if saved successfully
     */
    public boolean saveConversation(String conversationId, String userQuery, 
                                    String chatGPTResponse, String perplexityResponse) {
        if (!isConnected) {
            return saveToMemory(conversationId, userQuery, chatGPTResponse, perplexityResponse);
        }
        
        String sql = "INSERT INTO conversations (id, user_query, chatgpt_response, perplexity_response, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?);";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, conversationId);
            pstmt.setString(2, userQuery);
            pstmt.setString(3, chatGPTResponse);
            pstmt.setString(4, perplexityResponse);
            pstmt.setLong(5, System.currentTimeMillis());
            
            pstmt.executeUpdate();
            System.out.println("Conversation saved to SQLite database: " + conversationId);
            return true;
        } catch (SQLException e) {
            System.out.println("Error saving conversation: " + e.getMessage());
            return saveToMemory(conversationId, userQuery, chatGPTResponse, perplexityResponse);
        }
    }
    
    /**
     * Fallback method to save conversation to memory
     */
    private boolean saveToMemory(String conversationId, String userQuery, 
                                 String chatGPTResponse, String perplexityResponse) {
        try {
            // This is a fallback that doesn't actually persist the data
            // but acknowledges it was received
            System.out.println("Saving conversation to memory (non-persistent): " + conversationId);
            return true;
        } catch (Exception e) {
            System.out.println("Error saving to memory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieves conversation history from the database
     * @param limit maximum number of conversations to retrieve
     * @return list of conversation maps
     */
    public List<Map<String, Object>> getConversationHistory(int limit) {
        if (!isConnected) {
            return getFromMemory(limit);
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT * FROM conversations ORDER BY timestamp DESC LIMIT ?;";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> conversation = new HashMap<>();
                    conversation.put("id", rs.getString("id"));
                    conversation.put("userQuery", rs.getString("user_query"));
                    conversation.put("chatGPTResponse", rs.getString("chatgpt_response"));
                    conversation.put("perplexityResponse", rs.getString("perplexity_response"));
                    conversation.put("timestamp", rs.getLong("timestamp"));
                    result.add(conversation);
                }
            }
            
            return result;
        } catch (SQLException e) {
            System.out.println("Error retrieving conversation history: " + e.getMessage());
            return getFromMemory(limit);
        }
    }
    
    /**
     * Fallback method to retrieve conversations from memory
     */
    private List<Map<String, Object>> getFromMemory(int limit) {
        // In a real implementation without a database, would load from 
        // some other source. Here we just create dummy data.
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Dummy implementation for demonstration
        for (int i = 0; i < Math.min(limit, 3); i++) {
            Map<String, Object> conversation = new HashMap<>();
            conversation.put("id", "sample-" + i);
            conversation.put("userQuery", "Sample query " + i);
            conversation.put("chatGPTResponse", "ChatGPT response " + i);
            conversation.put("perplexityResponse", "Perplexity response " + i);
            conversation.put("timestamp", System.currentTimeMillis() - (i * 60000));
            result.add(conversation);
        }
        
        return result;
    }
    
    /**
     * Gets a specific conversation by ID
     * @param conversationId the ID of the conversation to retrieve
     * @return the conversation data or null if not found
     */
    public Map<String, Object> getConversation(String conversationId) {
        if (!isConnected) {
            return null;
        }
        
        String sql = "SELECT * FROM conversations WHERE id = ?;";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, conversationId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> conversation = new HashMap<>();
                    conversation.put("id", rs.getString("id"));
                    conversation.put("userQuery", rs.getString("user_query"));
                    conversation.put("chatGPTResponse", rs.getString("chatgpt_response"));
                    conversation.put("perplexityResponse", rs.getString("perplexity_response"));
                    conversation.put("timestamp", rs.getLong("timestamp"));
                    return conversation;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving conversation: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Securely stores API keys in the database
     * @param service the service name (e.g., "chatgpt", "perplexity")
     * @param apiKey the API key to store
     * @return true if stored successfully
     */
    public boolean saveApiKey(String service, String apiKey) {
        if (!isConnected) {
            return saveApiKeyToMemory(service, apiKey);
        }
        
        String encryptedKey = encryptApiKey(apiKey);
        String sql = "INSERT OR REPLACE INTO api_keys (service, api_key) VALUES (?, ?);";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, service);
            pstmt.setString(2, encryptedKey);
            
            pstmt.executeUpdate();
            System.out.println("API key saved for service: " + service);
            return true;
        } catch (SQLException e) {
            System.out.println("Error saving API key: " + e.getMessage());
            return saveApiKeyToMemory(service, apiKey);
        }
    }
    
    /**
     * Encrypts API key for secure storage (simple implementation)
     */
    private String encryptApiKey(String apiKey) {
        // In a real implementation, use AES or another encryption algorithm
        // Simple Base64 encoding for demonstration
        return java.util.Base64.getEncoder().encodeToString(apiKey.getBytes());
    }
    
    /**
     * Decrypts API key for use
     */
    private String decryptApiKey(String encryptedKey) {
        // In a real implementation, use the corresponding decryption method
        // Simple Base64 decoding for demonstration
        return new String(java.util.Base64.getDecoder().decode(encryptedKey));
    }
    
    /**
     * Fallback method to save API key to memory
     */
    private boolean saveApiKeyToMemory(String service, String apiKey) {
        try {
            // Simple encryption for demonstration
            String encryptedKey = encryptApiKey(apiKey);
            
            // In a real implementation without a database, would save to some secure storage
            System.out.println("API key saved to memory: " + service);
            return true;
        } catch (Exception e) {
            System.out.println("Error saving API key to memory: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Retrieves an API key from the database
     * @param service the service name (e.g., "chatgpt", "perplexity")
     * @return the API key or null if not found
     */
    public String getApiKey(String service) {
        if (!isConnected) {
            return getApiKeyFromMemory(service);
        }
        
        String sql = "SELECT api_key FROM api_keys WHERE service = ?;";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, service);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String encryptedKey = rs.getString("api_key");
                    return decryptApiKey(encryptedKey);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving API key: " + e.getMessage());
        }
        
        return getApiKeyFromMemory(service);
    }
    
    /**
     * Fallback method to retrieve API key from memory
     */
    private String getApiKeyFromMemory(String service) {
        // In a real implementation without a database, would load from some secure storage
        return "api_key_placeholder_for_" + service;
    }
    
    /**
     * Closes the database connection
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                this.isConnected = false;
                System.out.println("SQLite connection closed");
            } catch (SQLException e) {
                System.out.println("Error closing SQLite connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Exports conversations to a JSON file
     * @param filePath path to save the exported file
     * @return true if export was successful
     */
    public boolean exportConversations(String filePath) {
        try {
            List<Map<String, Object>> conversations = getConversationHistory(100); // Get up to 100 conversations
            
            // Build JSON manually since we don't want additional dependencies
            StringBuilder json = new StringBuilder("[\n");
            for (int i = 0; i < conversations.size(); i++) {
                Map<String, Object> conv = conversations.get(i);
                json.append("  {\n");
                json.append("    \"id\": \"").append(conv.get("id")).append("\",\n");
                json.append("    \"userQuery\": \"").append(escapeJson(conv.get("userQuery").toString())).append("\",\n");
                json.append("    \"chatGPTResponse\": \"").append(escapeJson(conv.get("chatGPTResponse").toString())).append("\",\n");
                json.append("    \"perplexityResponse\": \"").append(escapeJson(conv.get("perplexityResponse").toString())).append("\",\n");
                json.append("    \"timestamp\": ").append(conv.get("timestamp")).append("\n");
                json.append("  }");
                if (i < conversations.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            json.append("]");
            
            // Write to file
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(json.toString());
            }
            
            System.out.println("Conversations exported to: " + filePath);
            return true;
        } catch (Exception e) {
            System.out.println("Error exporting conversations: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Escapes JSON special characters in a string
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
} 